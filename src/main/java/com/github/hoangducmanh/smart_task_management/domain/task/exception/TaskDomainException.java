package com.github.hoangducmanh.smart_task_management.domain.task.exception;

public class TaskDomainException extends RuntimeException {
    public TaskDomainException(String message) {
        super(message);
    }
    public TaskDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
