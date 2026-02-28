package com.github.hoangducmanh.smart_task_management.domain.task.exception;

public class TaskStatusTransitionException extends TaskDomainException {
    public TaskStatusTransitionException(String message) {
        super(message);
    }
    public TaskStatusTransitionException(String message, Throwable cause) {
        super(message, cause);
    }

}
