package com.github.hoangducmanh.smart_task_management.infrastructure.persistence.task;

import java.util.HashSet;

import org.springframework.stereotype.Component;

import com.github.hoangducmanh.smart_task_management.domain.shared.AuditInfo;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Description;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Task;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskId;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskPriority;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskStatus;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Title;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.task.entity.TaskEntity;

@Component
public class TaskPersistenceMapper {
    public TaskEntity toEntity(Task task) {
        return TaskEntity.builder()
                .taskId(task.getId().value())
                .title(task.getTitle().value())
                .description(task.getDescription().value())
                .deadline(task.getDeadline())
                .taskStatus(task.getStatus().name())
                .ownerId(task.getOwnerId().value())
                .createdAt(task.getAuditInfo().createdAt())
                .updatedAt(task.getAuditInfo().updatedAt())
                .deletedAt(task.getAuditInfo().deletedAt())
                .build();
    }

    // For mapping TaskEntity to Task domain model without assignee information, used in listing and searching tasks where we don't need assignee details.
    public Task toDomainWithoutAssignee(TaskEntity entity) {
        return Task.reconstitute(
            TaskId.of(entity.getTaskId()),
            Title.fromString(entity.getTitle()),
            Description.fromString(entity.getDescription()),
            TaskStatus.fromDisplayName(entity.getTaskStatus()),
            TaskPriority.fromString(entity.getTaskPriority()),
            entity.getDeadline(),
            UserId.of(entity.getOwnerId()),
            new HashSet<>(), // Assignee IDs will be loaded separately in use cases that need them, so we can set it to an empty set here.
            AuditInfo.of(
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
            )
        );
    }

    // For mapping TaskEntity to Task domain model with assignee information, used in use cases that need to manage assignment information.
    public Task toDomainWithAssignees(TaskEntity entity, HashSet<UserId> assigneeIds) {
        return Task.reconstitute(
            TaskId.of(entity.getTaskId()),
            Title.fromString(entity.getTitle()),
            Description.fromString(entity.getDescription()),
            TaskStatus.fromDisplayName(entity.getTaskStatus()),
            TaskPriority.fromString(entity.getTaskPriority()),
            entity.getDeadline(),
            UserId.of(entity.getOwnerId()),
            assigneeIds, // Pass the loaded assignee IDs to the domain model.
            AuditInfo.of(
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
            )
        );
    }
}
