package com.github.hoangducmanh.smart_task_management.domain.task.exception;

public class TaskOwnerNotCanBeAssigneeException extends TaskDomainException {
    public TaskOwnerNotCanBeAssigneeException(String message) {
        super(message);
    }
    public TaskOwnerNotCanBeAssigneeException(String message, Throwable cause) {
        super(message, cause);
    }

}
