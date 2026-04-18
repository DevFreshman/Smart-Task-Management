package com.github.hoangducmanh.smart_task_management.web.task.mapper;

import com.github.hoangducmanh.smart_task_management.application.task.dto.query.TaskFilterQuery;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.PageResult;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskSummaryResult;
import com.github.hoangducmanh.smart_task_management.web.task.dto.request.TaskFilterRequest;
import com.github.hoangducmanh.smart_task_management.web.task.dto.response.PageResponse;
import com.github.hoangducmanh.smart_task_management.web.task.dto.response.TaskSummaryResponse;

public class QueryMapper {
    public static TaskSummaryResponse toResponse(TaskSummaryResult taskSummary) {
        return new TaskSummaryResponse(
            taskSummary.id(),
            taskSummary.title(),
            taskSummary.description(),
            taskSummary.ownerId(),
            taskSummary.status(),
            taskSummary.priority(),
            taskSummary.deadline()
        );
    }

    public static TaskSummaryResult toResult(TaskSummaryResponse taskSummary) {
        return new TaskSummaryResult(
            taskSummary.id(),
            taskSummary.title(),
            taskSummary.description(),
            taskSummary.ownerId(),
            taskSummary.status(),
            taskSummary.priority(),
            taskSummary.deadline()
        );
    }


    public static PageResponse<TaskSummaryResponse> toPageResponse(PageResult<TaskSummaryResult> taskPage) {
        return new PageResponse<>(
            taskPage.items().stream().map(QueryMapper::toResponse).toList(),
            taskPage.totalItems(),
            taskPage.totalPages(),
            taskPage.currentPage(),
            taskPage.pageSize()
        );
    }

   public static TaskFilterQuery toFilterQuery(TaskFilterRequest filterQuery) {
        return TaskFilterQuery.builder()
            .title(filterQuery.title())
            .description(filterQuery.description())
            .ownerId(filterQuery.ownerId())
            .assigneeId(filterQuery.assigneeId())
            .status(filterQuery.status())
            .priority(filterQuery.priority())
            .deadlineFrom(filterQuery.deadlineFrom())
            .deadlineTo(filterQuery.deadlineTo())
            .includedDeleted(filterQuery.includedDeleted())
            .build();
    }
}
