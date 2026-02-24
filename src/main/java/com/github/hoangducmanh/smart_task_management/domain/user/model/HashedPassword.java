package com.github.hoangducmanh.smart_task_management.domain.user.model;

public record HashedPassword(String value) {
    public HashedPassword {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Hashed password cannot be null or blank");
        }
        value = value.trim();
    }
    public static HashedPassword of(String value) {
        return new HashedPassword(value);
    }
}
