package com.github.hoangducmanh.smart_task_management.application.auth.usecase;

import com.github.hoangducmanh.smart_task_management.application.TimeProvider;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.command.LogoutCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.port.in.LogoutPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.token.RefreshTokenRepository;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;

public class LogoutUseCase implements LogoutPort {
    private final RefreshTokenRepository refreshTokenRepository;
    private final TimeProvider clockSystem;
    public LogoutUseCase(RefreshTokenRepository refreshTokenRepository, TimeProvider clockSystem){
        this.clockSystem = clockSystem;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public void execute(LogoutCommand command) {
        UserId userId = UserId.of(command.userId());
        refreshTokenRepository.revokeByUserId(userId.value(), clockSystem.now());
    }
}