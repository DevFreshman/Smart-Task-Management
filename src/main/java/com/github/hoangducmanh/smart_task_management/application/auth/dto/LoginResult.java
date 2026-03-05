package com.github.hoangducmanh.smart_task_management.application.auth.dto;

public record LoginResult(String accessToken,String refreshToken) {
    public static LoginResult of(String accessToken, String refreshToken){
        return new LoginResult(accessToken, refreshToken);
    }
}
