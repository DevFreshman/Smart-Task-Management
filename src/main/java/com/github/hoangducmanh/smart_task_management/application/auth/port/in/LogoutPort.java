package com.github.hoangducmanh.smart_task_management.application.auth.port.in;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.LogoutCommand;

public interface LogoutPort {
    void execute(LogoutCommand command);
}
