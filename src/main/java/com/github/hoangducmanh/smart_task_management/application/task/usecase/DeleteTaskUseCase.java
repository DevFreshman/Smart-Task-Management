package com.github.hoangducmanh.smart_task_management.application.task.usecase;

import java.time.Instant;

import com.github.hoangducmanh.smart_task_management.application.TimeProvider;
import com.github.hoangducmanh.smart_task_management.application.task.dto.command.DeleteTaskCommand;
import com.github.hoangducmanh.smart_task_management.application.task.dto.event.TaskDeleteEvent;
import com.github.hoangducmanh.smart_task_management.application.task.exception.TaskNotFoundException;
import com.github.hoangducmanh.smart_task_management.application.task.exception.TaskOwnershipException;
import com.github.hoangducmanh.smart_task_management.application.task.port.in.DeleteTaskPort;
import com.github.hoangducmanh.smart_task_management.application.task.port.out.event.TaskEventPublisher;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Task;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskId;
import com.github.hoangducmanh.smart_task_management.domain.task.repository.TaskRepository;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;

public class DeleteTaskUseCase implements DeleteTaskPort {

    private final TimeProvider clock;
    private final TaskRepository taskRepository;
    private final TaskEventPublisher taskEventPublisher;

    public DeleteTaskUseCase(TaskRepository taskRepository, TaskEventPublisher taskEventPublisher, TimeProvider clock) {
        this.taskRepository = taskRepository;
        this.taskEventPublisher = taskEventPublisher;
        this.clock = clock;
    }

    @Override
    public void deleteTask(DeleteTaskCommand command) {
        UserId userId = UserId.of(command.requestId());
        TaskId taskId = TaskId.of(command.taskId());
        Instant deletedAt = clock.now();
        // Perform soft delete by setting deletedAt timestamp
        Task task = taskRepository.findById(taskId).orElseThrow(
            () -> new TaskNotFoundException("Task not found with id: " + taskId.asString())
        );
        if(!task.isOwnedByUser(userId)) {
            throw new TaskOwnershipException("User does not have permission to delete this task");
        }
        taskRepository.softDeleteTask(taskId, deletedAt);
        // Publish task deletion event

        TaskDeleteEvent event = new TaskDeleteEvent(taskId.value());

        taskEventPublisher.publishTaskDeleteEvent(event);
    }
}