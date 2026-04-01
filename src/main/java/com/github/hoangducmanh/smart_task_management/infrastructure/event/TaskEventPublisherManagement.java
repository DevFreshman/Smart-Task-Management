package com.github.hoangducmanh.smart_task_management.infrastructure.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.github.hoangducmanh.smart_task_management.application.task.dto.event.TaskCreateEvent;
import com.github.hoangducmanh.smart_task_management.application.task.dto.event.TaskDeleteEvent;
import com.github.hoangducmanh.smart_task_management.application.task.dto.event.TaskUpdateEvent;
import com.github.hoangducmanh.smart_task_management.application.task.port.out.event.TaskEventPublisher;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class TaskEventPublisherManagement implements TaskEventPublisher {
    
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishTaskCreateEvent(TaskCreateEvent event) {
        if (event == null) {
            return;
        }
        eventPublisher.publishEvent(event);
    }

    @Override
    public void publishTaskUpdateEvent(TaskUpdateEvent event) {
        if (event == null) {
            return;
        }
        eventPublisher.publishEvent(event);
    }

    @Override
    public void publishTaskDeleteEvent(TaskDeleteEvent event) {
        if (event == null) {
            return;
        }
        eventPublisher.publishEvent(event);
    }

}
