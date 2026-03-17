package com.github.hoangducmanh.smart_task_management.infrastructure.persistence.refresh;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.StoredRefreshToken;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.token.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Repository
public class JpaRefreshTokenAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;
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
            now.minusMillis(Long.parseLong(System.getenv("REFRESH_TOKEN_EXPIRATION_MS"))),
            null
        );

        refreshTokenJpaRepository.save(entity);
    }

    @Override
    public void revokeByUserId(UUID userId, Instant now) {
        refreshTokenJpaRepository.revokeByUserId(userId, now);
    }

}
