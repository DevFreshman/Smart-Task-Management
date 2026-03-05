package com.github.hoangducmanh.smart_task_management.application.auth.port.in;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.RefreshTokenCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.RefreshTokenResult;

public interface RefreshTokenPort {
    RefreshTokenResult execute(RefreshTokenCommand command);
}
