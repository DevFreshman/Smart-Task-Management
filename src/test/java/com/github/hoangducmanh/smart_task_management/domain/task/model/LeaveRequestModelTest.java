package com.github.hoangducmanh.smart_task_management.domain.task.model;

import com.github.hoangducmanh.smart_task_management.domain.task.exception.LeaveRequestStatusTransitionException;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeaveRequestModelTest {

    // BR: New leave request starts as pending and expires after 3 days.
    @Test
    void create_shouldSetPendingAndDefaultExpiryAfterThreeDays() {
        Instant now = Instant.parse("2026-02-10T09:00:00Z");

        LeaveRequest request = LeaveRequest.create(
            LeaveRequestId.fromString("de84f4f3-c5af-4a57-b62f-1ddd12c537d3"),
            TaskId.fromString("5f4ad13f-f5be-4c2f-9f7a-86f0d6108b6e"),
            UserId.fromString("8f322f74-2b68-41b4-8d52-f286d2c497f1"),
            new ReasonLeave("Need a day off for family matters"),
            now
        );

        assertEquals(LeaveRequestStatus.PENDING, request.getStatus());
        assertEquals(now.plus(3, ChronoUnit.DAYS), request.getExpiresAt());
        assertEquals(now, request.getAuditInfo().createdAt());
        assertEquals(now, request.getAuditInfo().updatedAt());
    }

    // BR: Expiry depends on pending status and current time.
    @Test
    void isExpired_shouldReturnTrueOnlyWhenPendingAndNowAfterExpiresAt() {
        Instant now = Instant.parse("2026-02-10T09:00:00Z");
        LeaveRequest request = createPendingRequest(now);

        assertEquals(false, request.isExpired(now.plus(2, ChronoUnit.DAYS)));
        assertEquals(false, request.isExpired(now.plus(3, ChronoUnit.DAYS)));
        assertEquals(true, request.isExpired(now.plus(3, ChronoUnit.DAYS).plusSeconds(1)));
    }

    // BR: Approve transitions status and updates audit.
    @Test
    void approve_shouldChangeStatusAndUpdateAudit() {
        Instant now = Instant.parse("2026-02-10T09:00:00Z");
        LeaveRequest request = createPendingRequest(now);
        Instant approvedAt = now.plus(4, ChronoUnit.HOURS);

        request.approve(approvedAt);

        assertEquals(LeaveRequestStatus.APPROVED, request.getStatus());
        assertEquals(approvedAt, request.getAuditInfo().updatedAt());
        assertTrue(!request.isExpired(now.plus(5, ChronoUnit.DAYS)));
    }

    // BR: Reject transitions status and updates audit.
    @Test
    void reject_shouldChangeStatusAndUpdateAudit() {
        Instant now = Instant.parse("2026-02-10T09:00:00Z");
        LeaveRequest request = createPendingRequest(now);
        Instant rejectedAt = now.plus(2, ChronoUnit.HOURS);

        request.reject(rejectedAt);

        assertEquals(LeaveRequestStatus.REJECTED, request.getStatus());
        assertEquals(rejectedAt, request.getAuditInfo().updatedAt());
    }

    // BR: Expire transitions status and updates audit.
    @Test
    void expire_shouldChangeStatusAndUpdateAudit() {
        Instant now = Instant.parse("2026-02-10T09:00:00Z");
        LeaveRequest request = createPendingRequest(now);
        Instant expiredAt = now.plus(4, ChronoUnit.DAYS);

        request.expire(expiredAt);

        assertEquals(LeaveRequestStatus.EXPIRED, request.getStatus());
        assertEquals(expiredAt, request.getAuditInfo().updatedAt());
    }

    // BR: Only pending request can change status.
    @Test
    void statusTransition_shouldThrowWhenRequestIsNotPending() {
        Instant now = Instant.parse("2026-02-10T09:00:00Z");
        LeaveRequest approved = createPendingRequest(now);
        approved.approve(now.plus(1, ChronoUnit.MINUTES));

        assertThrows(LeaveRequestStatusTransitionException.class, () -> approved.approve(now.plus(2, ChronoUnit.MINUTES)));
        assertThrows(LeaveRequestStatusTransitionException.class, () -> approved.reject(now.plus(2, ChronoUnit.MINUTES)));
        assertThrows(LeaveRequestStatusTransitionException.class, () -> approved.expire(now.plus(2, ChronoUnit.MINUTES)));

        LeaveRequest rejected = createPendingRequest(now);
        rejected.reject(now.plus(1, ChronoUnit.MINUTES));
        assertThrows(LeaveRequestStatusTransitionException.class, () -> rejected.approve(now.plus(2, ChronoUnit.MINUTES)));
    }

    private LeaveRequest createPendingRequest(Instant now) {
        return LeaveRequest.create(
            LeaveRequestId.fromString("de84f4f3-c5af-4a57-b62f-1ddd12c537d3"),
            TaskId.fromString("5f4ad13f-f5be-4c2f-9f7a-86f0d6108b6e"),
            UserId.fromString("8f322f74-2b68-41b4-8d52-f286d2c497f1"),
            new ReasonLeave("Need a day off for family matters"),
            now
        );
    }
}
