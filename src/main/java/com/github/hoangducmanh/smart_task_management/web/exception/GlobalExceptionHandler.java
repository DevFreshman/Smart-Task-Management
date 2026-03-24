package com.github.hoangducmanh.smart_task_management.web.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.github.hoangducmanh.smart_task_management.application.auth.exception.AuthException;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.ErrorCode;
import com.github.hoangducmanh.smart_task_management.domain.user.exception.UserDomainException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    // Custom exceptions
    @ExceptionHandler(UserDomainException.class)
    public ResponseEntity<ErrorResponse> handleUserDomainException(UserDomainException ex) {
        log.warn("User domain exception: {}", ex.getMessage(), ex);
        return ResponseEntity
        .status(422)
        .body(ErrorResponse.of(422, "USER_BUSINESS_RULE_VIOLATION", "Business rule violated"));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuth(AuthException ex) {
        log.warn("Caused by - code: {}, cause: {}", ex.getErrorCode(), ex.getCause(),ex);
        int statusCode = resolveStatusCode(ex.getErrorCode());
        return ResponseEntity
        .status(statusCode)
        .body(ErrorResponse.of(statusCode, ex.getErrorCode().name(), resolveClientMessage(ex.getErrorCode())));
    }

    private int resolveStatusCode(ErrorCode errorCode) {
        return switch (errorCode) {
            case USER_NOT_FOUND, EMAIL_NOT_EXISTS -> 404;
            case INVALID_TOKEN, TOKEN_DOES_NOT_MATCH, 
            PASSWORD_MISMATCH, EMAIL_MISMATCH -> 401;
            case EMAIL_ALREADY_EXISTS -> 409;
            default -> 500;
        };
    }
    
    private String resolveClientMessage(ErrorCode errorCode) {
        return switch (errorCode) {
            case USER_NOT_FOUND, EMAIL_NOT_EXISTS -> "User not found";
            case INVALID_TOKEN, TOKEN_DOES_NOT_MATCH -> "Token is invalid or expired";
            case PASSWORD_MISMATCH, EMAIL_MISMATCH -> "Email or password is incorrect"; 
            case EMAIL_ALREADY_EXISTS -> "Email already exists";
            default -> "An unexpected error occurred";
        };
    }


    // Database exceptions
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Database exception: {}", ex.getMessage(), ex);
        return ResponseEntity
        .status(409)
        .body(ErrorResponse.of(409, "DATA_INTEGRITY_VIOLATION", "Data conflict"));
    }
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccess(DataAccessException ex) {
        log.error("Data access exception: {}", ex.getMessage(), ex);
        return ResponseEntity
        .status(503)
        .body(ErrorResponse.of(503, "DATABASE_ERROR", "Service temporarily unavailable"));
    }


    // Validation exceptions
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation exception: {}", ex.getMessage(), ex);
        return ResponseEntity
        .badRequest()
        .body(ErrorResponse.of(400, "VALIDATION_ERROR", "Input validation failed"));
    }

    
    // Fallback for unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity
        .status(500)
        .body(ErrorResponse.of(500, "INTERNAL_SERVER_ERROR", "An unexpected error occurred"));
    }
}
