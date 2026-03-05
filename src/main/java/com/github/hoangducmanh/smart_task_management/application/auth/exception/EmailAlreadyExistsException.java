package com.github.hoangducmanh.smart_task_management.application.auth.exception;

public class EmailAlreadyExistsException extends AuthException{
    public EmailAlreadyExistsException(String message){
        super(message);
    }

    public EmailAlreadyExistsException(String message,Throwable cause){
        super(message, cause);
    }
}
