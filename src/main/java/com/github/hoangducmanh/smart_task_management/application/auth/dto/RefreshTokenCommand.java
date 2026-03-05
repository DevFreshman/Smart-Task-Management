package com.github.hoangducmanh.smart_task_management.application.auth.dto;

public record RefreshTokenCommand(String refreshToken) {
    public static RefreshTokenCommand of(String refreshToken){
        return new RefreshTokenCommand(refreshToken);
    }
}
