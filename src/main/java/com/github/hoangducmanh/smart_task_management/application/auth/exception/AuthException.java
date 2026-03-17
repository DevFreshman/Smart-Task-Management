package com.github.hoangducmanh.smart_task_management.application.auth.exception;

public class AuthException extends RuntimeException{
    private final ErrorCode errorCode;

    public AuthException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AuthException(String message, ErrorCode errorCode, Throwable cause){
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
