package com.github.hoangducmanh.smart_task_management.application.task.port.in;

import com.github.hoangducmanh.smart_task_management.application.task.dto.command.UpdateTaskCommand;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskResult;

// This interface defines the contract for updating a task. It will be implemented by the UpdateTaskUseCase class.
// It can include methods for updating various simple attributes of a task, such as title, description, priority, deadline.
public interface UpdateTaskPort {
    TaskResult execute(UpdateTaskCommand updateTaskCommand);
}
