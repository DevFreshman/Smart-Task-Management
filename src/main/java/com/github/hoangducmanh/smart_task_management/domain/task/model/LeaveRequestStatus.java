package com.github.hoangducmanh.smart_task_management.domain.task.model;

public enum LeaveRequestStatus {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    EXPIRED("Expired");

    private final String displayName; 

    LeaveRequestStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
