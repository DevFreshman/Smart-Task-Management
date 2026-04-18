package com.github.hoangducmanh.smart_task_management.application.task.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.github.hoangducmanh.smart_task_management.application.task.dto.command.DeleteTaskCommand;
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

class DeleteTaskUseCaseTest {

    private TaskRepository taskRepository;
    private TaskEventPublisher taskEventPublisher;
    private TimeProvider clock;
    private DeleteTaskUseCase deleteTaskUseCase;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        taskEventPublisher = mock(TaskEventPublisher.class);
        clock = mock(TimeProvider.class);
        deleteTaskUseCase = new DeleteTaskUseCase(taskRepository, taskEventPublisher, clock);
    }

    // Case: Owner delete task successfully.
    // Verify use case soft delete task.
    @Test
    void deleteTask_shouldSoftDeleteAndPublishEvent() {
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID taskId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Instant deletedAt = Instant.parse("2026-03-02T10:00:00Z");
        Task task = existingTask(taskId, ownerId, Instant.parse("2026-03-01T08:00:00Z"));
        DeleteTaskCommand command = new DeleteTaskCommand(ownerId, taskId);
        when(clock.now()).thenReturn(deletedAt);
        when(taskRepository.findById(TaskId.of(taskId))).thenReturn(Optional.of(task));

        deleteTaskUseCase.execute(command);

        verify(taskRepository).softDeleteTask(TaskId.of(taskId), deletedAt);
    }

    // Case: Task to delete not found.
    // Verify throw TaskNotFoundException and do not call soft delete or publish event.
    @Test
    void deleteTask_shouldThrowWhenTaskNotFound() {
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID taskId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        DeleteTaskCommand command = new DeleteTaskCommand(ownerId, taskId);
        when(clock.now()).thenReturn(Instant.parse("2026-03-02T10:00:00Z"));
        when(taskRepository.findById(TaskId.of(taskId))).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(
            TaskNotFoundException.class,
            () -> deleteTaskUseCase.execute(command)
        );

        assertEquals("Task not found with id: 22222222-2222-2222-2222-222222222222", exception.getMessage());
        verify(taskRepository, never()).softDeleteTask(TaskId.of(taskId), Instant.parse("2026-03-02T10:00:00Z"));
        verifyNoInteractions(taskEventPublisher);
    }

    // Case: Requester does not own the task.
    // Verify use case throws TaskOwnershipException and do not call soft delete or publish event.
    @Test
    void deleteTask_shouldThrowWhenRequesterDoesNotOwnTask() {
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID requesterId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID taskId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        Task task = existingTask(taskId, ownerId, Instant.parse("2026-03-01T08:00:00Z"));
        DeleteTaskCommand command = new DeleteTaskCommand(requesterId, taskId);
        when(clock.now()).thenReturn(Instant.parse("2026-03-02T10:00:00Z"));
        when(taskRepository.findById(TaskId.of(taskId))).thenReturn(Optional.of(task));

        TaskOwnershipException exception = assertThrows(
            TaskOwnershipException.class,
            () -> deleteTaskUseCase.execute(command)
        );

        assertEquals("User does not have permission to delete this task", exception.getMessage());
        verify(taskRepository, never()).softDeleteTask(TaskId.of(taskId), Instant.parse("2026-03-02T10:00:00Z"));
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
