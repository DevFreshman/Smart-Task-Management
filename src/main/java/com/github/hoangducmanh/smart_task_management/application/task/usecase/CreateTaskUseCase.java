package com.github.hoangducmanh.smart_task_management.application.task.usecase;

import java.time.LocalDateTime;

import org.springframework.transaction.annotation.Transactional;

import com.github.hoangducmanh.smart_task_management.application.TimeProvider;
import com.github.hoangducmanh.smart_task_management.application.task.dto.command.CreateTaskCommand;
import com.github.hoangducmanh.smart_task_management.application.task.dto.event.TaskCreateEvent;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskResult;
import com.github.hoangducmanh.smart_task_management.application.task.port.in.CreateTaskPort;
import com.github.hoangducmanh.smart_task_management.application.task.port.out.event.TaskEventPublisher;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Description;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Task;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskId;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskPriority;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Title;
import com.github.hoangducmanh.smart_task_management.domain.task.repository.TaskRepository;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;


public class CreateTaskUseCase implements CreateTaskPort{

    private final TaskRepository taskRepository;
    private final TaskEventPublisher taskEventPublisher;
    private final TimeProvider clockSystem;

    public CreateTaskUseCase(TaskRepository taskRepository, TaskEventPublisher taskEventPublisher, TimeProvider clockSystem) {
        this.taskRepository = taskRepository;
        this.taskEventPublisher = taskEventPublisher;
        this.clockSystem = clockSystem;
    }

    @Transactional
    @Override
    public TaskResult execute(CreateTaskCommand createTaskCommand) {
        TaskId taskId = TaskId.generate();
        Title title = Title.fromString(createTaskCommand.title());
        Description description = Description.fromString(createTaskCommand.description());
        TaskPriority priority = TaskPriority.fromString(createTaskCommand.priority());
        LocalDateTime deadline = createTaskCommand.deadline();
        UserId ownerId = UserId.of(createTaskCommand.ownerId());
        Task task = Task.create(
        taskId,
        title,
        description,
        priority, 
        deadline, 
        ownerId, 
        clockSystem.now()
        );
        taskRepository.save(task);

        TaskCreateEvent event = new TaskCreateEvent(
            task.getId().value());
        
        taskEventPublisher.publishTaskCreateEvent(event);

        return TaskResult.from(task);
    }

}
