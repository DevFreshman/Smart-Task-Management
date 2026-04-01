package com.github.hoangducmanh.smart_task_management.domain.task.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.github.hoangducmanh.smart_task_management.domain.task.model.Task;

public interface TaskSchedulerRepository {
    // Scheduler
    List<Task> findTasksDueWithin24Hours(LocalDateTime from, LocalDateTime to);
}
