package com.github.hoangducmanh.smart_task_management.application.auth.usecase;

import com.github.hoangducmanh.smart_task_management.application.ClockSystem;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.RequestEmailVerificationCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.StoredEmailToken;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.EmailMismatchException;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.UserNotFoundException;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.email.SendEmailVerificationPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.token.EmailTokenGeneratorPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.token.EmailTokenHashPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.token.EmailVerificationTokenStore;
import com.github.hoangducmanh.smart_task_management.domain.user.model.Email;
import com.github.hoangducmanh.smart_task_management.domain.user.model.EmailStatus;
import com.github.hoangducmanh.smart_task_management.domain.user.model.HashedPassword;
import com.github.hoangducmanh.smart_task_management.domain.user.model.User;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import com.github.hoangducmanh.smart_task_management.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RequestEmailVerificationUseCaseTest {

    private EmailTokenGeneratorPort emailTokenGeneratorPort;
    private EmailTokenHashPort emailTokenHashPort;
    private UserRepository userRepository;
    private SendEmailVerificationPort sendEmailVerificationPort;
    private EmailVerificationTokenStore emailTokenRepository;
    private ClockSystem clockSystem;
    private RequestEmailVerificationUseCase requestEmailVerificationUseCase;

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String USER_EMAIL = "user@example.com";
    private static final Instant REGISTERED_AT = Instant.parse("2026-03-01T08:00:00Z");
    private static final Instant NOW = Instant.parse("2026-03-03T10:15:00Z");

    @BeforeEach
    void setUp() {
        emailTokenGeneratorPort = mock(EmailTokenGeneratorPort.class);
        emailTokenHashPort = mock(EmailTokenHashPort.class);
        userRepository = mock(UserRepository.class);
        sendEmailVerificationPort = mock(SendEmailVerificationPort.class);
        emailTokenRepository = mock(EmailVerificationTokenStore.class);
        clockSystem = mock(ClockSystem.class);
        requestEmailVerificationUseCase = new RequestEmailVerificationUseCase(
            emailTokenGeneratorPort, emailTokenHashPort, userRepository,
            sendEmailVerificationPort, emailTokenRepository, clockSystem
        );
    }

    // Case: Yêu cầu xác thực email thành công — full flow.
    // Verify: tìm user → kiểm tra email khớp → gọi requestEmailVerification (chuyển status sang PENDING_VERIFICATION)
    //   → generate token → hash token → saveOrReplace stored token (với hashed token, không phải raw)
    //   → save user (cập nhật emailStatus) → gửi email chứa raw token (để user click link).
    // Dùng ArgumentCaptor verify StoredEmailToken có đúng userId, email, và hashed token.
    // Dùng ArgumentCaptor verify user được save với emailStatus = PENDING_VERIFICATION.
    @Test
    void execute_shouldRequestEmailVerificationSuccessfully() {
        RequestEmailVerificationCommand command = RequestEmailVerificationCommand.of(USER_ID, USER_EMAIL);
        User user = createUser();

        when(clockSystem.now()).thenReturn(NOW);
        when(userRepository.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));
        when(emailTokenGeneratorPort.generateEmailToken()).thenReturn("raw-email-token");
        when(emailTokenHashPort.hash("raw-email-token")).thenReturn("hashed-email-token");

        requestEmailVerificationUseCase.execute(command);

        // Verify tìm user đúng userId
        verify(userRepository).findById(UserId.of(USER_ID));

        // Verify stored token chứa hashed token, không phải raw token
        ArgumentCaptor<StoredEmailToken> tokenCaptor = ArgumentCaptor.forClass(StoredEmailToken.class);
        verify(emailTokenRepository).saveOrReplace(tokenCaptor.capture());
        StoredEmailToken savedToken = tokenCaptor.getValue();
        assertEquals(USER_ID, savedToken.userId());
        assertEquals(USER_EMAIL, savedToken.email());
        assertEquals("hashed-email-token", savedToken.hashedToken());

        // Verify user được save với trạng thái PENDING_VERIFICATION
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(EmailStatus.PENDING_VERIFICATION, savedUser.getEmailStatus());
        assertEquals(NOW, savedUser.getAuditInfo().updatedAt());

        // Verify email gửi đi chứa raw token (để user click link xác thực), không phải hashed token
        verify(sendEmailVerificationPort).sendEmailVerification(USER_EMAIL, "raw-email-token");
    }

    // Case: User không tồn tại (userId không tìm thấy trong repository).
    // Verify throw UserNotFoundException với message chính xác.
    // Verify side effects không xảy ra: không generate token, không hash, không save, không gửi email.
    @Test
    void execute_shouldThrowWhenUserNotFound() {
        RequestEmailVerificationCommand command = RequestEmailVerificationCommand.of(USER_ID, USER_EMAIL);

        when(userRepository.findById(UserId.of(USER_ID))).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> requestEmailVerificationUseCase.execute(command)
        );

        assertEquals("User not found for the given user ID", exception.getMessage());
        verify(userRepository).findById(UserId.of(USER_ID));
        verifyNoInteractions(emailTokenGeneratorPort, emailTokenHashPort, emailTokenRepository, sendEmailVerificationPort);
        verify(userRepository, never()).save(any(User.class));
    }

    // Case: Email trong command không khớp email của user trong database.
    // Ví dụ: user đổi email nhưng client còn cache email cũ, hoặc attacker dùng userId với email giả.
    // Verify throw EmailMismatchException, không generate token, không gửi email.
    @Test
    void execute_shouldThrowWhenEmailDoesNotMatchUser() {
        RequestEmailVerificationCommand command = RequestEmailVerificationCommand.of(USER_ID, "other@example.com");
        User user = createUser();

        when(userRepository.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));

        EmailMismatchException exception = assertThrows(
            EmailMismatchException.class,
            () -> requestEmailVerificationUseCase.execute(command)
        );

        assertEquals("Provided email does not match user's email", exception.getMessage());
        verifyNoInteractions(emailTokenGeneratorPort, emailTokenHashPort, emailTokenRepository, sendEmailVerificationPort, clockSystem);
        verify(userRepository, never()).save(any(User.class));
    }

    // Case: Verify email gửi đi chứa raw token, KHÔNG phải hashed token.
    // Mục đích: user nhận email với raw token → click link → server hash lại để so sánh.
    // Nếu gửi hashed token thì flow xác thực sẽ bị hỏng.
    @Test
    void execute_shouldSendRawTokenInEmailNotHashedToken() {
        RequestEmailVerificationCommand command = RequestEmailVerificationCommand.of(USER_ID, USER_EMAIL);
        User user = createUser();

        when(clockSystem.now()).thenReturn(NOW);
        when(userRepository.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));
        when(emailTokenGeneratorPort.generateEmailToken()).thenReturn("raw-verification-token");
        when(emailTokenHashPort.hash("raw-verification-token")).thenReturn("hashed-verification-token");

        requestEmailVerificationUseCase.execute(command);

        // Verify email nhận raw token, không phải hashed token
        verify(sendEmailVerificationPort).sendEmailVerification(USER_EMAIL, "raw-verification-token");
        verify(sendEmailVerificationPort, never()).sendEmailVerification(any(), org.mockito.ArgumentMatchers.eq("hashed-verification-token"));
    }

    // Case: Verify stored token lưu hashed token, KHÔNG phải raw token (bảo mật).
    // Mục đích: nếu database bị leak, attacker không thể dùng stored token trực tiếp vì đã bị hash.
    @Test
    void execute_shouldStoreHashedTokenNotRawToken() {
        RequestEmailVerificationCommand command = RequestEmailVerificationCommand.of(USER_ID, USER_EMAIL);
        User user = createUser();

        when(clockSystem.now()).thenReturn(NOW);
        when(userRepository.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));
        when(emailTokenGeneratorPort.generateEmailToken()).thenReturn("secret-raw-token");
        when(emailTokenHashPort.hash("secret-raw-token")).thenReturn("safe-hashed-token");

        requestEmailVerificationUseCase.execute(command);

        // Verify token được hash trước khi lưu
        ArgumentCaptor<StoredEmailToken> tokenCaptor = ArgumentCaptor.forClass(StoredEmailToken.class);
        verify(emailTokenRepository).saveOrReplace(tokenCaptor.capture());
        assertEquals("safe-hashed-token", tokenCaptor.getValue().hashedToken());
    }

    private User createUser() {
        return User.register(
            UserId.fromString("11111111-1111-1111-1111-111111111111"),
            Email.of(USER_EMAIL),
            "John Doe",
            HashedPassword.of("stored-hash"),
            REGISTERED_AT
        );
    }
}
