package com.github.hoangducmanh.smart_task_management.domain.shared;

import java.time.Instant;
import java.util.Objects;

public record AuditInfo(Instant createdAt, Instant updatedAt, Instant deletedAt) {
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
    public static AuditInfo create(Instant now) {
        Objects.requireNonNull(now, "now cannot be null");
        return new AuditInfo(now, now, null);
    }
    public AuditInfo update(Instant now) {
        Objects.requireNonNull(now, "now cannot be null");
        return new AuditInfo(this.createdAt, now, this.deletedAt);
    }
    public AuditInfo delete(Instant now) {
        Objects.requireNonNull(now, "now cannot be null");
        return new AuditInfo(this.createdAt, this.updatedAt, now);
    }
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
