package com.github.hoangducmanh.smart_task_management.application.auth.port.in;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.RegisterCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.RegisterResult;

public interface RegisterPort {
    RegisterResult execute(RegisterCommand registerCommand);
}
