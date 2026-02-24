package com.github.hoangducmanh.smart_task_management.domain.user.exception;

public class InvalidRoleException extends UserDomainException {
    public InvalidRoleException(String message) {
        super(message);
    }
    public InvalidRoleException(String message, Throwable cause) {
        super(message, cause);
    }

}
