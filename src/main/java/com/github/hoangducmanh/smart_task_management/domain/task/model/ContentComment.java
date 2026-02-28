package com.github.hoangducmanh.smart_task_management.domain.task.model;

import com.github.hoangducmanh.smart_task_management.domain.task.exception.ContentExceedException;

public record ContentComment(String content) {
    private static final int MAX_CONTENT_LENGTH = 1000;
    public ContentComment {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be null or empty");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new ContentExceedException("Comment content cannot exceed " + MAX_CONTENT_LENGTH + " characters");
        }
    }
}
