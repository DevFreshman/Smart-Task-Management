package com.github.hoangducmanh.smart_task_management.application.auth.usecase;

import java.util.UUID;

import com.github.hoangducmanh.smart_task_management.application.ClockSystem;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.RefreshTokenCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.RefreshTokenResult;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.StoredRefreshToken;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.InvalidTokenException;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.UserNotFoundException;
import com.github.hoangducmanh.smart_task_management.application.auth.port.in.RefreshTokenPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.RefreshTokenRepository;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.TokenGeneratorPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.TokenHashPort;
import com.github.hoangducmanh.smart_task_management.domain.user.model.User;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import com.github.hoangducmanh.smart_task_management.domain.user.repository.UserRepository;

public class RefreshTokenUseCase implements RefreshTokenPort {
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHashPort tokenHashPort;
    private final TokenGeneratorPort tokenGenerator;
    private final UserRepository userRepository;
    private final ClockSystem clockSystem;

    public RefreshTokenUseCase(RefreshTokenRepository refreshTokenRepository, TokenHashPort tokenHashPort, 
        TokenGeneratorPort tokenGenerator, UserRepository userRepository, ClockSystem clockSystem) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenHashPort = tokenHashPort;
        this.tokenGenerator = tokenGenerator;
        this.userRepository = userRepository;
        this.clockSystem = clockSystem;
    }

    @Override
    public RefreshTokenResult execute(RefreshTokenCommand command) {
        String refreshToken = command.refreshToken();
        String hashRefreshToken = tokenHashPort.hash(refreshToken);
        UUID userId = refreshTokenRepository
            .consumeAndGetUserId(hashRefreshToken, clockSystem.now()).orElseThrow(
                () -> new InvalidTokenException("Refresh token not exists or expired")
            );
        User user = userRepository.findById(UserId.of(userId)).orElseThrow(
            () -> new UserNotFoundException("User not found for the given refresh token")
        );
        String newRefreshToken = tokenGenerator.generateRefreshToken();
        String hashNewRefreshToken = tokenHashPort.hash(newRefreshToken);
        StoredRefreshToken newStoredRefreshToken = StoredRefreshToken.of(hashNewRefreshToken, userId);
        refreshTokenRepository.save(newStoredRefreshToken);
        String newAccessToken = tokenGenerator.generateAccessToken(UserId.of(userId), user.getRole());
        return RefreshTokenResult.of(newAccessToken, newRefreshToken);
    }
}
