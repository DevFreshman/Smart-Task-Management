package com.github.hoangducmanh.smart_task_management.application.auth.exception;

public class EmailNotExistsException extends AuthException{

    public EmailNotExistsException(String message) {
        super(message);
    }

    public EmailNotExistsException(String message, Throwable throwable ){
        super(message,throwable);
    };
}
