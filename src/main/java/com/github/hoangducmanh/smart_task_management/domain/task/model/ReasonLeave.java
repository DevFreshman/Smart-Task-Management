package com.github.hoangducmanh.smart_task_management.domain.task.model;

import com.github.hoangducmanh.smart_task_management.domain.task.exception.ContentExceedException;

public record ReasonLeave(String reason) {
    private static final int MAX_REASON_LENGTH = 500;
    public ReasonLeave {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason for leave cannot be null or empty");
        }
        if (reason.length() > MAX_REASON_LENGTH) {
            throw new ContentExceedException("Reason for leave cannot exceed " + MAX_REASON_LENGTH + " characters");
        }
    }
}
