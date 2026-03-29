package com.github.hoangducmanh.smart_task_management.application.task.port.in;

import com.github.hoangducmanh.smart_task_management.application.task.dto.command.ChangeTaskStatusCommand;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskStatusResult;

public interface ChangeTaskStatusPort {
    TaskStatusResult changeTaskStatus(ChangeTaskStatusCommand command);
}
