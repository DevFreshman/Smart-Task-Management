package com.github.hoangducmanh.smart_task_management.application.task.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.github.hoangducmanh.smart_task_management.application.TimeProvider;
import com.github.hoangducmanh.smart_task_management.application.task.dto.command.CreateTaskCommand;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskResult;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.DeadlineInPastException;
import com.github.hoangducmanh.smart_task_management.domain.task.exception.InvalidTaskPriorityException;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Task;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskPriority;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskStatus;
import com.github.hoangducmanh.smart_task_management.domain.task.repository.TaskRepository;

class CreateTaskUseCaseTest {

    private TaskRepository taskRepository;
    private TimeProvider clockSystem;
    private CreateTaskUseCase createTaskUseCase;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        clockSystem = mock(TimeProvider.class);
        createTaskUseCase = new CreateTaskUseCase(taskRepository, clockSystem);
    }

    // Case: Create task successfully with valid input.
    // Verify task is saved to repository with correct properties and result is returned.
    @Test
    void execute_shouldCreateTaskAndReturnResult() {
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        LocalDateTime deadline = LocalDateTime.of(2026, 3, 5, 15, 0);
        Instant now = Instant.parse("2026-03-01T08:00:00Z");
        CreateTaskCommand command = new CreateTaskCommand(
            "  Prepare sprint board  ",
            "Define backlog items for the next sprint",
            "High",
            deadline,
            ownerId
        );
        when(clockSystem.now()).thenReturn(now);

        TaskResult result = createTaskUseCase.execute(command);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        Task savedTask = taskCaptor.getValue();

        assertEquals("Prepare sprint board", savedTask.getTitle().value());
        assertEquals("Define backlog items for the next sprint", savedTask.getDescription().value());
        assertEquals(TaskPriority.HIGH, savedTask.getPriority());
        assertEquals(TaskStatus.TODO, savedTask.getStatus());
        assertEquals(deadline, savedTask.getDeadline());
        assertEquals(ownerId, savedTask.getOwnerId().value());
        assertTrue(savedTask.getAssigneeIds().isEmpty());
        assertFalse(savedTask.isDeleted());

        assertEquals(savedTask.getId().value(), result.taskId());
        assertEquals("Prepare sprint board", result.title());
        assertEquals("Define backlog items for the next sprint", result.description());
        assertEquals(TaskStatus.TODO, result.status());
        assertEquals(TaskPriority.HIGH, result.priority());
        assertEquals(deadline, result.deadline());
        assertEquals(ownerId, result.ownerId());
        assertEquals(Set.of(), result.assigneeIds());
        assertEquals(now, result.createdAt());
        assertEquals(now, result.updatedAt());
        assertFalse(result.isDeleted());
    }

    // Case: Deadline is in the past.
    // Verify domain validation fails and DeadlineInPastException is thrown, and task is not saved
    @Test
    void execute_shouldThrowWhenDeadlineIsInPast() {
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Instant now = Instant.parse("2026-03-01T08:00:00Z");
        CreateTaskCommand command = new CreateTaskCommand(
            "Prepare sprint board",
            "Define backlog items for the next sprint",
            "High",
            LocalDateTime.of(2026, 2, 28, 23, 59),
            ownerId
        );
        when(clockSystem.now()).thenReturn(now);

        DeadlineInPastException exception = assertThrows(
            DeadlineInPastException.class,
            () -> createTaskUseCase.execute(command)
        );

        assertEquals("Deadline cannot be in the past", exception.getMessage());
        verifyNoInteractions(taskRepository);
    }

    // Case: Priority input is invalid.
    // Verify fail fast validation catches invalid priority and InvalidTaskPriorityException is thrown, and task is not saved
    @Test
    void execute_shouldThrowWhenPriorityIsInvalid() {
        UUID ownerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        CreateTaskCommand command = new CreateTaskCommand(
            "Prepare sprint board",
            "Define backlog items for the next sprint",
            "URGENT",
            LocalDateTime.of(2026, 3, 5, 15, 0),
            ownerId
        );

        InvalidTaskPriorityException exception = assertThrows(
            InvalidTaskPriorityException.class,
            () -> createTaskUseCase.execute(command)
        );

        assertEquals("Invalid task priority: URGENT", exception.getMessage());
        verifyNoInteractions(taskRepository, clockSystem);
    }
}
