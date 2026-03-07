package com.github.hoangducmanh.smart_task_management.application.auth.usecase;

import com.github.hoangducmanh.smart_task_management.application.ClockSystem;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.LogoutCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.port.in.LogoutPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.token.RefreshTokenRepository;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;

public class LogoutUseCase implements LogoutPort {
    private final RefreshTokenRepository refreshTokenRepository;
    private final ClockSystem clockSystem;
    public LogoutUseCase(RefreshTokenRepository refreshTokenRepository, ClockSystem clockSystem){
        this.clockSystem = clockSystem;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public void execute(LogoutCommand command) {
        UserId userId = UserId.of(command.userId());
        refreshTokenRepository.revokeByUserId(userId.value(), clockSystem.now());
    }
}