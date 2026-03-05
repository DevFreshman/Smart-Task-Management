package com.github.hoangducmanh.smart_task_management.application.auth.port.out;

import com.github.hoangducmanh.smart_task_management.domain.user.model.Role;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;

public interface TokenGeneratorPort {
    String generateRefreshToken();
    String generateAccessToken(UserId userId, Role role);
}
