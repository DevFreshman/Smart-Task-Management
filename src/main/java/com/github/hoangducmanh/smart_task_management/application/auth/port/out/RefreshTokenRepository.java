package com.github.hoangducmanh.smart_task_management.application.auth.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.StoredRefreshToken;

public interface RefreshTokenRepository {
    Optional<UUID> consumeAndGetUserId(String hashRefresh, Instant now);
    StoredRefreshToken save(StoredRefreshToken refreshToken);
    void revokeByUserId(UUID userId, Instant now);
}
