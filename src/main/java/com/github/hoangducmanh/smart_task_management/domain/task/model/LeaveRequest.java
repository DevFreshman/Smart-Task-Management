package com.github.hoangducmanh.smart_task_management.domain.task.model;

import java.time.Instant;
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
    private Instant expiresAt;

    private LeaveRequest(LeaveRequestId id, TaskId taskId, UserId assigneeId, ReasonLeave reason, LeaveRequestStatus status, AuditInfo auditInfo, Instant expiresAt) {
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
    public Instant getExpiresAt() {
        return expiresAt;
    }

    public static LeaveRequest create(LeaveRequestId id, TaskId taskId, UserId assigneeId, ReasonLeave reason, Instant now) {
        AuditInfo auditInfo = AuditInfo.create(now);
        Instant expiresAt = now.plusSeconds(3 * 24 * 60 * 60); // Default expiration time is 3 days
        return new LeaveRequest(id, taskId, assigneeId, reason, LeaveRequestStatus.PENDING, auditInfo, expiresAt);
    }

    public boolean isExpired(Instant now) {
        Objects.requireNonNull(now, "now cannot be null");
        return status == LeaveRequestStatus.PENDING && now.isAfter(expiresAt);
    }

    public void approve(Instant now) {
        ensureStillPending();
        this.status = LeaveRequestStatus.APPROVED;
        this.auditInfo = this.auditInfo.update(now);
    }

    public void reject(Instant now) {
        ensureStillPending();
        this.status = LeaveRequestStatus.REJECTED;
        this.auditInfo = this.auditInfo.update(now);
    }   

    public void expire(Instant now) {
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
