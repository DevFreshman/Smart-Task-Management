package com.github.hoangducmanh.smart_task_management.domain.user.exception;

public class InvalidHashedPasswordException extends UserDomainException {
    public InvalidHashedPasswordException(String message) {
        super(message);
    }
    public InvalidHashedPasswordException(String message, Throwable cause) {
        super(message, cause);
    }

}
