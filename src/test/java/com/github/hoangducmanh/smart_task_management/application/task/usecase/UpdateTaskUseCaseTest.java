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
import com.github.hoangducmanh.smart_task_management.application.task.dto.command.UpdateTaskCommand;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskResult;
import com.github.hoangducmanh.smart_task_management.application.task.exception.TaskNotFoundException;
import com.github.hoangducmanh.smart_task_management.application.task.exception.TaskOwnershipException;
import com.github.hoangducmanh.smart_task_management.application.task.port.out.event.TaskEventPublisher;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Description;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Task;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskId;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskPriority;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Title;
import com.github.hoangducmanh.smart_task_management.domain.task.repository.TaskRepository;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;

class UpdateTaskUseCaseTest {

    private TaskRepository taskRepository;
    private TaskEventPublisher taskEventPublisher;
    private TimeProvider clock;
    private UpdateTaskUseCase updateTaskUseCase;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        taskEventPublisher = mock(TaskEventPublisher.class);
        clock = mock(TimeProvider.class);
        updateTaskUseCase = new UpdateTaskUseCase(taskRepository, taskEventPublisher, clock);
    }

    // Case: Owner updates task successfully.
    // Verify task is updated with new data, saved, and update event is published.
    @Test
    void execute_shouldUpdateTaskPersistAndPublishEvent() {
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID taskId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Instant createdAt = Instant.parse("2026-03-01T08:00:00Z");
        Instant updatedAt = Instant.parse("2026-03-02T09:30:00Z");
        Task task = existingTask(taskId, ownerId, createdAt);
        UpdateTaskCommand command = new UpdateTaskCommand(
            ownerId,
            taskId,
            "  Updated sprint board  ",
            "Updated backlog scope for the sprint",
            "Critical"
        );
        when(taskRepository.findById(TaskId.of(taskId))).thenReturn(Optional.of(task));
        when(clock.now()).thenReturn(updatedAt);

        TaskResult result = updateTaskUseCase.execute(command);

        verify(taskRepository).save(task);
        verify(taskEventPublisher).publishTaskUpdateEvent(
            taskId,
            "Updated sprint board",
            "Updated backlog scope for the sprint",
            "CRITICAL"
        );
        assertEquals("Updated sprint board", task.getTitle().value());
        assertEquals("Updated backlog scope for the sprint", task.getDescription().value());
        assertEquals(TaskPriority.CRITICAL, task.getPriority());
        assertEquals(updatedAt, task.getAuditInfo().updatedAt());

        assertEquals(taskId, result.taskId());
        assertEquals("Updated sprint board", result.title());
        assertEquals("Updated backlog scope for the sprint", result.description());
        assertEquals(TaskPriority.CRITICAL, result.priority());
        assertEquals(createdAt, result.createdAt());
        assertEquals(updatedAt, result.updatedAt());
    }

    // Case: Task to update not found.
    // Verify throw TaskNotFoundException and do not save or publish event.
    @Test
    void execute_shouldThrowWhenTaskNotFound() {
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID taskId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UpdateTaskCommand command = new UpdateTaskCommand(
            ownerId,
            taskId,
            "Updated sprint board",
            "Updated backlog scope for the sprint",
            "Critical"
        );
        when(taskRepository.findById(TaskId.of(taskId))).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(
            TaskNotFoundException.class,
            () -> updateTaskUseCase.execute(command)
        );

        assertEquals("Task not found with id: 22222222-2222-2222-2222-222222222222", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
        verifyNoInteractions(taskEventPublisher);
    }

    // Case: Requester does not own the task.
    // Verify use case blocks at the permission check and does not save or publish event.
    @Test
    void execute_shouldThrowWhenRequesterDoesNotOwnTask() {
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID requesterId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID taskId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Task task = existingTask(taskId, ownerId, Instant.parse("2026-03-01T08:00:00Z"));
        UpdateTaskCommand command = new UpdateTaskCommand(
            requesterId,
            taskId,
            "Updated sprint board",
            "Updated backlog scope for the sprint",
            "Critical"
        );
        when(taskRepository.findById(TaskId.of(taskId))).thenReturn(Optional.of(task));

        TaskOwnershipException exception = assertThrows(
            TaskOwnershipException.class,
            () -> updateTaskUseCase.execute(command)
        );

        assertEquals("User does not have permission to update this task", exception.getMessage());
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
