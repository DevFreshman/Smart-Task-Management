package com.github.hoangducmanh.smart_task_management.application.task.exception;

public class TaskOwnershipException extends TaskApplicationException {

    public TaskOwnershipException(String message) {
        super(message, TaskErrorCode.TASK_OWNERSHIP_MISMATCH);
    }

}
