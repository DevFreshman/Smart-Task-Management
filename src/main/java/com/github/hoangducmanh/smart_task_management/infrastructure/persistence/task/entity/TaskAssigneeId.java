package com.github.hoangducmanh.smart_task_management.infrastructure.persistence.task.entity;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@Builder
public class TaskAssigneeId implements Serializable{

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "assignee_id", nullable = false)
    private UUID assigneeId;
}
