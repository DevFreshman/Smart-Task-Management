package com.github.hoangducmanh.smart_task_management.domain.task.exception;

public class TitleOverLimitException extends TaskDomainException {
    public TitleOverLimitException(String message) {
        super(message);
    }
    public TitleOverLimitException(String message, Throwable cause) {
        super(message, cause);
    }

}
