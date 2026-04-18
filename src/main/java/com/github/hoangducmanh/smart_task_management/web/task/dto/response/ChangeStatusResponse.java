package com.github.hoangducmanh.smart_task_management.web.task.dto.response;

import java.util.UUID;

public record ChangeStatusResponse(
    UUID taskId,
    String newStatus
) {

}
