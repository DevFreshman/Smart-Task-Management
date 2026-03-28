package com.github.hoangducmanh.smart_task_management.application.task.port.out.event;

import java.util.UUID;

public interface DeleteTaskEvent {
    void publishTaskDeleteEvent(UUID taskId);
}
