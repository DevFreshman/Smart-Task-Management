package com.github.hoangducmanh.smart_task_management.application.auth.usecase;

import com.github.hoangducmanh.smart_task_management.application.ClockSystem;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.StoredEmailToken;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.VerifyEmailCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.EmailMismatchException;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.TokenDoesNotMatchException;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.UserNotFoundException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class VerifyEmailUseCaseTest {

    private UserRepository userRepository;
    private EmailVerificationTokenStore emailTokenRepository;
    private EmailTokenHashPort emailTokenHashPort;
    private ClockSystem clockSystem;
    private VerifyEmailUseCase verifyEmailUseCase;

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String USER_EMAIL = "user@example.com";
    private static final Instant REGISTERED_AT = Instant.parse("2026-03-01T08:00:00Z");
    private static final Instant NOW = Instant.parse("2026-03-03T10:15:00Z");

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        emailTokenRepository = mock(EmailVerificationTokenStore.class);
        emailTokenHashPort = mock(EmailTokenHashPort.class);
        clockSystem = mock(ClockSystem.class);
        verifyEmailUseCase = new VerifyEmailUseCase(userRepository, emailTokenRepository, emailTokenHashPort, clockSystem);
    }

    // Case: Xác thực email thành công — full flow.
    // Verify: hash raw token → tìm user theo userId → kiểm tra email khớp user → consume token từ store
    //   → kiểm tra email trong stored token khớp → gọi markEmailAsVerified → save user.
    // Dùng ArgumentCaptor verify user được save với emailStatus = VERIFIED và auditInfo.updatedAt == now.
    // User phải ở trạng thái PENDING_VERIFICATION trước khi verify (domain rule).
    @Test
    void execute_shouldVerifyEmailSuccessfully() {
        VerifyEmailCommand command = VerifyEmailCommand.of(USER_ID, USER_EMAIL, "raw-token");
        User user = createPendingVerificationUser();
        StoredEmailToken storedToken = StoredEmailToken.of(USER_ID, USER_EMAIL, "hashed-token");

        when(emailTokenHashPort.hash("raw-token")).thenReturn("hashed-token");
        when(userRepository.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));
        when(emailTokenRepository.consumeIfMatches(USER_ID, "hashed-token")).thenReturn(Optional.of(storedToken));
        when(clockSystem.now()).thenReturn(NOW);

        verifyEmailUseCase.execute(command);

        // Verify token được hash trước khi consume
        verify(emailTokenHashPort).hash("raw-token");
        verify(emailTokenRepository).consumeIfMatches(USER_ID, "hashed-token");

        // Verify user được save với trạng thái VERIFIED
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(EmailStatus.VERIFIED, savedUser.getEmailStatus());
        assertEquals(NOW, savedUser.getAuditInfo().updatedAt());
    }

    // Case: User không tồn tại (userId không tìm thấy trong repository).
    // Verify throw UserNotFoundException với message chính xác.
    // Verify side effects không xảy ra: không consume token, không save user.
    @Test
    void execute_shouldThrowWhenUserNotFound() {
        VerifyEmailCommand command = VerifyEmailCommand.of(USER_ID, USER_EMAIL, "raw-token");

        when(emailTokenHashPort.hash("raw-token")).thenReturn("hashed-token");
        when(userRepository.findById(UserId.of(USER_ID))).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> verifyEmailUseCase.execute(command)
        );

        assertEquals("User not found for the given user ID", exception.getMessage());
        verify(userRepository).findById(UserId.of(USER_ID));
        verify(emailTokenRepository, never()).consumeIfMatches(any(), any());
        verify(userRepository, never()).save(any(User.class));
    }

    // Case: Email trong command không khớp email của user trong database.
    // Ví dụ: user đã đổi email sau khi yêu cầu xác thực, hoặc attacker dùng userId của người khác.
    // Verify throw EmailMismatchException, không consume token, không save user.
    @Test
    void execute_shouldThrowWhenEmailDoesNotMatchUser() {
        VerifyEmailCommand command = VerifyEmailCommand.of(USER_ID, "other@example.com", "raw-token");
        User user = createPendingVerificationUser();

        when(emailTokenHashPort.hash("raw-token")).thenReturn("hashed-token");
        when(userRepository.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));

        EmailMismatchException exception = assertThrows(
            EmailMismatchException.class,
            () -> verifyEmailUseCase.execute(command)
        );

        assertEquals("Provided email does not match user's email", exception.getMessage());
        verify(emailTokenRepository, never()).consumeIfMatches(any(), any());
        verify(userRepository, never()).save(any(User.class));
    }

    // Case: Token không tồn tại hoặc không khớp (đã hết hạn, đã dùng, hoặc sai token).
    // consumeIfMatches trả về Optional.empty() → throw TokenDoesNotMatchException.
    // Verify không gọi markEmailAsVerified, không save user.
    @Test
    void execute_shouldThrowWhenTokenDoesNotMatch() {
        VerifyEmailCommand command = VerifyEmailCommand.of(USER_ID, USER_EMAIL, "wrong-token");
        User user = createPendingVerificationUser();

        when(emailTokenHashPort.hash("wrong-token")).thenReturn("hashed-wrong-token");
        when(userRepository.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));
        when(emailTokenRepository.consumeIfMatches(USER_ID, "hashed-wrong-token")).thenReturn(Optional.empty());

        TokenDoesNotMatchException exception = assertThrows(
            TokenDoesNotMatchException.class,
            () -> verifyEmailUseCase.execute(command)
        );

        assertEquals("Email verification token does not match", exception.getMessage());
        verify(emailTokenRepository).consumeIfMatches(USER_ID, "hashed-wrong-token");
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(clockSystem);
    }

    // Case: Token khớp nhưng email trong stored token khác email trong command.
    // Tình huống: token được tạo cho email cũ, user đổi email rồi dùng token cũ với email mới.
    // Verify throw EmailMismatchException với message riêng, không save user.
    @Test
    void execute_shouldThrowWhenStoredTokenEmailDoesNotMatchCommandEmail() {
        VerifyEmailCommand command = VerifyEmailCommand.of(USER_ID, USER_EMAIL, "raw-token");
        User user = createPendingVerificationUser();
        StoredEmailToken storedToken = StoredEmailToken.of(USER_ID, "old-email@example.com", "hashed-token");

        when(emailTokenHashPort.hash("raw-token")).thenReturn("hashed-token");
        when(userRepository.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));
        when(emailTokenRepository.consumeIfMatches(USER_ID, "hashed-token")).thenReturn(Optional.of(storedToken));

        EmailMismatchException exception = assertThrows(
            EmailMismatchException.class,
            () -> verifyEmailUseCase.execute(command)
        );

        assertEquals("Provided email does not match the email associated with the token", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(clockSystem);
    }

    // Case: Verify raw token KHÔNG bao giờ được truyền trực tiếp vào store — luôn hash trước.
    // Đảm bảo consumeIfMatches nhận hashed token, không phải raw token.
    @Test
    void execute_shouldHashTokenBeforeConsumption() {
        VerifyEmailCommand command = VerifyEmailCommand.of(USER_ID, USER_EMAIL, "raw-token-value");
        User user = createPendingVerificationUser();
        StoredEmailToken storedToken = StoredEmailToken.of(USER_ID, USER_EMAIL, "deterministic-hash");

        when(emailTokenHashPort.hash("raw-token-value")).thenReturn("deterministic-hash");
        when(userRepository.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));
        when(emailTokenRepository.consumeIfMatches(USER_ID, "deterministic-hash")).thenReturn(Optional.of(storedToken));
        when(clockSystem.now()).thenReturn(NOW);

        verifyEmailUseCase.execute(command);

        // Verify chỉ hash mới được dùng, raw token không được truyền trực tiếp
        verify(emailTokenRepository).consumeIfMatches(eq(USER_ID), eq("deterministic-hash"));
        verify(emailTokenRepository, never()).consumeIfMatches(eq(USER_ID), eq("raw-token-value"));
    }

    /**
     * Tạo user ở trạng thái PENDING_VERIFICATION (domain rule yêu cầu phải qua bước requestVerification
     * trước khi verify, nếu không sẽ throw InvalidEmailStatusTransitionException).
     */
    private User createPendingVerificationUser() {
        User user = User.register(
            UserId.fromString("11111111-1111-1111-1111-111111111111"),
            Email.of(USER_EMAIL),
            "John Doe",
            HashedPassword.of("stored-hash"),
            REGISTERED_AT
        );
        user.requestEmailVerification(REGISTERED_AT.plusSeconds(60));
        return user;
    }
}
