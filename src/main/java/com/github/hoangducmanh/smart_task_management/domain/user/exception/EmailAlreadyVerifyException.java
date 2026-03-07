package com.github.hoangducmanh.smart_task_management.domain.user.exception;

public class EmailAlreadyVerifyException extends UserDomainException {
    public EmailAlreadyVerifyException(String message) {
        super(message);
    }

    public EmailAlreadyVerifyException(String message, Throwable cause) {
        super(message, cause);
    }
}
