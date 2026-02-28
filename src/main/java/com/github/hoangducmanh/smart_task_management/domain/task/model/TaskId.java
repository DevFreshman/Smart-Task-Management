package com.github.hoangducmanh.smart_task_management.domain.task.model;

import java.util.UUID;

public record TaskId(UUID value) {
    public TaskId {
        if (value == null) {
            throw new IllegalArgumentException("TaskId cannot be null");
        }
    }
    public static TaskId generate() {
        return new TaskId(UUID.randomUUID());
    }
    //If we want to create TaskId from string, we can add a static factory method
    public static TaskId fromString(String value) {
        if (value==null||value.trim().isEmpty()) {
            throw new IllegalArgumentException("TaskId string value cannot be null or empty");
        }
        try {
            return new TaskId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID string: " + value, e);
        }
    }
    public String asString() {
        return value.toString();
    }
    @Override
    public String toString() {
        return "TaskId=[" + value + "]";
    }
}
