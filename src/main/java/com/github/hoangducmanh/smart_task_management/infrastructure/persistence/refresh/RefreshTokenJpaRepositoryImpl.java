package com.github.hoangducmanh.smart_task_management.infrastructure.persistence.refresh;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RefreshTokenJpaRepositoryImpl implements RefreshTokenRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<UUID> consumeAndGetUserId(String hashToken, java.time.Instant now) {
        List<UUID> result = jdbcTemplate.query("""
            UPDATE refresh_tokens
            SET revoked_at = ?
            WHERE token_hash = ?
              AND revoked_at IS NULL
            RETURNING user_id
        """,
        (rs, rowNum) -> rs.getObject("user_id", UUID.class),
        Timestamp.from(now), hashToken);

        return result.stream().findFirst();
    }

}
