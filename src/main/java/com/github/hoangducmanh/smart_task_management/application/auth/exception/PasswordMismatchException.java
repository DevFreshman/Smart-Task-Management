package com.github.hoangducmanh.smart_task_management.application.auth.exception;

public class PasswordMismatchException extends AuthException{
    public PasswordMismatchException(String message){
        super(message);
    }

    public PasswordMismatchException(String message,Throwable cause){
        super(message, cause);
    }
}
