package com.github.hoangducmanh.smart_task_management.application.task.usecase;

import com.github.hoangducmanh.smart_task_management.application.TimeProvider;
import com.github.hoangducmanh.smart_task_management.application.task.dto.command.ChangeTaskStatusCommand;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskStatusResult;
import com.github.hoangducmanh.smart_task_management.application.task.exception.TaskNotFoundException;
import com.github.hoangducmanh.smart_task_management.application.task.exception.TaskOwnershipException;
import com.github.hoangducmanh.smart_task_management.application.task.port.in.ChangeTaskStatusPort;
import com.github.hoangducmanh.smart_task_management.application.task.port.out.event.TaskEventPublisher;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Task;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskId;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskStatus;
import com.github.hoangducmanh.smart_task_management.domain.task.repository.TaskRepository;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;

public class ChangeTaskStatusUseCase implements ChangeTaskStatusPort {

    private final TaskRepository taskRepository;
    private final TaskEventPublisher taskEventPublisher;
    private final TimeProvider clock;

    public ChangeTaskStatusUseCase(TaskRepository taskRepository, TaskEventPublisher taskEventPublisher, TimeProvider clock) {
        this.taskRepository = taskRepository;
        this.taskEventPublisher = taskEventPublisher;
        this.clock = clock;
    }
    @Override
    public TaskStatusResult changeTaskStatus(ChangeTaskStatusCommand command) {
        UserId userId = UserId.of(command.requestId());
        TaskId taskId = TaskId.of(command.taskId());
        TaskStatus newStatus = TaskStatus.valueOf(command.newStatus());
        
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId.value()));
        
        if (!task.isOwnedByUser(userId)) {
            throw new TaskOwnershipException("User does not have permission to change status of this task");
        }

        task.changeStatus(newStatus, clock.now());
        
        taskRepository.save(task);

        taskEventPublisher.publishTaskStatusChangeEvent(task.getId().value(), newStatus.getDisplayName());

        return new TaskStatusResult(task.getId().value(), task.getStatus().getDisplayName());
    }
    
}
