package com.github.hoangducmanh.smart_task_management.domain.task.model;

import com.github.hoangducmanh.smart_task_management.domain.task.exception.DeadlineInPastException;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.TaskAssigneeAlreadyExistsException;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.TaskAssigneeLimitExceededException;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.TaskAssigneeNotExistsException;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.TaskOwnerNotCanBeAssigneeException;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.TaskStatusTransitionException;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskModelTest {

    // BR: Task creation sets default business state.
    @Test
    void create_shouldInitializeDefaultFields() {
        Instant now = Instant.parse("2026-02-10T08:05:00Z");
        LocalDateTime deadline = LocalDateTime.ofInstant(now, ZoneOffset.UTC).plusDays(3);

        Task task = Task.create(
            TaskId.fromString("09d2cbde-458d-4d66-8d64-5e4cb904f2dc"),
            new Title("Implement task domain"),
            new Description("Description"),
            TaskPriority.HIGH,
            deadline,
            ownerId(),
            now
        );

        assertEquals(TaskStatus.TODO, task.getStatus());
        assertEquals(TaskPriority.HIGH, task.getPriority());
        assertEquals(now, task.getAuditInfo().createdAt());
        assertEquals(now, task.getAuditInfo().updatedAt());
        assertEquals(0, task.getAssigneeIds().size());
    }

    // BR: Task cannot be created with deadline in the past.
    @Test
    void create_shouldThrowWhenDeadlineIsInPastComparedToNow() {
        Instant now = Instant.parse("2026-02-10T08:00:00Z");
        LocalDateTime pastDeadline = LocalDateTime.ofInstant(now, ZoneOffset.UTC).minusMinutes(1);

        assertThrows(
            DeadlineInPastException.class,
            () -> Task.create(
                TaskId.fromString("09d2cbde-458d-4d66-8d64-5e4cb904f2dc"),
                new Title("Implement task domain"),
                new Description("Description"),
                TaskPriority.HIGH,
                pastDeadline,
                ownerId(),
                now
            )
        );
    }

    // BR: Update modifies only provided fields.
    @Test
    void update_shouldChangeOnlyNonNullFields() {
        Instant now = Instant.parse("2026-02-10T08:00:00Z");
        Task task = createTask(now);

        task.update(new Title("New title"), null, TaskPriority.CRITICAL, now.plus(1, ChronoUnit.MINUTES));

        assertEquals("New title", task.getTitle().value());
        assertEquals("Initial description", task.getDescription().value());
        assertEquals(TaskPriority.CRITICAL, task.getPriority());
    }

    // BR: Deadline change supports set/remove and updates audit.
    @Test
    void changeDeadline_shouldUpdateAndAllowRemovingDeadline() {
        Instant now = Instant.parse("2026-02-10T08:00:00Z");
        Task task = createTask(now);
        Instant changedAt = now.plus(2, ChronoUnit.HOURS);
        LocalDateTime newDeadline = LocalDateTime.ofInstant(now, ZoneOffset.UTC).plusDays(7);

        task.changeDeadline(newDeadline, changedAt);
        assertEquals(newDeadline, task.getDeadline());
        assertEquals(changedAt, task.getAuditInfo().updatedAt());

        Instant removeAt = changedAt.plus(10, ChronoUnit.MINUTES);
        task.changeDeadline(null, removeAt);
        assertEquals(null, task.getDeadline());
        assertEquals(removeAt, task.getAuditInfo().updatedAt());
    }

    // BR: Deadline change rejects past datetime.
    @Test
    void changeDeadline_shouldThrowWhenNewDeadlineIsPast() {
        Instant now = Instant.parse("2026-02-10T08:00:00Z");
        Task task = createTask(now);
        LocalDateTime pastDeadline = LocalDateTime.ofInstant(now, ZoneOffset.UTC).minusMinutes(1);

        assertThrows(
            DeadlineInPastException.class,
            () -> task.changeDeadline(pastDeadline, now)
        );
    }

    // BR: Status follows allowed transition path.
    @Test
    void changeStatus_shouldFollowValidTransitionsAndUpdateAudit() {
        Instant now = Instant.parse("2026-02-10T08:00:00Z");
        Task task = createTask(now);

        Instant toInProgressAt = now.plus(10, ChronoUnit.MINUTES);
        task.changeStatus(TaskStatus.IN_PROGRESS, toInProgressAt);
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(toInProgressAt, task.getAuditInfo().updatedAt());

        Instant toCompletedAt = now.plus(20, ChronoUnit.MINUTES);
        task.changeStatus(TaskStatus.COMPLETED, toCompletedAt);
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertEquals(toCompletedAt, task.getAuditInfo().updatedAt());
    }

    // BR: Invalid status transition is rejected.
    @Test
    void changeStatus_shouldThrowOnInvalidTransition() {
        Instant now = Instant.parse("2026-02-10T08:00:00Z");
        Task task = createTask(now);

        assertThrows(
            TaskStatusTransitionException.class,
            () -> task.changeStatus(TaskStatus.COMPLETED, now.plus(1, ChronoUnit.MINUTES))
        );
    }

    // BR: Assigning user adds assignee and updates audit.
    @Test
    void assignToUsers_shouldAddAssigneeAndUpdateAudit() {
        Instant now = Instant.parse("2026-02-10T08:00:00Z");
        Task task = createTask(now);
        UserId assignee = UserId.generate();
        Instant assignedAt = now.plus(15, ChronoUnit.MINUTES);

        task.assignToUsers(assignee, assignedAt);

        assertEquals(1, task.getAssigneeIds().size());
        assertEquals(true, task.getAssigneeIds().contains(assignee));
        assertEquals(assignedAt, task.getAuditInfo().updatedAt());
    }

    // BR: Owner cannot be assigned to own task.
    @Test
    void assignToUsers_shouldThrowWhenAssigneeIsOwner() {
        Instant now = Instant.parse("2026-02-10T08:00:00Z");
        Task task = createTask(now);

        assertThrows(
            TaskOwnerNotCanBeAssigneeException.class,
            () -> task.assignToUsers(ownerId(), now.plus(1, ChronoUnit.MINUTES))
        );
    }

    // BR: Duplicate assignee is not allowed.
    @Test
    void assignToUsers_shouldThrowWhenAssigneeAlreadyExists() {
        Instant now = Instant.parse("2026-02-10T08:00:00Z");
        Task task = createTask(now);
        UserId assignee = UserId.generate();

        task.assignToUsers(assignee, now.plus(1, ChronoUnit.MINUTES));

        assertThrows(
            TaskAssigneeAlreadyExistsException.class,
            () -> task.assignToUsers(assignee, now.plus(2, ChronoUnit.MINUTES))
        );
    }

    // BR: Assignee count cannot exceed domain limit.
    @Test
    void assignToUsers_shouldThrowWhenExceedingLimit() {
        Instant now = Instant.parse("2026-02-10T08:00:00Z");
        Task task = createTask(now);

        for (int i = 0; i < 10; i++) {
            task.assignToUsers(UserId.generate(), now.plus(i + 1L, ChronoUnit.MINUTES));
        }

        assertThrows(
            TaskAssigneeLimitExceededException.class,
            () -> task.assignToUsers(UserId.generate(), now.plus(11, ChronoUnit.MINUTES))
        );
    }

    // BR: Removing existing assignee updates task.
    @Test
    void removeAssignee_shouldRemoveAndUpdateAudit() {
        Instant now = Instant.parse("2026-02-10T08:00:00Z");
        Task task = createTask(now);
        UserId assignee = UserId.generate();
        task.assignToUsers(assignee, now.plus(1, ChronoUnit.MINUTES));

        Instant removedAt = now.plus(2, ChronoUnit.MINUTES);
        task.removeAssignee(assignee, removedAt);

        assertEquals(0, task.getAssigneeIds().size());
        assertEquals(removedAt, task.getAuditInfo().updatedAt());
    }

    // BR: Removing non-existing assignee is rejected.
    @Test
    void removeAssignee_shouldThrowWhenAssigneeDoesNotExist() {
        Instant now = Instant.parse("2026-02-10T08:00:00Z");
        Task task = createTask(now);

        assertThrows(
            TaskAssigneeNotExistsException.class,
            () -> task.removeAssignee(UserId.generate(), now.plus(1, ChronoUnit.MINUTES))
        );
    }

    private Task createTask(Instant now) {
        LocalDateTime deadline = LocalDateTime.ofInstant(now, ZoneOffset.UTC).plusDays(2);
        return Task.create(
            TaskId.fromString("09d2cbde-458d-4d66-8d64-5e4cb904f2dc"),
            new Title("Initial title"),
            new Description("Initial description"),
            TaskPriority.MEDIUM,
            deadline,
            ownerId(),
            now
        );
    }

    private UserId ownerId() {
        return UserId.fromString("e9535f84-3f3e-45cc-a0ec-a468f916cb6f");
    }
}
