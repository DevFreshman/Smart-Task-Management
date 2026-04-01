package com.github.hoangducmanh.smart_task_management.infrastructure.persistence.task.specification;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.github.hoangducmanh.smart_task_management.application.task.dto.command.TaskFilterCommand;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.task.entity.TaskEntity;

public class TaskSpecification {

    public static Specification<TaskEntity> hasFilter(TaskFilterCommand filter) {
        return Specification.<TaskEntity>unrestricted()
                .and(hasOwnerId(filter.ownerId().value()))
                .and(hasTitle(filter.title().value()))
                .and(hasDescription(filter.description().value()))
                .and(hasStatus(filter.status().getDisplayName()))
                .and(hasPriority(filter.priority().getDisplayName()))
                .and(hasDeadlineFrom(filter.deadlineFrom()))
                .and(hasDeadlineTo(filter.deadlineTo()))
                .and(hasDeleted(filter.includedDeleted()));
    }

    public static Specification<TaskEntity> hasOwnerId(UUID ownerId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("ownerId"), ownerId);
    }

    public static Specification<TaskEntity> hasTitle(String title) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("title"), "%" + title + "%");
    }

    public static Specification<TaskEntity> hasDescription(String description) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("description"), "%" + description + "%");
    }

    public static Specification<TaskEntity> hasStatus(String status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<TaskEntity> hasPriority(String priority) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("priority"), priority);
    }

    public static Specification<TaskEntity> hasDeadlineFrom(LocalDateTime deadlineFrom) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("deadline"), deadlineFrom);
    }

    public static Specification<TaskEntity> hasDeadlineTo(LocalDateTime deadlineTo) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("deadline"), deadlineTo);
    }

    public static Specification<TaskEntity> hasDeleted(boolean includedDeleted) {
        if (includedDeleted) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.isNotNull(root.get("deletedAt"));
        } else {
            return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt"));
        }
    }
}
