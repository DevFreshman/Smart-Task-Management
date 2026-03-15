package com.github.hoangducmanh.smart_task_management.infrastructure.persistence.refresh;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepositoryCustom {
    Optional<UUID> consumeAndGetUserId(String hashToken, Instant now);
}
