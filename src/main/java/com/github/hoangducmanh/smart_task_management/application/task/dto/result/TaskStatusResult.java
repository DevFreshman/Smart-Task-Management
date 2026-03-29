package com.github.hoangducmanh.smart_task_management.application.task.dto.result;

import java.util.UUID;

public record TaskStatusResult(
    UUID taskId,
    String status
) {

}
