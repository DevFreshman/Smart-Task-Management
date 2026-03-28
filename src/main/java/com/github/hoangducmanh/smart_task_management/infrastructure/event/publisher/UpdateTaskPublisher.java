package com.github.hoangducmanh.smart_task_management.infrastructure.event.publisher;

import org.springframework.context.ApplicationEvent;

public class UpdateTaskPublisher extends ApplicationEvent {
    
    private final String taskId;

    public UpdateTaskPublisher(Object source,String taskId) {
        super(source);
        this.taskId = taskId;
    }

    public String taskId(){
        return taskId;  
    }

}
