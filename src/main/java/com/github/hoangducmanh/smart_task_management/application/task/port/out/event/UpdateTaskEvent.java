package com.github.hoangducmanh.smart_task_management.application.task.port.out.event;

import java.util.UUID;

public interface UpdateTaskEvent {
    void publishTaskUpdateEvent(UUID taskId);
}
