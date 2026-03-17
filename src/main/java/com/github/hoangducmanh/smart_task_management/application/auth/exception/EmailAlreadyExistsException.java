package com.github.hoangducmanh.smart_task_management.application.auth.exception;

public class EmailAlreadyExistsException extends AuthException{
    public EmailAlreadyExistsException(String message){
        super(message, ErrorCode.EMAIL_ALREADY_EXISTS);
    }
    public EmailAlreadyExistsException(String message,Throwable cause){
        super(message, ErrorCode.EMAIL_ALREADY_EXISTS, cause);
    }
}
