package com.github.hoangducmanh.smart_task_management.domain.task.exception;

public class TaskAssigneeAlreadyExistsException extends TaskDomainException {
    public TaskAssigneeAlreadyExistsException(String message) {
        super(message);
    }
    public TaskAssigneeAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

}
