package com.github.hoangducmanh.smart_task_management.application.auth.usecase;

import java.util.UUID;

import com.github.hoangducmanh.smart_task_management.application.ClockSystem;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.LogoutCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.port.in.LogoutPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.RefreshTokenRepository;

public class LogoutUseCase implements LogoutPort {
    private final RefreshTokenRepository refreshTokenRepository;
    private final ClockSystem clockSystem;
    public LogoutUseCase(RefreshTokenRepository refreshTokenRepository, ClockSystem clockSystem){
        this.clockSystem = clockSystem;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public void execute(LogoutCommand command) {
        UUID userId = command.userId();
        refreshTokenRepository.revokeByUserId(userId, clockSystem.now());
    }
}