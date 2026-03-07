package com.github.hoangducmanh.smart_task_management.application.auth.port.in;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.VerifyEmailCommand;

public interface VerifyEmailPort {
    void execute(VerifyEmailCommand command);
}
