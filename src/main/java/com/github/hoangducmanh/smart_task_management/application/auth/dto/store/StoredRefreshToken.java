package com.github.hoangducmanh.smart_task_management.application.auth.dto.store;

import java.util.UUID;

public record StoredRefreshToken(String hashToken, UUID userId) {
    public static StoredRefreshToken of(String hashToken, UUID userId){
        return new StoredRefreshToken(hashToken, userId);
    }
}
