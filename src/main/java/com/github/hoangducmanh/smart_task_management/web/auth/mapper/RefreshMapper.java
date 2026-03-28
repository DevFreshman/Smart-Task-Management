package com.github.hoangducmanh.smart_task_management.web.auth.mapper;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.command.RefreshTokenCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.result.RefreshTokenResult;
import com.github.hoangducmanh.smart_task_management.web.auth.dto.request.RefreshRequest;
import com.github.hoangducmanh.smart_task_management.web.auth.dto.response.RefreshResponse;

public class RefreshMapper {
    public static RefreshResponse toResponse(RefreshTokenResult result) {
        return new RefreshResponse(result.accessToken(), result.refreshToken());
    }

    public static RefreshTokenCommand toCommand(RefreshRequest request) {
        return RefreshTokenCommand.of(request.refreshToken());
    }
}
