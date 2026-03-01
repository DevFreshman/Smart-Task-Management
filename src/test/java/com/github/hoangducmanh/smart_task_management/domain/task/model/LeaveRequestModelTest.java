package com.github.hoangducmanh.smart_task_management.domain.task.model;

import com.github.hoangducmanh.smart_task_management.domain.task.exception.LeaveRequestStatusTransitionException;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeaveRequestModelTest {

    // BR: New leave request starts as pending and expires after 3 days.
    @Test
    void create_shouldSetPendingAndDefaultExpiryAfterThreeDays() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 9, 0);

        LeaveRequest request = LeaveRequest.create(
            LeaveRequestId.fromString("de84f4f3-c5af-4a57-b62f-1ddd12c537d3"),
            TaskId.fromString("5f4ad13f-f5be-4c2f-9f7a-86f0d6108b6e"),
            UserId.fromString("8f322f74-2b68-41b4-8d52-f286d2c497f1"),
            new ReasonLeave("Need a day off for family matters"),
            now
        );

        assertEquals(LeaveRequestStatus.PENDING, request.getStatus());
        assertEquals(now.plusDays(3), request.getExpiresAt());
        assertEquals(now, request.getAuditInfo().createdAt());
        assertEquals(now, request.getAuditInfo().updatedAt());
    }

    // BR: Expiry depends on pending status and current time.
    @Test
    void isExpired_shouldReturnTrueOnlyWhenPendingAndNowAfterExpiresAt() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 9, 0);
        LeaveRequest request = createPendingRequest(now);

        assertEquals(false, request.isExpired(now.plusDays(2)));
        assertEquals(false, request.isExpired(now.plusDays(3)));
        assertEquals(true, request.isExpired(now.plusDays(3).plusSeconds(1)));
    }

    // BR: Approve transitions status and updates audit.
    @Test
    void approve_shouldChangeStatusAndUpdateAudit() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 9, 0);
        LeaveRequest request = createPendingRequest(now);
        LocalDateTime approvedAt = now.plusHours(4);

        request.approve(approvedAt);

        assertEquals(LeaveRequestStatus.APPROVED, request.getStatus());
        assertEquals(approvedAt, request.getAuditInfo().updatedAt());
        assertTrue(!request.isExpired(now.plusDays(5)));
    }

    // BR: Reject transitions status and updates audit.
    @Test
    void reject_shouldChangeStatusAndUpdateAudit() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 9, 0);
        LeaveRequest request = createPendingRequest(now);
        LocalDateTime rejectedAt = now.plusHours(2);

        request.reject(rejectedAt);

        assertEquals(LeaveRequestStatus.REJECTED, request.getStatus());
        assertEquals(rejectedAt, request.getAuditInfo().updatedAt());
    }

    // BR: Expire transitions status and updates audit.
    @Test
    void expire_shouldChangeStatusAndUpdateAudit() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 9, 0);
        LeaveRequest request = createPendingRequest(now);
        LocalDateTime expiredAt = now.plusDays(4);

        request.expire(expiredAt);

        assertEquals(LeaveRequestStatus.EXPIRED, request.getStatus());
        assertEquals(expiredAt, request.getAuditInfo().updatedAt());
    }

    // BR: Only pending request can change status.
    @Test
    void statusTransition_shouldThrowWhenRequestIsNotPending() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 9, 0);
        LeaveRequest approved = createPendingRequest(now);
        approved.approve(now.plusMinutes(1));

        assertThrows(LeaveRequestStatusTransitionException.class, () -> approved.approve(now.plusMinutes(2)));
        assertThrows(LeaveRequestStatusTransitionException.class, () -> approved.reject(now.plusMinutes(2)));
        assertThrows(LeaveRequestStatusTransitionException.class, () -> approved.expire(now.plusMinutes(2)));

        LeaveRequest rejected = createPendingRequest(now);
        rejected.reject(now.plusMinutes(1));
        assertThrows(LeaveRequestStatusTransitionException.class, () -> rejected.approve(now.plusMinutes(2)));
    }

    private LeaveRequest createPendingRequest(LocalDateTime now) {
        return LeaveRequest.create(
            LeaveRequestId.fromString("de84f4f3-c5af-4a57-b62f-1ddd12c537d3"),
            TaskId.fromString("5f4ad13f-f5be-4c2f-9f7a-86f0d6108b6e"),
            UserId.fromString("8f322f74-2b68-41b4-8d52-f286d2c497f1"),
            new ReasonLeave("Need a day off for family matters"),
            now
        );
    }
}
