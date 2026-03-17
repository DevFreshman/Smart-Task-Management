package com.github.hoangducmanh.smart_task_management.web.auth.mapper;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.RegisterCommand;
import com.github.hoangducmanh.smart_task_management.web.auth.dto.request.RegisterRequest;

public class RegisterMapper {
    public static RegisterCommand toCommand(RegisterRequest request) {
        return RegisterCommand.of(request.email(), request.password(), request.name());
    }
}
