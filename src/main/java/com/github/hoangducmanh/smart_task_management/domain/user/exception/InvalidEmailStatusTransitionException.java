package com.github.hoangducmanh.smart_task_management.domain.user.exception;

public class InvalidEmailStatusTransitionException extends UserDomainException {
    public InvalidEmailStatusTransitionException(String message) {
        super(message);
    }
    public InvalidEmailStatusTransitionException(String message, Throwable cause) {
        super(message, cause);
    }

}
