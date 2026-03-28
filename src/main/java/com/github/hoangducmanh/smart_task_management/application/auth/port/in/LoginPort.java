package com.github.hoangducmanh.smart_task_management.application.auth.port.in;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.command.LoginCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.result.LoginResult;

public interface LoginPort {
    LoginResult execute(LoginCommand loginCommand);
}
