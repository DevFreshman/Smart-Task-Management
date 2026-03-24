package com.github.hoangducmanh.smart_task_management.infrastructure.persistence.refresh;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.StoredRefreshToken;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.token.RefreshTokenRepository;



@Repository
public class RefreshTokenAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    private final long refreshTokenExpirationMs;

    public RefreshTokenAdapter(
        RefreshTokenJpaRepository refreshTokenJpaRepository,
        @Value("${REFRESH_TOKEN_EXPIRATION_MS}") long refreshTokenExpirationMs
    ) {
        this.refreshTokenJpaRepository = refreshTokenJpaRepository;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Override
    public Optional<UUID> consumeAndGetUserId(String hashRefresh, Instant now) {
        return refreshTokenJpaRepository.consumeAndGetUserId(hashRefresh, now);
    }

    @Override
    public void save(StoredRefreshToken refreshToken, Instant now) {
        
        RefreshTokenEntity entity = new RefreshTokenEntity(
            refreshToken.hashToken(),
            refreshToken.userId(),
            now,
            now.plusMillis(refreshTokenExpirationMs),
            null
        );

        refreshTokenJpaRepository.save(entity);
    }

    @Override
    public void revokeByUserId(UUID userId, Instant now) {
        refreshTokenJpaRepository.revokeByUserId(userId, now);
    }

}
