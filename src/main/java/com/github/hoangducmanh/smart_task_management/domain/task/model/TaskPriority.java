package com.github.hoangducmanh.smart_task_management.domain.task.model;

import com.github.hoangducmanh.smart_task_management.domain.task.exception.InvalidTaskPriorityException;

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
        return displayName.toUpperCase();
    }

    public static TaskPriority fromString(String priority) {
        for (TaskPriority p : TaskPriority.values()) {
            if (p.displayName.equalsIgnoreCase(priority)) {
                return p;
            }
        }
        throw new InvalidTaskPriorityException("Invalid task priority: " + priority);
    }
}
