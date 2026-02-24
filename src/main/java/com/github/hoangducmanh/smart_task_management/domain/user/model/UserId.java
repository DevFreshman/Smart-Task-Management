package com.github.hoangducmanh.smart_task_management.domain.user.model;

import java.util.UUID;

public final record UserId(UUID value) {
    public UserId {
        if (value == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        if(value.equals(new UUID(0, 0))) {
            throw new IllegalArgumentException("UserId cannot be empty");
        }
    }
    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }
    public static UserId fromString(String value) {
        if (value==null||value.trim().isEmpty()) {
            throw new IllegalArgumentException("UserId string value cannot be null or empty");
        }
        try {
            return new UserId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID string: " + value, e);
        }
    }
    public String asString() {
        return value.toString();
    }
    @Override
    public String toString() {
        return "UserId=[" + value + "]";
    }
}
