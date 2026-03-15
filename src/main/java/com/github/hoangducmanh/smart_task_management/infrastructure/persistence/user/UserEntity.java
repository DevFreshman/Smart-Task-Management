package com.github.hoangducmanh.smart_task_management.infrastructure.persistence.user;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Builder
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "name")
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "email_status", nullable = false)
    private String emailStatus;

    @Column(name = "hashed_password", nullable = false)
    private String hashedPassword;

    @Column(name = "role", nullable = false)
    private String userRole;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
