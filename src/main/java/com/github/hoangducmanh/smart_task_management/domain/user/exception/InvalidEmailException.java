package com.github.hoangducmanh.smart_task_management.domain.user.exception;

public class InvalidEmailException extends UserDomainException {
    public InvalidEmailException(String message) {
        super(message);
    }
    public InvalidEmailException(String message, Throwable cause) {
        super(message, cause);
    }

}
