package com.github.hoangducmanh.smart_task_management.application.task.port.in;

import com.github.hoangducmanh.smart_task_management.application.task.dto.command.UnassignTaskCommand;

public interface UnassignTaskPort {
    void execute(UnassignTaskCommand command);
}
