package com.github.hoangducmanh.smart_task_management.web.auth.dto.response;

public record RefreshResponse(
    String accessToken,
    String refreshToken
) {

}
