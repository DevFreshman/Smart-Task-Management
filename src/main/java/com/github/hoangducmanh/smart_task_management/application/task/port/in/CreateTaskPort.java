package com.github.hoangducmanh.smart_task_management.application.task.port.in;

import com.github.hoangducmanh.smart_task_management.application.task.dto.command.CreateTaskCommand;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskResult;

public interface CreateTaskPort {
    TaskResult execute(CreateTaskCommand createTaskCommand);
}
