package com.github.hoangducmanh.smart_task_management.application.auth.port.in;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.RequestEmailVerificationCommand;

public interface RequestEmailVerificationPort {
    void execute(RequestEmailVerificationCommand command);
}
