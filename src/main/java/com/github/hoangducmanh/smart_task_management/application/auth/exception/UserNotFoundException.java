package com.github.hoangducmanh.smart_task_management.application.auth.exception;

public class UserNotFoundException extends AuthException {
    public UserNotFoundException(String message) {
        super(message, ErrorCode.USER_NOT_FOUND);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, ErrorCode.USER_NOT_FOUND, cause);
    }

}
