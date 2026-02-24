package com.github.hoangducmanh.smart_task_management.domain.user.exception;

public class InvalidUserNameException extends UserDomainException {
    public InvalidUserNameException(String message) {
        super(message);
    }
    public InvalidUserNameException(String message, Throwable cause) {
        super(message, cause);
    }

}
