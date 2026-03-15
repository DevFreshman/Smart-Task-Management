package com.github.hoangducmanh.smart_task_management.infrastructure.persistence.refresh;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, String>, RefreshTokenRepositoryCustom {
    
    @Modifying
    @Query("UPDATE RefreshTokenEntity t SET t.revokedAt = :now WHERE t.userId = :userId AND t.revokedAt IS NULL")
    public void revokeByUserId(@Param("userId") UUID userId, @Param("now") Instant now);
}
