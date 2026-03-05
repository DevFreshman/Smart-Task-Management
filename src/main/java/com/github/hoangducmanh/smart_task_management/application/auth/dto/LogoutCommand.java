package com.github.hoangducmanh.smart_task_management.application.auth.dto;

import java.util.UUID;

public record LogoutCommand(UUID userId) {
    public static LogoutCommand of(UUID userId){
        return new LogoutCommand(userId);
    }
}
