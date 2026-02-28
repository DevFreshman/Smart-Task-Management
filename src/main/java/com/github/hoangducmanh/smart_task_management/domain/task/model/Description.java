package com.github.hoangducmanh.smart_task_management.domain.task.model;

import com.github.hoangducmanh.smart_task_management.domain.task.exception.DescriptionOverLimitException;

public record Description(String value) {
    private static final int MAX_LENGTH = 1000;

    public Description {
        if (value != null && value.length() > MAX_LENGTH) {
            throw new DescriptionOverLimitException("Description cannot exceed " + MAX_LENGTH + " characters");
        }
    }

}
