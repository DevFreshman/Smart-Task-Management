package com.github.hoangducmanh.smart_task_management.application.task.port.out;

import java.util.UUID;

import com.github.hoangducmanh.smart_task_management.application.task.dto.command.TaskFilterCommand;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.PageResult;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskSummaryResult;

public interface TaskQueryServicePort {
    PageResult<TaskSummaryResult> findTasksByFilter(TaskFilterCommand filter, int page, int size);
    TaskSummaryResult findTaskById(UUID taskId);
}
