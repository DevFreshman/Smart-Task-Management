package com.github.hoangducmanh.smart_task_management.application.task.dto.result;

public record TaskSummaryResult(
    String id,
    String title,
    String description,
    String ownerId,
    String status,
    String priority,
    String deadline
) {

}
