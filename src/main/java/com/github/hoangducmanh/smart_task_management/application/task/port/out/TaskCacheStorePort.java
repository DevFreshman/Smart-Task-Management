package com.github.hoangducmanh.smart_task_management.application.task.port.out;

import java.util.UUID;

import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskSummaryResult;

public interface TaskCacheStorePort {
    void evictTaskCache(UUID taskId);
    void evictAllTaskCache();
    void putTaskCache(UUID taskId, UUID ownerId, String title, String description, String status, String priority, String deadline);
    TaskSummaryResult getTaskCache(UUID taskId);
}
