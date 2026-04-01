package com.github.hoangducmanh.smart_task_management.application.task.usecase;

import com.github.hoangducmanh.smart_task_management.application.TimeProvider;
import com.github.hoangducmanh.smart_task_management.application.task.dto.command.UnassignTaskCommand;
import com.github.hoangducmanh.smart_task_management.application.task.dto.event.TaskUpdateEvent;
import com.github.hoangducmanh.smart_task_management.application.task.exception.TaskNotFoundException;
import com.github.hoangducmanh.smart_task_management.application.task.exception.TaskOwnershipException;
import com.github.hoangducmanh.smart_task_management.application.task.port.in.UnassignTaskPort;
import com.github.hoangducmanh.smart_task_management.application.task.port.out.event.TaskEventPublisher;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Task;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskId;
import com.github.hoangducmanh.smart_task_management.domain.task.repository.TaskRepository;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;

public class UnassignTaskUseCase implements UnassignTaskPort {

    private final TaskRepository taskRepository;
    private final TaskEventPublisher taskEventPublisher;
    private final TimeProvider clock;

    public UnassignTaskUseCase(TaskRepository taskRepository, TaskEventPublisher taskEventPublisher, TimeProvider clock) {
        this.taskRepository = taskRepository;
        this.taskEventPublisher = taskEventPublisher;
        this.clock = clock;
    }

    @Override
    public void execute(UnassignTaskCommand command) {
        UserId requestId = UserId.of(command.requestId());
        UserId assigneeId = UserId.of(command.assigneeId());
        TaskId taskId = TaskId.of(command.taskId());

        Task task = taskRepository.findById(taskId).orElseThrow(
            () -> new TaskNotFoundException("Task not found with id: " + taskId.asString())
        );

        if(!task.isOwnedByUser(requestId)) {
            throw new TaskOwnershipException("User does not have permission to unassign this task");
        }

        task.removeAssignee(assigneeId, clock.now());

        taskRepository.save(task);

        TaskUpdateEvent event = new TaskUpdateEvent(taskId.value());

        taskEventPublisher.publishTaskUpdateEvent(event);
    }
}