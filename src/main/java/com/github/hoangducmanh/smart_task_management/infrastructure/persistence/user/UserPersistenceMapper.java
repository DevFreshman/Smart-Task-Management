package com.github.hoangducmanh.smart_task_management.infrastructure.persistence.user;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.github.hoangducmanh.smart_task_management.domain.shared.AuditInfo;
import com.github.hoangducmanh.smart_task_management.domain.user.model.Email;
import com.github.hoangducmanh.smart_task_management.domain.user.model.EmailStatus;
import com.github.hoangducmanh.smart_task_management.domain.user.model.HashedPassword;
import com.github.hoangducmanh.smart_task_management.domain.user.model.User;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserRole;

@Component
public class UserPersistenceMapper  {
    public UserEntity toEntity(User user) {
        Objects.requireNonNull(user,"User cannot be null");

        return UserEntity.builder()
                .userId(user.getId().value())
                .name(user.getName())
                .email(user.getEmail().value())
                .emailStatus(user.getEmailStatus().name())
                .hashedPassword(user.getHashedPassword().value())
                .userRole(user.getRole().name())
                .createdAt(user.getAuditInfo().createdAt())
                .updatedAt(user.getAuditInfo().updatedAt())
                .deletedAt(user.getAuditInfo().deletedAt())
                .build();
    }
    public User toDomain(UserEntity entity) {
        Objects.requireNonNull(entity, "UserEntity cannot be null");
     
        try {
            return User.reconstitute( UserId.of(entity.getUserId())
            , Email.of(entity.getEmail()), EmailStatus.fromStatus(entity.getEmailStatus())
            , HashedPassword.of(entity.getHashedPassword())
            , AuditInfo.of(entity.getCreatedAt(), entity.getUpdatedAt(), entity.getDeletedAt())
            , entity.getName(),UserRole.fromRoleName(entity.getUserRole()));
        } catch (RuntimeException e) {
            throw new IllegalStateException("Failed to map UserEntity to User domain model: " + entity.getUserId(), e);
        }
    }
}
