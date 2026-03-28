package com.github.hoangducmanh.smart_task_management.application.auth.port.in;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.command.RegisterCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.result.RegisterResult;

public interface RegisterPort {
    RegisterResult execute(RegisterCommand registerCommand);
}
