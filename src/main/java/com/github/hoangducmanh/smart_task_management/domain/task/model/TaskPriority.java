package com.github.hoangducmanh.smart_task_management.domain.task.model;

public enum TaskPriority {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical");
    
    private final String displayName;
    
    TaskPriority(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }   
}
