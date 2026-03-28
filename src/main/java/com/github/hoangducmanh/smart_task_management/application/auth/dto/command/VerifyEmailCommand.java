package com.github.hoangducmanh.smart_task_management.application.auth.dto.command;

import java.util.UUID;

public record VerifyEmailCommand(UUID userId, String email, String token) {
    public static VerifyEmailCommand of(UUID userId, String email, String rawToken){
        return new VerifyEmailCommand(userId, email, rawToken);
    }

}
