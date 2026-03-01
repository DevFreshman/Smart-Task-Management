package com.github.hoangducmanh.smart_task_management.domain.task.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.github.hoangducmanh.smart_task_management.domain.shared.PageResult;
import com.github.hoangducmanh.smart_task_management.domain.task.model.Task;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskFilter;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskId;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;

public interface TaskRepository {
    Optional<Task> findById(TaskId id);
    Task save(Task task);
    boolean existsById(TaskId id);

    // User
    PageResult<Task> findByOwnerId(UserId ownerId, int page, int size);
    PageResult<Task> findByAssigneeId(UserId assigneeId, int page, int size);
    PageResult<Task> findByFilter(UserId ownerId, TaskFilter filter, int page, int size);
    PageResult<Task> findByKeyword(UserId reqUserId,String keyword, int page, int size);

    // ADMIN
    PageResult<Task> findAllWithFilter(TaskFilter filter, int page, int size);

    // Scheduler
    List<Task> findTasksDueWithin24Hours(LocalDateTime from, LocalDateTime to);
}
