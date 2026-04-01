package com.github.hoangducmanh.smart_task_management.application.task.port.in;

import com.github.hoangducmanh.smart_task_management.application.task.dto.command.TaskFilterCommand;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.PageResult;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskSummaryResult;

public interface GetListTaskByFilterPort {
    PageResult<TaskSummaryResult> execute(TaskFilterCommand filter, int page, int size);
}
