package com.github.hoangducmanh.smart_task_management.application.auth.port.in;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.command.RequestEmailVerificationCommand;

public interface RequestEmailVerificationPort {
    void execute(RequestEmailVerificationCommand command);
}
