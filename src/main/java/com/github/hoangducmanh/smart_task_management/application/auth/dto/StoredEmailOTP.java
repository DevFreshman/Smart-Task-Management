package com.github.hoangducmanh.smart_task_management.application.auth.dto;

import java.util.UUID;

public record StoredEmailOTP(UUID userId, String email, String hashedOTP) {
    public static StoredEmailOTP of(UUID userId, String email, String hashedOTP){
        return new StoredEmailOTP(userId, email, hashedOTP);
    }
}
