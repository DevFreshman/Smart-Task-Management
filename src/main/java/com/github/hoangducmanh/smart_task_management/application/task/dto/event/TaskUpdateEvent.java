package com.github.hoangducmanh.smart_task_management.application.task.dto.event;

import java.util.UUID;

public record TaskUpdateEvent(UUID taskId) {
}