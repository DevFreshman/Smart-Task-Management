package com.github.hoangducmanh.smart_task_management.domain.task.exception;

public class TaskDeleteException extends TaskDomainException {
    public TaskDeleteException(String message) {
        super(message);
    }
    public TaskDeleteException(String message, Throwable cause) {
        super(message, cause);
    }

}
