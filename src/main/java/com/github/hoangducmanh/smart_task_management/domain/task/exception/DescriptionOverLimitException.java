package com.github.hoangducmanh.smart_task_management.domain.task.exception;

public class DescriptionOverLimitException extends TaskDomainException {
    public DescriptionOverLimitException(String message) {
        super(message);
    }
    public DescriptionOverLimitException(String message, Throwable cause) {
        super(message, cause);
    }

}
