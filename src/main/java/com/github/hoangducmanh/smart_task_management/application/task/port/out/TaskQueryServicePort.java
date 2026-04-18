package com.github.hoangducmanh.smart_task_management.application.task.port.out;

import java.util.UUID;

import com.github.hoangducmanh.smart_task_management.application.task.dto.query.TaskFilterQuery;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.PageResult;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskSummaryResult;

public interface TaskQueryServicePort {
    PageResult<TaskSummaryResult> findTasksByFilter(TaskFilterQuery filter, int page, int size);
    TaskSummaryResult findTaskById(UUID taskId);
}
