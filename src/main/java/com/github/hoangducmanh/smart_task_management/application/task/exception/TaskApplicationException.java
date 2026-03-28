package com.github.hoangducmanh.smart_task_management.application.task.exception;

public class TaskApplicationException extends RuntimeException {
    private final TaskErrorCode errorCode;

    public TaskApplicationException(String message, TaskErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public TaskApplicationException(String message, TaskErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public TaskErrorCode getErrorCode() {
        return errorCode;
    }
}
