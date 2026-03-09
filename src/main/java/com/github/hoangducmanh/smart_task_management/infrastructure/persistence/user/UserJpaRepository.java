package com.github.hoangducmanh.smart_task_management.infrastructure.persistence.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByUserIdAndDeletedAtIsNull(UUID id);
    Optional<UserEntity> findByEmailAndDeletedAtIsNull(String email);
    boolean existsByEmailAndDeletedAtIsNull(String email);
    boolean existsByUserIdAndDeletedAtIsNull(UUID id);
}
