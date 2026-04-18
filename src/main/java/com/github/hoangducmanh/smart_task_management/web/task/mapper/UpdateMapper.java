package com.github.hoangducmanh.smart_task_management.web.task.mapper;

import java.util.UUID;

import com.github.hoangducmanh.smart_task_management.application.task.dto.command.ChangeTaskStatusCommand;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskStatusResult;
import com.github.hoangducmanh.smart_task_management.web.task.dto.request.ChangeStatusRequest;
import com.github.hoangducmanh.smart_task_management.web.task.dto.response.ChangeStatusResponse;

public class UpdateMapper {
    public static ChangeTaskStatusCommand toChangeTaskStatusCommand(ChangeStatusRequest request, UUID taskId, UUID userId) {
        return new ChangeTaskStatusCommand(taskId, userId, request.newStatus());
    }

    public static ChangeStatusResponse toChangeStatusResponse(TaskStatusResult result) {
        return new ChangeStatusResponse(result.taskId(), result.status());
    }
}
