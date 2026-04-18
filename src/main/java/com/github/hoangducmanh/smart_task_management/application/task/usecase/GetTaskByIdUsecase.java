package com.github.hoangducmanh.smart_task_management.application.task.usecase;

import java.util.UUID;

import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskSummaryResult;
import com.github.hoangducmanh.smart_task_management.application.task.port.in.GetTaskByIdPort;
import com.github.hoangducmanh.smart_task_management.application.task.port.out.TaskCacheStorePort;
import com.github.hoangducmanh.smart_task_management.application.task.port.out.TaskQueryServicePort;

public class GetTaskByIdUsecase implements GetTaskByIdPort {

    private final TaskQueryServicePort taskQueryRepository;
    private final TaskCacheStorePort taskCacheStore;

    public GetTaskByIdUsecase(TaskQueryServicePort taskQueryRepository, TaskCacheStorePort taskCacheStore) {
        this.taskQueryRepository = taskQueryRepository;
        this.taskCacheStore = taskCacheStore;
    }
    
    @Override
    public TaskSummaryResult execute(UUID taskId) {
        
        TaskSummaryResult cachedTask = taskCacheStore.getTaskCache(taskId);
        if (cachedTask != null) {
            return cachedTask;
        }

        // If not in cache, query from the database
        TaskSummaryResult task = taskQueryRepository.findTaskById(taskId);
        if (task != null) {
            
            taskCacheStore.putTaskCache(
                taskId, UUID.fromString(task.ownerId()), task.title(), task.description(), 
                task.status(), task.priority(), task.deadline());
        }
        return task;
    }

}
