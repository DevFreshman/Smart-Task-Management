package com.github.hoangducmanh.smart_task_management.domain.task.model;

import java.time.LocalDateTime;

public record TaskFilter(TaskStatus status, TaskPriority priority, LocalDateTime deadlineFrom, LocalDateTime deadlineTo) {

}
