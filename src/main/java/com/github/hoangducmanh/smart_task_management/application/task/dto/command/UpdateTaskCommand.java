package com.github.hoangducmanh.smart_task_management.application.task.dto.command;

import java.util.UUID;

public record UpdateTaskCommand(
    UUID requestId,
    UUID taskId,
    String title,
    String description,
    String priority
) {
}
