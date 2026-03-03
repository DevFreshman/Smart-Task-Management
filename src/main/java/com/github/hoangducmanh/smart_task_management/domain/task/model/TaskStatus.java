package com.github.hoangducmanh.smart_task_management.domain.task.model;

import com.github.hoangducmanh.smart_task_management.domain.task.exception.TaskStatusTransitionException;

public enum TaskStatus {
    TODO("To Do"){
        @Override
        public TaskStatus updateStatus(TaskStatus newStatus) {
            if(newStatus == IN_PROGRESS || newStatus == CANCELLED){
                return newStatus;
            }
            throw new TaskStatusTransitionException("Invalid status transition from TODO to " + newStatus);
        }
    },
    IN_PROGRESS("In Progress"){
        @Override
        public TaskStatus updateStatus(TaskStatus newStatus) {
            if(newStatus == COMPLETED || newStatus == CANCELLED) {
                return newStatus;
            }
            throw new TaskStatusTransitionException("Invalid status transition from IN_PROGRESS to " + newStatus);
        }
    },
    COMPLETED("Completed"){
        @Override
        public TaskStatus updateStatus(TaskStatus newStatus) {
            if(newStatus != COMPLETED) {
                throw new TaskStatusTransitionException("Task is already completed. Cannot transition to " + newStatus);
            }
            if(newStatus == CANCELLED){
                return newStatus;
            }
            return this;
        }
    },
    CANCELLED("Cancelled"){
        @Override
        public TaskStatus updateStatus(TaskStatus newStatus) {
            if(newStatus == CANCELLED) {
                throw new TaskStatusTransitionException("Task is already Cancelled. Cannot transition to " + newStatus);
            }
            if (newStatus == TODO) {
                return newStatus;
            }
            throw new TaskStatusTransitionException("The task status changes to To Do only when reopening a cancelled task");
        }
    };

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }

    public static TaskStatus fromDisplayName(String displayName) {
        for (TaskStatus status : TaskStatus.values()) {
            if (status.displayName.equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No TaskStatus with display name: " + displayName);
    }
    public abstract TaskStatus updateStatus(TaskStatus newStatus);
}
