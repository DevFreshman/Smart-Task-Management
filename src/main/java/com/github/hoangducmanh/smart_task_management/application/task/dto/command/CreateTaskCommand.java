package com.github.hoangducmanh.smart_task_management.application.task.dto.command;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTaskCommand(
    String title,
    String description,
    String priority,
    LocalDateTime deadline,
    UUID ownerId
) {}