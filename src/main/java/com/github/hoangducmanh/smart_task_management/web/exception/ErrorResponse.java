package com.github.hoangducmanh.smart_task_management.web.exception;

import java.time.Instant;

public record ErrorResponse(
    int status,
    String errorCode,
    String message,
    Instant timestamp
) {
    public static ErrorResponse of(int status, String errorCode, String message) {
        return new ErrorResponse(status, errorCode, message, Instant.now());
    }
}
