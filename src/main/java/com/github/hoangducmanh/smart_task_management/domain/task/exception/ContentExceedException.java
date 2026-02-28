package com.github.hoangducmanh.smart_task_management.domain.task.exception;

public class ContentExceedException extends TaskDomainException {
    public ContentExceedException(String message) {
        super(message);
    }
    public ContentExceedException(String message, Throwable cause) {
        super(message, cause);
    }

}
