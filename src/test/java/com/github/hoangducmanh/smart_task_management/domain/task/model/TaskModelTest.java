package com.github.hoangducmanh.smart_task_management.domain.task.model;

import com.github.hoangducmanh.smart_task_management.domain.task.exception.DeadlineInPastException;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.TaskAssigneeAlreadyExistsException;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.TaskAssigneeLimitExceededException;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.TaskAssigneeNotExistsException;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.TaskOwnerNotCanBeAssigneeException;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.TaskStatusTransitionException;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskModelTest {

    // BR: Task creation sets default business state.
    @Test
    void create_shouldInitializeDefaultFields() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 2, 10, 8, 0);
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 8, 5);

        Task task = Task.create(
            TaskId.fromString("09d2cbde-458d-4d66-8d64-5e4cb904f2dc"),
            new Title("Implement task domain"),
            new Description("Description"),
            TaskPriority.HIGH,
            now.plusDays(3),
            ownerId(),
            createdAt,
            now
        );

        assertEquals(TaskStatus.TODO, task.getStatus());
        assertEquals(TaskPriority.HIGH, task.getPriority());
        assertEquals(createdAt, task.getAuditInfo().createdAt());
        assertEquals(createdAt, task.getAuditInfo().updatedAt());
        assertEquals(0, task.getAssigneeIds().size());
    }

    // BR: Task cannot be created with deadline in the past.
    @Test
    void create_shouldThrowWhenDeadlineIsInPastComparedToNow() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 8, 0);

        assertThrows(
            DeadlineInPastException.class,
            () -> Task.create(
                TaskId.fromString("09d2cbde-458d-4d66-8d64-5e4cb904f2dc"),
                new Title("Implement task domain"),
                new Description("Description"),
                TaskPriority.HIGH,
                now.minusMinutes(1),
                ownerId(),
                now,
                now
            )
        );
    }

    // BR: Update modifies only provided fields.
    @Test
    void update_shouldChangeOnlyNonNullFields() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 8, 0);
        Task task = createTask(now);

        task.update(new Title("New title"), null, TaskPriority.CRITICAL);

        assertEquals("New title", task.getTitle().value());
        assertEquals("Initial description", task.getDescription().value());
        assertEquals(TaskPriority.CRITICAL, task.getPriority());
    }

    // BR: Deadline change supports set/remove and updates audit.
    @Test
    void changeDeadline_shouldUpdateAndAllowRemovingDeadline() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 8, 0);
        Task task = createTask(now);
        LocalDateTime changedAt = now.plusHours(2);
        LocalDateTime newDeadline = now.plusDays(7);

        task.changeDeadline(newDeadline, changedAt);
        assertEquals(newDeadline, task.getDeadline());
        assertEquals(changedAt, task.getAuditInfo().updatedAt());

        LocalDateTime removeAt = changedAt.plusMinutes(10);
        task.changeDeadline(null, removeAt);
        assertEquals(null, task.getDeadline());
        assertEquals(removeAt, task.getAuditInfo().updatedAt());
    }

    // BR: Deadline change rejects past datetime.
    @Test
    void changeDeadline_shouldThrowWhenNewDeadlineIsPast() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 8, 0);
        Task task = createTask(now);

        assertThrows(
            DeadlineInPastException.class,
            () -> task.changeDeadline(now.minusMinutes(1), now)
        );
    }

    // BR: Status follows allowed transition path.
    @Test
    void changeStatus_shouldFollowValidTransitionsAndUpdateAudit() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 8, 0);
        Task task = createTask(now);

        LocalDateTime toInProgressAt = now.plusMinutes(10);
        task.changeStatus(TaskStatus.IN_PROGRESS, toInProgressAt);
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(toInProgressAt, task.getAuditInfo().updatedAt());

        LocalDateTime toCompletedAt = now.plusMinutes(20);
        task.changeStatus(TaskStatus.COMPLETED, toCompletedAt);
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertEquals(toCompletedAt, task.getAuditInfo().updatedAt());
    }

    // BR: Invalid status transition is rejected.
    @Test
    void changeStatus_shouldThrowOnInvalidTransition() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 8, 0);
        Task task = createTask(now);

        assertThrows(
            TaskStatusTransitionException.class,
            () -> task.changeStatus(TaskStatus.COMPLETED, now.plusMinutes(1))
        );
    }

    // BR: Assigning user adds assignee and updates audit.
    @Test
    void assignToUsers_shouldAddAssigneeAndUpdateAudit() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 8, 0);
        Task task = createTask(now);
        UserId assignee = UserId.generate();
        LocalDateTime assignedAt = now.plusMinutes(15);

        task.assignToUsers(assignee, assignedAt);

        assertEquals(1, task.getAssigneeIds().size());
        assertEquals(true, task.getAssigneeIds().contains(assignee));
        assertEquals(assignedAt, task.getAuditInfo().updatedAt());
    }

    // BR: Owner cannot be assigned to own task.
    @Test
    void assignToUsers_shouldThrowWhenAssigneeIsOwner() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 8, 0);
        Task task = createTask(now);

        assertThrows(
            TaskOwnerNotCanBeAssigneeException.class,
            () -> task.assignToUsers(ownerId(), now.plusMinutes(1))
        );
    }

    // BR: Duplicate assignee is not allowed.
    @Test
    void assignToUsers_shouldThrowWhenAssigneeAlreadyExists() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 8, 0);
        Task task = createTask(now);
        UserId assignee = UserId.generate();

        task.assignToUsers(assignee, now.plusMinutes(1));

        assertThrows(
            TaskAssigneeAlreadyExistsException.class,
            () -> task.assignToUsers(assignee, now.plusMinutes(2))
        );
    }

    // BR: Assignee count cannot exceed domain limit.
    @Test
    void assignToUsers_shouldThrowWhenExceedingLimit() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 8, 0);
        Task task = createTask(now);

        for (int i = 0; i < 10; i++) {
            task.assignToUsers(UserId.generate(), now.plusMinutes(i + 1L));
        }

        assertThrows(
            TaskAssigneeLimitExceededException.class,
            () -> task.assignToUsers(UserId.generate(), now.plusMinutes(11))
        );
    }

    // BR: Removing existing assignee updates task.
    @Test
    void removeAssignee_shouldRemoveAndUpdateAudit() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 8, 0);
        Task task = createTask(now);
        UserId assignee = UserId.generate();
        task.assignToUsers(assignee, now.plusMinutes(1));

        LocalDateTime removedAt = now.plusMinutes(2);
        task.removeAssignee(assignee, removedAt);

        assertEquals(0, task.getAssigneeIds().size());
        assertEquals(removedAt, task.getAuditInfo().updatedAt());
    }

    // BR: Removing non-existing assignee is rejected.
    @Test
    void removeAssignee_shouldThrowWhenAssigneeDoesNotExist() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 10, 8, 0);
        Task task = createTask(now);

        assertThrows(
            TaskAssigneeNotExistsException.class,
            () -> task.removeAssignee(UserId.generate(), now.plusMinutes(1))
        );
    }

    private Task createTask(LocalDateTime now) {
        return Task.create(
            TaskId.fromString("09d2cbde-458d-4d66-8d64-5e4cb904f2dc"),
            new Title("Initial title"),
            new Description("Initial description"),
            TaskPriority.MEDIUM,
            now.plusDays(2),
            ownerId(),
            now,
            now
        );
    }

    private UserId ownerId() {
        return UserId.fromString("e9535f84-3f3e-45cc-a0ec-a468f916cb6f");
    }
}
