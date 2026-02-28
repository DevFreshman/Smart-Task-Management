package com.github.hoangducmanh.smart_task_management.domain.task.exception;

public class TaskAssigneeNotExistsException extends TaskDomainException {
    public TaskAssigneeNotExistsException(String message) {
        super(message);
    }
    public TaskAssigneeNotExistsException(String message, Throwable cause) {
        super(message, cause);
    }

}
