package com.github.hoangducmanh.smart_task_management.application.task.port.out.event;

import com.github.hoangducmanh.smart_task_management.application.task.dto.event.TaskCreateEvent;
import com.github.hoangducmanh.smart_task_management.application.task.dto.event.TaskDeleteEvent;
import com.github.hoangducmanh.smart_task_management.application.task.dto.event.TaskUpdateEvent;

public interface TaskEventPublisher {
    void publishTaskCreateEvent(TaskCreateEvent event);

    void publishTaskUpdateEvent(TaskUpdateEvent event);

    void publishTaskDeleteEvent(TaskDeleteEvent event);
}
