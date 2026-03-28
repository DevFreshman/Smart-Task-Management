package com.github.hoangducmanh.smart_task_management.application.auth.port.in;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.command.RefreshTokenCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.result.RefreshTokenResult;

public interface RefreshTokenPort {
    RefreshTokenResult execute(RefreshTokenCommand command);
}
