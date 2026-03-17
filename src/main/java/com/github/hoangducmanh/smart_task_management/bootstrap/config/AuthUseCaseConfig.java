package com.github.hoangducmanh.smart_task_management.bootstrap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.hoangducmanh.smart_task_management.application.auth.port.in.LoginPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.token.AccessTokenGeneratorPort;
import com.github.hoangducmanh.smart_task_management.application.auth.usecase.LoginUseCase;
import com.github.hoangducmanh.smart_task_management.application.auth.usecase.LogoutUseCase;
import com.github.hoangducmanh.smart_task_management.application.auth.usecase.RefreshTokenUseCase;
import com.github.hoangducmanh.smart_task_management.application.auth.usecase.RegisterUseCase;
import com.github.hoangducmanh.smart_task_management.infrastructure.clock.ClockSystemImpl;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.refresh.JpaRefreshTokenAdapter;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.user.JpaUserPersistenceAdapter;
import com.github.hoangducmanh.smart_task_management.infrastructure.security.service.JwtService;
import com.github.hoangducmanh.smart_task_management.infrastructure.security.service.PasswordService;
import com.github.hoangducmanh.smart_task_management.infrastructure.security.service.RefreshService;

@Configuration
public class AuthUseCaseConfig {

    @Bean
    public LoginPort loginUseCase(
        JpaRefreshTokenAdapter refreshTokenRepository,
        JwtService jwtService,
        RefreshService refreshService,
        PasswordService passwordService,
        JpaUserPersistenceAdapter userRepository,
        ClockSystemImpl clockSystem
    ) {
        return new LoginUseCase(refreshTokenRepository,
             jwtService,
            refreshService,
             passwordService,
             refreshService,
              userRepository,
               clockSystem);
    }

    @Bean
    public LogoutUseCase logoutUseCase(
        JpaRefreshTokenAdapter refreshTokenRepository,
        ClockSystemImpl clockSystem
    ) {
        return new LogoutUseCase(
            refreshTokenRepository,
             clockSystem
        );
    }

    @Bean
    public RefreshTokenUseCase refreshTokenUseCase(
            AccessTokenGeneratorPort accessTokenGenerator,
            RefreshService refreshService,
            JpaRefreshTokenAdapter refreshTokenRepository,
            JpaUserPersistenceAdapter userRepository,
            ClockSystemImpl clockSystem
    ) {
        return new RefreshTokenUseCase(
            refreshTokenRepository,
            refreshService,
            accessTokenGenerator,
            refreshService,
            userRepository,
            clockSystem
        );
    }

    @Bean
    public RegisterUseCase registerUseCase(
            PasswordService passwordService,
            JpaUserPersistenceAdapter userRepository,
            ClockSystemImpl clockSystem
    ) {
        return new RegisterUseCase(
            userRepository,
            passwordService,
            clockSystem
        );
    }
}
