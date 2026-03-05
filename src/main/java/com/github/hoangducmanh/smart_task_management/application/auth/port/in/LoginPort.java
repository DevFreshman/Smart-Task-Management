package com.github.hoangducmanh.smart_task_management.application.auth.port.in;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.LoginCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.LoginResult;

public interface LoginPort {
    LoginResult execute(LoginCommand loginCommand);
}
