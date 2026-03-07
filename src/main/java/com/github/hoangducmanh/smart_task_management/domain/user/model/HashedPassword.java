package com.github.hoangducmanh.smart_task_management.domain.user.model;

import com.github.hoangducmanh.smart_task_management.domain.user.exception.InvalidHashedPasswordException;

public record HashedPassword(String value) {
    public HashedPassword {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidHashedPasswordException("Hashed password cannot be null or blank");
        }
        value = value.trim();
    }
    public static HashedPassword of(String value) {
        return new HashedPassword(value);
    }
}
