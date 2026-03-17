package com.github.hoangducmanh.smart_task_management.application.auth.exception;

public class EmailMismatchException extends AuthException {
    public EmailMismatchException(String message) {
        super(message, ErrorCode.EMAIL_MISMATCH);
    }

    public EmailMismatchException(String message, Throwable cause) {
        super(message, ErrorCode.EMAIL_MISMATCH, cause);
    }

}
