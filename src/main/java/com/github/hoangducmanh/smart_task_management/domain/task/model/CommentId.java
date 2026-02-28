package com.github.hoangducmanh.smart_task_management.domain.task.model;

import java.util.UUID;

public record CommentId(UUID value) {
    public CommentId {
        if (value == null) {
            throw new IllegalArgumentException("Comment ID cannot be null");
        }
    }
    public static CommentId of(UUID value) {
        return new CommentId(value);
    }
    public static CommentId generate() {
        return new CommentId(UUID.randomUUID());
    }
    public static CommentId fromString(String value) {
        if (value==null||value.trim().isEmpty()) {
            throw new IllegalArgumentException("CommentId string value cannot be null or empty");
        }
        try {
            return new CommentId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID string: " + value, e);
        }
    }
}
