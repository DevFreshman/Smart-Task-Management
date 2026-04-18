package com.github.hoangducmanh.smart_task_management.application.task.dto.query;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskFilterQuery(
    String title,
    String description,
    UUID ownerId, 
    UUID assigneeId,
    String status, 
    String priority, 
    LocalDateTime deadlineFrom, 
    LocalDateTime deadlineTo,
    boolean includedDeleted) {

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String title;
            private String description;
            private UUID ownerId;
            private UUID assigneeId;
            private String status;
            private String priority;
            private LocalDateTime deadlineFrom;
            private LocalDateTime deadlineTo;
            private boolean includedDeleted;

            public Builder() {
                // Set default values if needed
                this.includedDeleted = false; // By default, do not include deleted tasks
            }

            public Builder title(String title) {
                this.title = title;
                return this;
            }

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public Builder ownerId(UUID ownerId) {
                this.ownerId = ownerId;
                return this;
            }

            public Builder assigneeId(UUID assigneeId) {
                this.assigneeId = assigneeId;
                return this;
            }

            public Builder status(String status) {
                this.status = status;
                return this;
            }

            public Builder priority(String priority) {
                this.priority = priority;
                return this;
            }

            public Builder deadlineFrom(LocalDateTime deadlineFrom) {
                this.deadlineFrom = deadlineFrom;
                return this;
            }

            public Builder deadlineTo(LocalDateTime deadlineTo) {
                this.deadlineTo = deadlineTo;
                return this;
            }

            public Builder includedDeleted(boolean includedDeleted) {
                this.includedDeleted = includedDeleted;
                return this;
            }

            public TaskFilterQuery build() {
                return new TaskFilterQuery(
                    title, 
                    description, 
                    ownerId, 
                    assigneeId, 
                    status, 
                    priority, 
                    deadlineFrom, 
                    deadlineTo, 
                    includedDeleted
                );
            }
        }
}
