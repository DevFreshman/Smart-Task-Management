package com.github.hoangducmanh.smart_task_management.application.task.usecase;

import com.github.hoangducmanh.smart_task_management.application.task.dto.query.TaskFilterQuery;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.PageResult;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskSummaryResult;
import com.github.hoangducmanh.smart_task_management.application.task.port.in.GetListTaskByFilterPort;
import com.github.hoangducmanh.smart_task_management.application.task.port.out.TaskQueryServicePort;

public class GetListTaskByTaskUseCase implements GetListTaskByFilterPort {

    private final TaskQueryServicePort taskQueryRepository;

    public GetListTaskByTaskUseCase(TaskQueryServicePort taskQueryRepository) {
        this.taskQueryRepository = taskQueryRepository;
    }

    @Override
    public PageResult<TaskSummaryResult> execute(TaskFilterQuery filter, int page, int size) {
        return taskQueryRepository.findTasksByFilter(filter, page, size);
    }
}
