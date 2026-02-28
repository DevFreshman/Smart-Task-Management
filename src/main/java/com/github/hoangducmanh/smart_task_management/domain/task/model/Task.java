package com.github.hoangducmanh.smart_task_management.domain.task.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.github.hoangducmanh.smart_task_management.domain.shared.AuditInfo;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.DeadlineInPastException;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.TaskAssigneeAlreadyExistsException;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.TaskAssigneeLimitExceededException;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.TaskAssigneeNotExistsException;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.TaskDeleteException;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.TaskOwnerNotCanBeAssigneeException;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;

public class Task {
    private final TaskId id;                // Unique identifier for the task
    private Title title;                 // Title of the task
    private Description description;     // Description of the task
    private TaskStatus status;           // Status of the task (e.g., TODO, IN_PROGRESS, COMPLETED)
    private TaskPriority priority;       // Priority of the task (e.g., LOW, MEDIUM, HIGH, CRITICAL)
    private LocalDateTime deadline;       // Deadline for the task, can be null if no deadline
    private final UserId ownerId;          // ID of the user who owns the task
    private HashSet<UserId> assigneeIds;       // IDs of users assigned to the task
    private AuditInfo auditInfo;            // Audit information (createdAt, updatedAt, createdBy, updatedBy)

    private Task(TaskId id, Title title, Description description, TaskStatus status, TaskPriority priority, LocalDateTime deadline, UserId ownerId, HashSet<UserId> assigneeIds, AuditInfo auditInfo) {
        this.id = Objects.requireNonNull(id, "Task ID cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.description = description; // Description can be null or empty, but we can allow it to be updated later
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.priority = Objects.requireNonNull(priority, "Priority cannot be null");
        this.deadline = deadline; // Deadline can be null, but we can allow it to be updated later
        this.ownerId = Objects.requireNonNull(ownerId, "Owner ID cannot be null");
        this.assigneeIds = assigneeIds; // Assignee IDs can be empty, but we can allow it to be updated later
        this.auditInfo = Objects.requireNonNull(auditInfo, "Audit info cannot be null");
    }

    public TaskId getId() {
            return id;
        }
    public Title getTitle() {
            return title;
        }
    public Description getDescription() {
            return description;
        }
    public TaskStatus getStatus() {
            return status;
        }
    public TaskPriority getPriority() {
            return priority;
        }
    public LocalDateTime getDeadline() {
            return deadline;
        }
    public UserId getOwnerId() {
            return ownerId;
        }
    public Set<UserId> getAssigneeIds() {
            return Set.copyOf(assigneeIds);
        }
    public AuditInfo getAuditInfo() {
            return auditInfo;
        }
    
    private void ensureNotDeleted() {
    if (auditInfo.isDeleted()) throw new TaskDeleteException("Task is deleted");
  }
    
    public static Task create(TaskId id, Title title, Description description, TaskPriority priority, LocalDateTime deadline, UserId ownerId, LocalDateTime createdAt, LocalDateTime now) {
        if(deadline != null && deadline.isBefore(now)) {
            throw new DeadlineInPastException("Deadline cannot be in the past"); // throws DeadlineInPastException
        }
        return new Task(id, title, description, TaskStatus.TODO, priority, deadline, ownerId, new HashSet<>(), AuditInfo.create(createdAt));
    }

    // update information of task, except status and assignees
    public void update(Title title, Description description, TaskPriority priority, LocalDateTime deadline, LocalDateTime now) {
        ensureNotDeleted();
        if(deadline != null && deadline.isBefore(now)) {
            throw new DeadlineInPastException("Deadline cannot be in the past"); // throws DeadlineInPastException
        }
        if(title != null) {
            this.title = title;
        }
        if(description != null) {
            this.description = description;
        }
        if(priority != null) {
            this.priority = priority;
        }
        if(deadline != null) {
            this.deadline = deadline;
        }
        this.auditInfo = this.auditInfo.update(now);
    }

    // change status of task, but only allow valid status transition
    public void changeStatus(TaskStatus newStatus, LocalDateTime now) {
        Objects.requireNonNull(newStatus, "New status cannot be null");
        ensureNotDeleted();
        this.status = this.status.updateStatus(newStatus); // throws TaskStatusTransitionException if invalid transition
        this.auditInfo = this.auditInfo.update(now);
    }
    public void assignToUsers(UserId newAssigneeId, LocalDateTime now) {
        Objects.requireNonNull(newAssigneeId, "New assignee ID cannot be null");
        ensureNotDeleted();
        if(newAssigneeId.equals(ownerId)) {
            throw new TaskOwnerNotCanBeAssigneeException("Task owner cannot be an assignee"); // throws TaskOwnerNotCanBeAssigneeException
        }
        if(assigneeIds.contains(newAssigneeId)) {
            throw new TaskAssigneeAlreadyExistsException("User is already an assignee of this task");
        }
        if((assigneeIds.size() >= 10)) {
            throw new TaskAssigneeLimitExceededException("Task cannot have more than 10 assignees");
        }
        this.assigneeIds.add(newAssigneeId);
        this.auditInfo = this.auditInfo.update(now);
    }
    public void removeAssignee(UserId assigneeId, LocalDateTime now) {
        Objects.requireNonNull(assigneeId, "Assignee ID cannot be null");
        ensureNotDeleted();
        if(!assigneeIds.contains(assigneeId)) {
            throw new TaskAssigneeNotExistsException("User is not an assignee of this task");
        }
        this.assigneeIds.remove(assigneeId);
        this.auditInfo = this.auditInfo.update(now);
    }
}