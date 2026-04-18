package com.github.hoangducmanh.smart_task_management.web.task.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record TaskFilterRequest(
    String title,
    String description,
    UUID ownerId, 
    UUID assigneeId,
    @NotBlank String status, 
    @NotBlank String priority, 
    LocalDateTime deadlineFrom, 
    LocalDateTime deadlineTo,
    boolean includedDeleted,
    int page,
    int size
) {
    public TaskFilterRequest {
        status = status != null ? status.toUpperCase().trim() : null;
        priority = priority != null ? priority.toUpperCase().trim() : null;
    }
}
