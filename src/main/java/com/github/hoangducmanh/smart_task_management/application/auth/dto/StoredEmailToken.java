package com.github.hoangducmanh.smart_task_management.application.auth.dto;

import java.util.UUID;

public record StoredEmailToken(UUID userId, String email, String hashedToken) {
    public static StoredEmailToken of(UUID userId, String email, String hashedToken){
        return new StoredEmailToken(userId, email, hashedToken);
    }
}
