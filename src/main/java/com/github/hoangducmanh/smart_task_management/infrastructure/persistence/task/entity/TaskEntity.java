package com.github.hoangducmanh.smart_task_management.infrastructure.persistence.task.entity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder
@Getter
@Entity
@Table(name = "tasks")
public class TaskEntity {

    @Id
    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "task_status", nullable = false)
    private String taskStatus;

    @Column(name = "task_priority", nullable = false)
    private String taskPriority;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

}
