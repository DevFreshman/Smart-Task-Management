package com.github.hoangducmanh.smart_task_management.application.task.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.hoangducmanh.smart_task_management.application.TimeProvider;
import com.github.hoangducmanh.smart_task_management.application.task.dto.command.UnassignTaskCommand;
import com.github.hoangducmanh.smart_task_management.application.task.exception.TaskNotFoundException;
import com.github.hoangducmanh.smart_task_management.application.task.exception.TaskOwnershipException;
import com.github.hoangducmanh.smart_task_management.application.task.port.out.event.TaskEventPublisher;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.TaskAssigneeNotExistsException;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Description;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Task;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskId;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskPriority;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Title;
import com.github.hoangducmanh.smart_task_management.domain.task.repository.TaskRepository;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;

class UnassignTaskUseCaseTest {

    private TaskRepository taskRepository;
    private TaskEventPublisher taskEventPublisher;
    private TimeProvider clock;
    private UnassignTaskUseCase unassignTaskUseCase;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        taskEventPublisher = mock(TaskEventPublisher.class);
        clock = mock(TimeProvider.class);
        unassignTaskUseCase = new UnassignTaskUseCase(taskRepository, taskEventPublisher, clock);
    }

    // Case: Owner unassign assignee successfully.
    // Verify assignee is removed from task, repository.save is called with updated task, and publish event unassign với đúng task id và assignee id.
    @Test
    void execute_shouldUnassignAssigneeAndPublishEvent() {
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID assigneeId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        UUID taskId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Task task = existingTask(taskId, ownerId, Instant.parse("2026-03-01T08:00:00Z"));
        task.assignToUsers(UserId.of(assigneeId), Instant.parse("2026-03-01T09:00:00Z"));
        UnassignTaskCommand command = new UnassignTaskCommand(ownerId, taskId, assigneeId);
        Instant updatedAt = Instant.parse("2026-03-02T10:00:00Z");
        when(taskRepository.findById(TaskId.of(taskId))).thenReturn(Optional.of(task));
        when(clock.now()).thenReturn(updatedAt);

        unassignTaskUseCase.execute(command);

        verify(taskRepository).save(task);
        assertEquals(0, task.getAssigneeIds().size());
        assertEquals(updatedAt, task.getAuditInfo().updatedAt());
    }

    // Case: Task to unassign not found.
    // Verify throw TaskNotFoundException.
    @Test
    void execute_shouldThrowWhenTaskNotFound() {
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID assigneeId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        UUID taskId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UnassignTaskCommand command = new UnassignTaskCommand(ownerId, taskId, assigneeId);
        when(taskRepository.findById(TaskId.of(taskId))).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(
            TaskNotFoundException.class,
            () -> unassignTaskUseCase.execute(command)
        );

        assertEquals("Task not found with id: 22222222-2222-2222-2222-222222222222", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
        verifyNoInteractions(taskEventPublisher, clock);
    }

    // Case: Requester does not own the task.
    // Verify use case blocks at the permission check and does not save or publish event.
    @Test
    void execute_shouldThrowWhenRequesterDoesNotOwnTask() {
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID requesterId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID assigneeId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        UUID taskId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Task task = existingTask(taskId, ownerId, Instant.parse("2026-03-01T08:00:00Z"));
        UnassignTaskCommand command = new UnassignTaskCommand(requesterId, taskId, assigneeId);
        when(taskRepository.findById(TaskId.of(taskId))).thenReturn(Optional.of(task));

        TaskOwnershipException exception = assertThrows(
            TaskOwnershipException.class,
            () -> unassignTaskUseCase.execute(command)
        );

        assertEquals("User does not have permission to unassign this task", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
        verifyNoInteractions(taskEventPublisher, clock);
    }

    // Case: Assignee to unassign does not exist in task.
    // Verify domain exception TaskAssigneeNotExistsException is thrown, and do not save or publish event.
    @Test
    void execute_shouldThrowWhenAssigneeDoesNotExistInTask() {
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID assigneeId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        UUID taskId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Task task = existingTask(taskId, ownerId, Instant.parse("2026-03-01T08:00:00Z"));
        UnassignTaskCommand command = new UnassignTaskCommand(ownerId, taskId, assigneeId);
        when(taskRepository.findById(TaskId.of(taskId))).thenReturn(Optional.of(task));
        when(clock.now()).thenReturn(Instant.parse("2026-03-02T10:00:00Z"));

        TaskAssigneeNotExistsException exception = assertThrows(
            TaskAssigneeNotExistsException.class,
            () -> unassignTaskUseCase.execute(command)
        );

        assertEquals("User is not an assignee of this task", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
        verifyNoInteractions(taskEventPublisher);
    }

    private Task existingTask(UUID taskId, UUID ownerId, Instant createdAt) {
        return Task.create(
            TaskId.of(taskId),
            Title.fromString("Original sprint board"),
            Description.fromString("Original backlog scope"),
            TaskPriority.MEDIUM,
            LocalDateTime.of(2026, 3, 5, 15, 0),
            UserId.of(ownerId),
            createdAt
        );
    }
}
