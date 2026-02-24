package com.github.hoangducmanh.smart_task_management.domain.shared;

import java.time.LocalDateTime;
import java.util.Objects;

public record AuditInfo(LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
    public AuditInfo{
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt cannot be null");
        }
        if (updatedAt == null|| updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("updatedAt cannot be null or before createdAt");
        }
        if(deletedAt != null && (deletedAt.isBefore(createdAt) || deletedAt.isBefore(updatedAt))) {
            throw new IllegalArgumentException("deletedAt cannot be before createdAt or updatedAt");
        }
    }
    public static AuditInfo create(LocalDateTime now) {
        Objects.requireNonNull(now, "now cannot be null");
        return new AuditInfo(now, now, null);
    }
    public AuditInfo update(LocalDateTime now) {
        Objects.requireNonNull(now, "now cannot be null");
        return new AuditInfo(this.createdAt, now, this.deletedAt);
    }
    public AuditInfo delete(LocalDateTime now) {
        Objects.requireNonNull(now, "now cannot be null");
        return new AuditInfo(this.createdAt, this.updatedAt, now);
    }
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
