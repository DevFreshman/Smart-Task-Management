package com.github.hoangducmanh.smart_task_management.application.task.dto.result;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.hoangducmanh.smart_task_management.domain.task.model.Task;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskPriority;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskStatus;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;

public record TaskResult(
    UUID taskId,
    String title,
    String description,
    TaskStatus status,
    TaskPriority priority,
    LocalDateTime deadline,
    UUID ownerId,
    Set<UUID> assigneeIds,
    Instant createdAt,
    Instant updatedAt,
    boolean isDeleted
) {
    public static TaskResult from(Task task) {
        return new TaskResult(
            task.getId().value(),
            task.getTitle().value(),
            task.getDescription() != null ? task.getDescription().value() : null,
            task.getStatus(),
            task.getPriority(),
            task.getDeadline(),
            task.getOwnerId().value(),
            task.getAssigneeIds().stream()
                .map(UserId::value)
                .collect(Collectors.toSet()),
            task.getAuditInfo().createdAt(),
            task.getAuditInfo().updatedAt(),
            task.getAuditInfo().isDeleted()
        );
    }
}