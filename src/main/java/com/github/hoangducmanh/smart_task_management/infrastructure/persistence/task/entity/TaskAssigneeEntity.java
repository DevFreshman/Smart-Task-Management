package com.github.hoangducmanh.smart_task_management.infrastructure.persistence.task.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Entity
@Getter
@Table(name = "assignee_task")
public class TaskAssigneeEntity {

    @EmbeddedId
    private TaskAssigneeId id;

}
