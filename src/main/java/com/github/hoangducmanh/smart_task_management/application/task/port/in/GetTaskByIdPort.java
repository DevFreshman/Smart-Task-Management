package com.github.hoangducmanh.smart_task_management.application.task.port.in;

import java.util.UUID;

import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskSummaryResult;

public interface GetTaskByIdPort {
    TaskSummaryResult execute(UUID taskId);
}
