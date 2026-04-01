package com.github.hoangducmanh.smart_task_management.application.task.dto.command;

import java.time.LocalDateTime;

import com.github.hoangducmanh.smart_task_management.domain.task.model.Description;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskPriority;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskStatus;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Title;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;

public record TaskFilterCommand(
    Title title,
    Description description,
    UserId ownerId, 
    UserId assigneeId,
    TaskStatus status, 
    TaskPriority priority, 
    LocalDateTime deadlineFrom, 
    LocalDateTime deadlineTo,
    boolean includedDeleted) {

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Title title;
            private Description description;
            private UserId ownerId;
            private UserId assigneeId;
            private TaskStatus status;
            private TaskPriority priority;
            private LocalDateTime deadlineFrom;
            private LocalDateTime deadlineTo;
            private boolean includedDeleted;

            public Builder() {
                // Set default values if needed
                this.includedDeleted = false; // By default, do not include deleted tasks
            }

            public Builder title(Title title) {
                this.title = title;
                return this;
            }

            public Builder description(Description description) {
                this.description = description;
                return this;
            }

            public Builder ownerId(UserId ownerId) {
                this.ownerId = ownerId;
                return this;
            }

            public Builder assigneeId(UserId assigneeId) {
                this.assigneeId = assigneeId;
                return this;
            }

            public Builder status(TaskStatus status) {
                this.status = status;
                return this;
            }

            public Builder priority(TaskPriority priority) {
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

            public TaskFilterCommand build() {
                return new TaskFilterCommand(
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
