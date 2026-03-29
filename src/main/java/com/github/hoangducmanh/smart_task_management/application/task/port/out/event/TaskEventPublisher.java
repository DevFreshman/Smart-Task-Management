package com.github.hoangducmanh.smart_task_management.application.task.port.out.event;

import java.util.UUID;

public interface TaskEventPublisher {
    void publishTaskCreateEvent();
    void publishTaskUpdateEvent(UUID taskId, String title, String description, String priority);
    void publishTaskStatusChangeEvent(UUID taskId, String newStatus);
    void publishTaskDeleteEvent(UUID taskId);
    void publishTaskUnassignEvent(UUID taskId, UUID assigneeId);
}
