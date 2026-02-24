package com.github.hoangducmanh.smart_task_management.domain.user.exception;

public abstract class UserDomainException extends RuntimeException {
    public UserDomainException(String message) {
        super(message);
    }
    public UserDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
