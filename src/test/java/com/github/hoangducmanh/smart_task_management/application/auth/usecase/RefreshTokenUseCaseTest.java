package com.github.hoangducmanh.smart_task_management.application.auth.usecase;

import com.github.hoangducmanh.smart_task_management.application.ClockSystem;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.RefreshTokenCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.RefreshTokenResult;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.StoredRefreshToken;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.InvalidTokenException;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.UserNotFoundException;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.RefreshTokenRepository;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.TokenGeneratorPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.TokenHashPort;
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

class RefreshTokenUseCaseTest {

    private RefreshTokenRepository refreshTokenRepository;
    private TokenHashPort tokenHashPort;
    private TokenGeneratorPort tokenGeneratorPort;
    private UserRepository userRepository;
    private ClockSystem clockSystem;
    private RefreshTokenUseCase refreshTokenUseCase;

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final Instant NOW = Instant.parse("2026-03-03T10:15:00Z");

    @BeforeEach
    void setUp() {
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        tokenHashPort = mock(TokenHashPort.class);
        tokenGeneratorPort = mock(TokenGeneratorPort.class);
        userRepository = mock(UserRepository.class);
        clockSystem = mock(ClockSystem.class);
        refreshTokenUseCase = new RefreshTokenUseCase(
            refreshTokenRepository, tokenHashPort, tokenGeneratorPort, userRepository, clockSystem
        );
    }

    // Case: Refresh token thành công.
    // Verify full flow: hash token cũ → consume lấy userId → tìm user → generate token mới → hash + save refresh token mới → trả về kết quả.
    // Dùng ArgumentCaptor verify StoredRefreshToken được save với hash mới và đúng userId.
    @Test
    void execute_shouldRefreshTokensAndReturnNewPair() {
        RefreshTokenCommand command = RefreshTokenCommand.of("old-refresh-token");
        User user = createUser();

        when(clockSystem.now()).thenReturn(NOW);
        when(tokenHashPort.hash("old-refresh-token")).thenReturn("hashed-old-token");
        when(refreshTokenRepository.consumeAndGetUserId("hashed-old-token", NOW)).thenReturn(Optional.of(USER_ID));
        when(userRepository.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));
        when(tokenGeneratorPort.generateRefreshToken()).thenReturn("new-refresh-token");
        when(tokenHashPort.hash("new-refresh-token")).thenReturn("hashed-new-token");
        when(tokenGeneratorPort.generateAccessToken(UserId.of(USER_ID), user.getRole())).thenReturn("new-access-token");

        RefreshTokenResult result = refreshTokenUseCase.execute(command);

        assertEquals("new-access-token", result.accessToken());
        assertEquals("new-refresh-token", result.refreshToken());

        // Verify refresh token cũ được hash trước khi consume
        verify(tokenHashPort).hash("old-refresh-token");
        verify(refreshTokenRepository).consumeAndGetUserId("hashed-old-token", NOW);

        // Verify refresh token mới được hash và save đúng
        ArgumentCaptor<StoredRefreshToken> tokenCaptor = ArgumentCaptor.forClass(StoredRefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        StoredRefreshToken savedToken = tokenCaptor.getValue();
        assertEquals("hashed-new-token", savedToken.hashToken());
        assertEquals(USER_ID, savedToken.userId());

        // Verify access token được generate với đúng userId và role
        verify(tokenGeneratorPort).generateAccessToken(UserId.of(USER_ID), user.getRole());
    }

