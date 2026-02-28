package com.github.hoangducmanh.smart_task_management.domain.task.model;

import com.github.hoangducmanh.smart_task_management.domain.task.exception.TitleOverLimitException;

public record Title(String value) {
    private static final int MAX_LENGTH = 200;
    public Title {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        // We can set a reasonable max length for title, for example 200 characters
        // throwing TitleTooLongException if it exceeds the limit
        if (value.length() > MAX_LENGTH) {
            throw new TitleOverLimitException("Title cannot be longer than " + MAX_LENGTH + " characters");
        }
    }

}
