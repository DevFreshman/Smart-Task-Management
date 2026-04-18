package com.github.hoangducmanh.smart_task_management.application.task.port.in;

import com.github.hoangducmanh.smart_task_management.application.task.dto.command.DeleteTaskCommand;

public interface DeleteTaskPort {
    void execute(DeleteTaskCommand command);
}
