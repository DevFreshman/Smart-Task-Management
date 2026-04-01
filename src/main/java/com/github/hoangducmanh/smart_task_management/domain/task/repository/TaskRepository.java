package com.github.hoangducmanh.smart_task_management.domain.task.repository;

import java.time.Instant;
import java.util.Optional;

import com.github.hoangducmanh.smart_task_management.domain.task.model.Task;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskId;

public interface TaskRepository {
    // base find method for tasks without assignment info.
    Optional<Task> findById(TaskId id);
    
    // Find task with assignees for internal use in use cases that need to manage assignment information.
    Optional<Task> findByIdWithAssignees(TaskId id);

    // Delete task (soft delete by setting deletedAt timestamp)
    void softDeleteTask(TaskId taskId, Instant deletedAt);

    Task save(Task task);
    boolean existsById(TaskId id);
}
