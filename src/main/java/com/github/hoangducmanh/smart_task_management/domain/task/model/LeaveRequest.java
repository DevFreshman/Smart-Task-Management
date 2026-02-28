package com.github.hoangducmanh.smart_task_management.domain.task.model;

import java.time.LocalDateTime;
import java.util.Objects;

import com.github.hoangducmanh.smart_task_management.domain.shared.AuditInfo;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.LeaveRequestStatusTransitionException;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;

public class LeaveRequest {
    private final LeaveRequestId id;
    private final TaskId taskId;
    private final UserId assigneeId;
    private final ReasonLeave reason;
    private LeaveRequestStatus status;
    private AuditInfo auditInfo;
    private LocalDateTime expiresAt;

    private LeaveRequest(LeaveRequestId id, TaskId taskId, UserId assigneeId, ReasonLeave reason, LeaveRequestStatus status, AuditInfo auditInfo, LocalDateTime expiresAt) {
        this.id =Objects.requireNonNull(id, "LeaveRequest ID cannot be null");
        this.taskId = Objects.requireNonNull(taskId, "Task ID cannot be null");
        this.assigneeId = Objects.requireNonNull(assigneeId, "Assignee ID cannot be null");
        this.reason = Objects.requireNonNull(reason, "Reason for leave cannot be null");
        this.status = Objects.requireNonNull(status, "Initial status cannot be null");
        this.auditInfo = Objects.requireNonNull(auditInfo, "Audit info cannot be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expires at cannot be null");
    }

    public LeaveRequestId getId() {
        return id;
    }
    public TaskId getTaskId() {
        return taskId;
    }
    public UserId getAssigneeId() {
        return assigneeId;
    }
    public ReasonLeave getReason() {
        return reason;
    }
    public LeaveRequestStatus getStatus() {
        return status;
    }
    public AuditInfo getAuditInfo() {
        return auditInfo;
    }
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public static LeaveRequest create(LeaveRequestId id, TaskId taskId, UserId assigneeId, ReasonLeave reason, LocalDateTime now) {
        AuditInfo auditInfo = AuditInfo.create(now);
        LocalDateTime expiresAt = now.plusDays(3); // Default expiration time is 3 days
        return new LeaveRequest(id, taskId, assigneeId, reason, LeaveRequestStatus.PENDING, auditInfo, expiresAt);
    }

    public boolean isExpired(LocalDateTime now) {
        return status == LeaveRequestStatus.PENDING && now.isAfter(expiresAt);
    }

    public void approve(LocalDateTime now) {
        ensureStillPending();
        this.status = LeaveRequestStatus.APPROVED;
        this.auditInfo = this.auditInfo.update(now);
    }

    public void reject(LocalDateTime now) {
        ensureStillPending();
        this.status = LeaveRequestStatus.REJECTED;
        this.auditInfo = this.auditInfo.update(now);
    }   

    public void expire(LocalDateTime now) {
        ensureStillPending();
        this.status = LeaveRequestStatus.EXPIRED;
        this.auditInfo = this.auditInfo.update(now);
    }   

    private void ensureStillPending() {
        if (this.status != LeaveRequestStatus.PENDING) {
            throw new LeaveRequestStatusTransitionException(
                "Cannot change status, request is already " + this.status
            );
        }   
    }
}
