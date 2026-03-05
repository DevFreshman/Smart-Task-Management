package com.github.hoangducmanh.smart_task_management.application.auth.exception;

public class InvalidTokenException extends AuthException{

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable throwable ){
        super(message,throwable);
    };

}
