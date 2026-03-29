package com.github.hoangducmanh.smart_task_management.application.task.dto.command;

import java.util.UUID;

public record UnassignTaskCommand(
    UUID requestId,
    UUID taskId,
    UUID assigneeId
) {

}
