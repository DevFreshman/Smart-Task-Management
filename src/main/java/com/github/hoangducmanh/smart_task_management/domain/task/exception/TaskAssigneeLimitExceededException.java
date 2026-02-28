package com.github.hoangducmanh.smart_task_management.domain.task.exception;

public class TaskAssigneeLimitExceededException extends TaskDomainException {
    public TaskAssigneeLimitExceededException(String message) {
        super(message);
    }
    public TaskAssigneeLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }

}
