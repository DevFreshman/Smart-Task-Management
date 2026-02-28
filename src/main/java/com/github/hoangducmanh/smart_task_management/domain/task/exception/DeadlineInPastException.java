package com.github.hoangducmanh.smart_task_management.domain.task.exception;

public class DeadlineInPastException extends TaskDomainException {
    public DeadlineInPastException(String message) {
        super(message);
    }
    public DeadlineInPastException(String message, Throwable cause) {
        super(message, cause);
    }

}
