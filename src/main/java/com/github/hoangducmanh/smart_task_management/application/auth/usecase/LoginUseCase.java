package com.github.hoangducmanh.smart_task_management.application.auth.usecase;

import com.github.hoangducmanh.smart_task_management.application.ClockSystem;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.LoginCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.LoginResult;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.StoredRefreshToken;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.EmailNotExistsException;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.PasswordMismatchException;
import com.github.hoangducmanh.smart_task_management.application.auth.port.in.LoginPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.password.PasswordHashPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.token.AccessTokenGeneratorPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.token.RefreshTokenGeneratorPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.token.RefreshTokenHashPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.token.RefreshTokenRepository;
import com.github.hoangducmanh.smart_task_management.domain.user.model.Email;
import com.github.hoangducmanh.smart_task_management.domain.user.model.User;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import com.github.hoangducmanh.smart_task_management.domain.user.repository.UserRepository;

public class LoginUseCase implements LoginPort {

    private final RefreshTokenRepository tokenRepository;
    private final PasswordHashPort passwordHashPort;
    private final AccessTokenGeneratorPort accessTokenPort;
    private final RefreshTokenGeneratorPort refreshTokenPort;
    private final RefreshTokenHashPort tokenHashPort;
    private final UserRepository userRepository;
    private final ClockSystem clockSystem;

    public LoginUseCase(RefreshTokenRepository tokenRepository, AccessTokenGeneratorPort accessTokenPort,
                        RefreshTokenGeneratorPort refreshTokenPort, PasswordHashPort passwordHashPort, RefreshTokenHashPort tokenHashPort,
                        UserRepository userRepository, ClockSystem clockSystem) {
        this.tokenRepository = tokenRepository;
        this.accessTokenPort = accessTokenPort;
        this.refreshTokenPort = refreshTokenPort;
        this.passwordHashPort = passwordHashPort;
        this.tokenHashPort = tokenHashPort;
        this.userRepository = userRepository;
        this.clockSystem = clockSystem;
    }

    @Override
    public LoginResult execute(LoginCommand loginCommand) {
        Email email = Email.of(loginCommand.email());
        
        User user = userRepository.findByEmail(email).orElseThrow(
            () -> new EmailNotExistsException("Email not exists"));
        String rawPassword = loginCommand.password();
        boolean matched = passwordHashPort.matches(rawPassword, user.getHashedPassword().value());
        if (!matched) {
            throw new PasswordMismatchException("Password does not match");
        }
        UserId userId = user.getId();
        String refreshToken = refreshTokenPort.generateRefreshToken();
        String hashRefreshToken = tokenHashPort.hash(refreshToken);
        tokenRepository.revokeByUserId(userId.value(), clockSystem.now());
        tokenRepository.save(StoredRefreshToken.of(hashRefreshToken, userId.value()), clockSystem.now());
        String accessToken = accessTokenPort.generateAccessToken(userId, user.getRole());
        return LoginResult.of(accessToken, refreshToken);
    }
}