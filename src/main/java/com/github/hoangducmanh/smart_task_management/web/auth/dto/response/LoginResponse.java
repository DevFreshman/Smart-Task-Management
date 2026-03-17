package com.github.hoangducmanh.smart_task_management.web.auth.dto.response;

public record LoginResponse(
    String accessToken,
    String refreshToken
) {

}
