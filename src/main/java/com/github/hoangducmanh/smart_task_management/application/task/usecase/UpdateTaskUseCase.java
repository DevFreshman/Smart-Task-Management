package com.github.hoangducmanh.smart_task_management.application.task.usecase;

import org.springframework.transaction.annotation.Transactional;

import com.github.hoangducmanh.smart_task_management.application.TimeProvider;
import com.github.hoangducmanh.smart_task_management.application.task.dto.command.UpdateTaskCommand;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskResult;
import com.github.hoangducmanh.smart_task_management.application.task.exception.TaskNotFoundException;
import com.github.hoangducmanh.smart_task_management.application.task.exception.TaskOwnershipException;
import com.github.hoangducmanh.smart_task_management.application.task.port.in.UpdateTaskPort;
import com.github.hoangducmanh.smart_task_management.application.task.port.out.event.UpdateTaskEvent;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Description;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Task;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskId;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskPriority;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Title;
import com.github.hoangducmanh.smart_task_management.domain.task.repository.TaskRepository;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;

public class UpdateTaskUseCase implements UpdateTaskPort{

    private final TaskRepository taskRepository;
    private final TimeProvider clock;
    private final UpdateTaskEvent event;

    public UpdateTaskUseCase(TaskRepository taskRepository, UpdateTaskEvent event, TimeProvider clock) {
        this.taskRepository = taskRepository;
        this.event = event;
        this.clock = clock;
    }

    @Transactional
    @Override
    public TaskResult execute(UpdateTaskCommand updateTaskCommand) {
        UserId userId = UserId.of(updateTaskCommand.requestId());
        TaskId taskId = TaskId.of(updateTaskCommand.taskId());
        Title title = Title.fromString(updateTaskCommand.title());
        Description description = Description.fromString(updateTaskCommand.description());
        TaskPriority priority = TaskPriority.fromString(updateTaskCommand.priority());


        // 1. Fetch the task by ID
        Task task = taskRepository.findById(taskId).orElseThrow(
            () -> new TaskNotFoundException("Task not found with id: " + taskId.asString())
        );
        // 2. Check if the user is the owner of the task
        if(!task.isOwnedByUser(userId)) {
            throw new TaskOwnershipException("User does not have permission to update this task");
        }
        // 3. Update the task with new values
        task.update(title, description, priority, clock.now());
        // 4. Save the updated task
        taskRepository.save(task);
        // 5. Publish task update event
        event.publishTaskUpdateEvent(taskId.value());

        return TaskResult.from(task);
    }
}
