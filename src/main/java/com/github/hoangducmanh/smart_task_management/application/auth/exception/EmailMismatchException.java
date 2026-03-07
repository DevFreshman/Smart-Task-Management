package com.github.hoangducmanh.smart_task_management.application.auth.exception;

public class EmailMismatchException extends AuthException {
    public EmailMismatchException(String message) {
        super(message);
    }

    public EmailMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

}
