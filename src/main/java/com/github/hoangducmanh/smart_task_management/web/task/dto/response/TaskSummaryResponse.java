package com.github.hoangducmanh.smart_task_management.web.task.dto.response;

public record TaskSummaryResponse(
    String id,
    String title,
    String description,
    String ownerId,
    String status,
    String priority,
    String deadline
) {
}
