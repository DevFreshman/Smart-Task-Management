package com.github.hoangducmanh.smart_task_management.web.auth.mapper;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.LoginCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.LoginResult;
import com.github.hoangducmanh.smart_task_management.web.auth.dto.request.LoginRequest;
import com.github.hoangducmanh.smart_task_management.web.auth.dto.response.LoginResponse;

public class LoginMapper {
    public static LoginCommand toCommand(LoginRequest request) {
        return LoginCommand.of(request.email(), request.password());
    }
    public static LoginResponse toResponse(LoginResult result) {
        return new LoginResponse(result.accessToken(), result.refreshToken());
    }
}
