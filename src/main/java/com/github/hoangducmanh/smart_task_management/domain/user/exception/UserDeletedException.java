package com.github.hoangducmanh.smart_task_management.domain.user.exception;

public class UserDeletedException extends UserDomainException {
    public UserDeletedException(String message) {
        super(message);
    }
    public UserDeletedException(String message, Throwable cause) {
        super(message, cause);
    }

}
