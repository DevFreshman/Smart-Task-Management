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
import com.github.hoangducmanh.smart_task_management.application.task.dto.command.ChangeTaskStatusCommand;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskStatusResult;
import com.github.hoangducmanh.smart_task_management.application.task.exception.TaskNotFoundException;
import com.github.hoangducmanh.smart_task_management.application.task.exception.TaskOwnershipException;
import com.github.hoangducmanh.smart_task_management.application.task.port.out.event.TaskEventPublisher;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.TaskStatusTransitionException;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Description;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Task;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskId;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskPriority;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskStatus;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Title;
import com.github.hoangducmanh.smart_task_management.domain.task.repository.TaskRepository;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;

class ChangeTaskStatusUseCaseTest {

    private TaskRepository taskRepository;
    private TaskEventPublisher taskEventPublisher;
    private TimeProvider clock;
    private ChangeTaskStatusUseCase changeTaskStatusUseCase;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        taskEventPublisher = mock(TaskEventPublisher.class);
        clock = mock(TimeProvider.class);
        changeTaskStatusUseCase = new ChangeTaskStatusUseCase(taskRepository, taskEventPublisher, clock);
    }

    // Case: Owner changes task status successfully.
    // Verify status is updated with valid transition, task is saved, status-change event is published, and result returns the new display name.
    @Test
    void changeTaskStatus_shouldUpdateStatusPersistAndPublishEvent() {
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID taskId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Task task = existingTask(taskId, ownerId, Instant.parse("2026-03-01T08:00:00Z"));
        Instant updatedAt = Instant.parse("2026-03-02T10:00:00Z");
        ChangeTaskStatusCommand command = new ChangeTaskStatusCommand(ownerId, taskId, "IN_PROGRESS");
        when(taskRepository.findById(TaskId.of(taskId))).thenReturn(Optional.of(task));
        when(clock.now()).thenReturn(updatedAt);

        TaskStatusResult result = changeTaskStatusUseCase.changeTaskStatus(command);

        verify(taskRepository).save(task);
        verify(taskEventPublisher).publishTaskStatusChangeEvent(taskId, "IN PROGRESS");
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals(updatedAt, task.getAuditInfo().updatedAt());
        assertEquals(taskId, result.taskId());
        assertEquals("IN PROGRESS", result.status());
    }

    // Case: Task to change status not found.
    // Verify throw TaskNotFoundException and do not save or publish event.
    @Test
    void changeTaskStatus_shouldThrowWhenTaskNotFound() {
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID taskId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        ChangeTaskStatusCommand command = new ChangeTaskStatusCommand(ownerId, taskId, "IN_PROGRESS");
        when(taskRepository.findById(TaskId.of(taskId))).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(
            TaskNotFoundException.class,
            () -> changeTaskStatusUseCase.changeTaskStatus(command)
        );

        assertEquals("Task not found with id: 22222222-2222-2222-2222-222222222222", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
        verifyNoInteractions(taskEventPublisher);
    }

    // Case: Requester does not own the task.
    // Verify use case blocks the status change before mutating the state, saving, or publishing an event.
    @Test
    void changeTaskStatus_shouldThrowWhenRequesterDoesNotOwnTask() {
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID requesterId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID taskId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Task task = existingTask(taskId, ownerId, Instant.parse("2026-03-01T08:00:00Z"));
        ChangeTaskStatusCommand command = new ChangeTaskStatusCommand(requesterId, taskId, "IN_PROGRESS");
        when(taskRepository.findById(TaskId.of(taskId))).thenReturn(Optional.of(task));

        TaskOwnershipException exception = assertThrows(
            TaskOwnershipException.class,
            () -> changeTaskStatusUseCase.changeTaskStatus(command)
        );

        assertEquals("User does not have permission to change status of this task", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
        verifyNoInteractions(taskEventPublisher);
    }

    // Case: Transition status is invalid.
    // Verify domain exception is bubbled up intact and use case does not save / publish event when changing from TODO to COMPLETED.
    @Test
    void changeTaskStatus_shouldThrowWhenTransitionIsInvalid() {
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID taskId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Task task = existingTask(taskId, ownerId, Instant.parse("2026-03-01T08:00:00Z"));
        ChangeTaskStatusCommand command = new ChangeTaskStatusCommand(ownerId, taskId, "COMPLETED");
        when(taskRepository.findById(TaskId.of(taskId))).thenReturn(Optional.of(task));
        when(clock.now()).thenReturn(Instant.parse("2026-03-02T10:00:00Z"));

        TaskStatusTransitionException exception = assertThrows(
            TaskStatusTransitionException.class,
            () -> changeTaskStatusUseCase.changeTaskStatus(command)
        );

        assertEquals("Invalid status transition from TODO to COMPLETED", exception.getMessage());
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
