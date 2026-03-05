package com.github.hoangducmanh.smart_task_management.application.auth.dto;

public record RefreshTokenResult(String accessToken, String refreshToken) {
    public static RefreshTokenResult of(String accessToken, String refreshToken){
        return new RefreshTokenResult(accessToken, refreshToken);
    }
}