    // Case: Refresh token không tồn tại hoặc đã hết hạn.
    // consumeAndGetUserId trả về Optional.empty() → throw InvalidTokenException.
    // Verify side effects không xảy ra: không tìm user, không generate token mới, không save token mới.
    @Test
    void execute_shouldThrowWhenRefreshTokenNotExistsOrExpired() {
        RefreshTokenCommand command = RefreshTokenCommand.of("invalid-refresh-token");

        when(clockSystem.now()).thenReturn(NOW);
        when(tokenHashPort.hash("invalid-refresh-token")).thenReturn("hashed-invalid-token");
        when(refreshTokenRepository.consumeAndGetUserId("hashed-invalid-token", NOW)).thenReturn(Optional.empty());

        InvalidTokenException exception = assertThrows(
            InvalidTokenException.class,
            () -> refreshTokenUseCase.execute(command)
        );

        assertEquals("Refresh token not exists or expired", exception.getMessage());
        verify(tokenHashPort).hash("invalid-refresh-token");
        verify(refreshTokenRepository).consumeAndGetUserId("hashed-invalid-token", NOW);
        verifyNoInteractions(userRepository);
        verify(tokenGeneratorPort, never()).generateRefreshToken();
        verify(tokenGeneratorPort, never()).generateAccessToken(any(UserId.class), any());
        verify(refreshTokenRepository, never()).save(any(StoredRefreshToken.class));
    }

    // Case: User không tồn tại (đã bị xóa sau khi token được cấp).
    // consumeAndGetUserId thành công nhưng findById trả về empty → throw UserNotFoundException.
    // Verify không generate token mới, không save token mới.
    @Test
    void execute_shouldThrowWhenUserNotFound() {
        RefreshTokenCommand command = RefreshTokenCommand.of("valid-refresh-token");

        when(clockSystem.now()).thenReturn(NOW);
        when(tokenHashPort.hash("valid-refresh-token")).thenReturn("hashed-valid-token");
        when(refreshTokenRepository.consumeAndGetUserId("hashed-valid-token", NOW)).thenReturn(Optional.of(USER_ID));
        when(userRepository.findById(UserId.of(USER_ID))).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> refreshTokenUseCase.execute(command)
        );

        assertEquals("User not found for the given refresh token", exception.getMessage());
        verify(refreshTokenRepository).consumeAndGetUserId("hashed-valid-token", NOW);
        verify(userRepository).findById(UserId.of(USER_ID));
        verify(tokenGeneratorPort, never()).generateRefreshToken();
        verify(tokenGeneratorPort, never()).generateAccessToken(any(UserId.class), any());
        verify(refreshTokenRepository, never()).save(any(StoredRefreshToken.class));
    }

    // Case: Verify token cũ được hash trước khi gửi đến repository.
    // Đảm bảo không bao giờ truyền raw token vào consumeAndGetUserId — luôn hash trước.
    @Test
    void execute_shouldHashTokenBeforeConsumption() {
        RefreshTokenCommand command = RefreshTokenCommand.of("raw-token-value");
        User user = createUser();

        when(clockSystem.now()).thenReturn(NOW);
        when(tokenHashPort.hash("raw-token-value")).thenReturn("deterministic-hash");
        when(refreshTokenRepository.consumeAndGetUserId("deterministic-hash", NOW)).thenReturn(Optional.of(USER_ID));
        when(userRepository.findById(UserId.of(USER_ID))).thenReturn(Optional.of(user));
        when(tokenGeneratorPort.generateRefreshToken()).thenReturn("new-token");
        when(tokenHashPort.hash("new-token")).thenReturn("new-hash");
        when(tokenGeneratorPort.generateAccessToken(any(UserId.class), any())).thenReturn("access");

        refreshTokenUseCase.execute(command);

        // Verify raw token KHÔNG được truyền trực tiếp — chỉ hash mới được dùng
        verify(refreshTokenRepository).consumeAndGetUserId(eq("deterministic-hash"), eq(NOW));
        verify(refreshTokenRepository, never()).consumeAndGetUserId(eq("raw-token-value"), any());
    }

    private User createUser() {
        return User.register(
            UserId.fromString("11111111-1111-1111-1111-111111111111"),
            Email.of("user@example.com"),
            "John Doe",
            HashedPassword.of("stored-hash"),
            Instant.parse("2026-03-01T08:00:00Z")
        );
    }
}
