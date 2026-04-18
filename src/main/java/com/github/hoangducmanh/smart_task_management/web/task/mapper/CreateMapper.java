package com.github.hoangducmanh.smart_task_management.web.task.mapper;

import java.util.UUID;

import com.github.hoangducmanh.smart_task_management.application.task.dto.command.CreateTaskCommand;
import com.github.hoangducmanh.smart_task_management.web.task.dto.request.CreateTaskRequest;

public class CreateMapper {
    public static CreateTaskCommand toCreateCommand(CreateTaskRequest request, UUID ownerId) {
        return new CreateTaskCommand(
            request.title(),
            request.description(),
            request.priority(),
            request.deadline(),
            ownerId
        );
    }
}
