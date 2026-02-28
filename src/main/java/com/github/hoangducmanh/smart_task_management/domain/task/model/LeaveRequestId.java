package com.github.hoangducmanh.smart_task_management.domain.task.model;

import java.util.UUID;

public record LeaveRequestId(UUID value) {
    public LeaveRequestId {
        if (value == null) {
            throw new IllegalArgumentException("LeaveRequest ID cannot be null");
        }
    }
    public static LeaveRequestId of(UUID value) {
        return new LeaveRequestId(value);
    }
    public static LeaveRequestId generate() {
        return new LeaveRequestId(UUID.randomUUID());
    }
    public static LeaveRequestId fromString(String value) {
        if (value==null||value.trim().isEmpty()) {
            throw new IllegalArgumentException("LeaveRequestId string value cannot be null or empty");
        }
        try {
            return new LeaveRequestId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID string: " + value, e);
        }
    }

}
