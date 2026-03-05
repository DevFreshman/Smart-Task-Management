package com.github.hoangducmanh.smart_task_management.application.auth.usecase;

import com.github.hoangducmanh.smart_task_management.application.ClockSystem;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.LoginCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.LoginResult;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.StoredRefreshToken;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.EmailNotExistsException;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.PasswordMismatchException;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.PasswordHashPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.RefreshTokenRepository;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.TokenGeneratorPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.TokenHashPort;
import com.github.hoangducmanh.smart_task_management.domain.user.exception.InvalidEmailException;
import com.github.hoangducmanh.smart_task_management.domain.user.model.Email;
import com.github.hoangducmanh.smart_task_management.domain.user.model.HashedPassword;
import com.github.hoangducmanh.smart_task_management.domain.user.model.User;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import com.github.hoangducmanh.smart_task_management.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LoginUseCaseTest {
    private RefreshTokenRepository tokenRepository;
    private PasswordHashPort passwordHashPort;
    private TokenGeneratorPort tokenGeneratorPort;
    private TokenHashPort tokenHashPort;
    private UserRepository userRepository;
    private ClockSystem clockSystem;
    private LoginUseCase loginUseCase;

    @BeforeEach
    void setUp() {
        tokenRepository = mock(RefreshTokenRepository.class);
        passwordHashPort = mock(PasswordHashPort.class);
        tokenGeneratorPort = mock(TokenGeneratorPort.class);
        tokenHashPort = mock(TokenHashPort.class);
        userRepository = mock(UserRepository.class);
        clockSystem = mock(ClockSystem.class);
        loginUseCase = new LoginUseCase(
            tokenRepository,
            tokenGeneratorPort,
            passwordHashPort,
            tokenHashPort,
            userRepository,
            clockSystem
        );
    }

    // Case: Đăng nhập thành công.
    // Verify đủ flow: tìm user theo email normalized, check mật khẩu, tạo token, hash + lưu refresh token mới, và trả về LoginResult.
    @Test
    void execute_shouldLoginAndReturnTokens() {
        LoginCommand command = LoginCommand.of("  USER@Example.com ", "raw-password");
        Instant now = Instant.parse("2026-03-03T10:15:00Z");
        User user = User.register(
            UserId.fromString("11111111-1111-1111-1111-111111111111"),
            Email.of("user@example.com"),
            "John Doe",
            HashedPassword.of("stored-hash"),
            Instant.parse("2026-03-03T10:15:00Z")
        );

        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
        when(passwordHashPort.matches("raw-password", "stored-hash")).thenReturn(true);
        when(tokenGeneratorPort.generateRefreshToken()).thenReturn("refresh-token");
        when(tokenHashPort.hash("refresh-token")).thenReturn("refresh-token-hash");
        when(tokenGeneratorPort.generateAccessToken(user.getId(), user.getRole())).thenReturn("access-token");
        when(clockSystem.now()).thenReturn(now);

        LoginResult result = loginUseCase.execute(command);

        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        verify(userRepository).findByEmail(emailCaptor.capture());
        assertEquals("user@example.com", emailCaptor.getValue().asString());

        verify(passwordHashPort).matches("raw-password", "stored-hash");
        verify(tokenRepository).revokeByUserId(user.getId().value(), now);

        ArgumentCaptor<StoredRefreshToken> savedTokenCaptor = ArgumentCaptor.forClass(StoredRefreshToken.class);
        verify(tokenRepository).save(savedTokenCaptor.capture());
        StoredRefreshToken savedToken = savedTokenCaptor.getValue();
        assertEquals("refresh-token-hash", savedToken.hashToken());
        assertEquals(user.getId().value(), savedToken.userId());

        verify(tokenGeneratorPort).generateAccessToken(user.getId(), user.getRole());
        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
    }

    // Case: Email không tồn tại.
    // Verify throw đúng EmailNotExistsException và không tạo token / không ghi refresh token.
    @Test
    void execute_shouldThrowWhenEmailNotExists() {
        LoginCommand command = LoginCommand.of("user@example.com", "raw-password");
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

        EmailNotExistsException exception = assertThrows(
            EmailNotExistsException.class,
            () -> loginUseCase.execute(command)
        );

        assertEquals("Email not exists", exception.getMessage());
        verify(userRepository).findByEmail(Email.of("user@example.com"));
        verifyNoInteractions(passwordHashPort, tokenGeneratorPort, tokenHashPort, tokenRepository);
    }

    // Case: Sai mật khẩu.
    // Verify password check fail thì dừng flow ngay: không generate token, không delete/save refresh token.
    @Test
    void execute_shouldThrowWhenPasswordMismatch() {
        LoginCommand command = LoginCommand.of("user@example.com", "wrong-password");
        User user = User.register(
            UserId.fromString("11111111-1111-1111-1111-111111111111"),
            Email.of("user@example.com"),
            "John Doe",
            HashedPassword.of("stored-hash"),
            Instant.parse("2026-03-03T10:15:00Z")
        );
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
        when(passwordHashPort.matches("wrong-password", "stored-hash")).thenReturn(false);

        PasswordMismatchException exception = assertThrows(
            PasswordMismatchException.class,
            () -> loginUseCase.execute(command)
        );

        assertEquals("Password does not match", exception.getMessage());
        verify(passwordHashPort).matches("wrong-password", "stored-hash");
        verify(tokenGeneratorPort, never()).generateRefreshToken();
        verify(tokenGeneratorPort, never()).generateAccessToken(any(UserId.class), any());
        verifyNoInteractions(tokenHashPort, tokenRepository);
    }

    // Case: Email input invalid.
    // Verify fail tại Email.of() (domain validation) trước khi chạm repository hoặc các dependency khác.
    @Test
    void execute_shouldThrowWhenEmailIsInvalid() {
        LoginCommand command = LoginCommand.of("not-an-email", "raw-password");

        assertThrows(InvalidEmailException.class, () -> loginUseCase.execute(command));

        verifyNoInteractions(userRepository, passwordHashPort, tokenGeneratorPort, tokenHashPort, tokenRepository);
    }
}
