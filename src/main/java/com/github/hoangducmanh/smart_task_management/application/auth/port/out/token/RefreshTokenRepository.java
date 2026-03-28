package com.github.hoangducmanh.smart_task_management.application.auth.port.out.token;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.store.StoredRefreshToken;

public interface RefreshTokenRepository {
    Optional<UUID> consumeAndGetUserId(String hashRefresh, Instant now);
    void save(StoredRefreshToken refreshToken, Instant now);
    void revokeByUserId(UUID userId, Instant now);
}
