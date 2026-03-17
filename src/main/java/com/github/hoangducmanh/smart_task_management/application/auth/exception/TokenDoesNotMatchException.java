package com.github.hoangducmanh.smart_task_management.application.auth.exception;

public class TokenDoesNotMatchException extends AuthException {
    public TokenDoesNotMatchException(String message) {
        super(message, ErrorCode.TOKEN_DOES_NOT_MATCH);
    }

    public TokenDoesNotMatchException(String message, Throwable cause) {
        super(message, ErrorCode.TOKEN_DOES_NOT_MATCH, cause);
    }

}
