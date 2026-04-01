package com.github.hoangducmanh.smart_task_management.infrastructure.cache.store;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskSummaryResult;
import com.github.hoangducmanh.smart_task_management.application.task.port.out.TaskCacheStorePort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TaskCacheStoreAdapter implements TaskCacheStorePort {

    private final StringRedisTemplate redisTemplate;
    private final String keyPrefix = "task_cache:";
    private final Duration TTL = Duration.ofHours(1);


    @Override
    public void evictTaskCache(UUID taskId) {
        String key = keyPrefix + taskId.toString();
        redisTemplate.delete(key);
    }

    @Override
    public void evictAllTaskCache() {
        // This is a simple implementation that deletes all keys with the prefix.
        // In production, consider using Redis keyspace notifications or a more efficient way to track keys.
        redisTemplate.keys(keyPrefix + "*").forEach(redisTemplate::delete);
    }

    @Override
    @SuppressWarnings("null")
    public void putTaskCache(UUID taskId, UUID ownerId, String title, String description, 
        String status, String priority,String deadline) {
        String key = keyPrefix + taskId.toString();
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        hashOps.put(key, "ownerId", ownerId.toString());
        if(title != null) {
            hashOps.put(key, "title", title);
        }
        if(description != null) {
            hashOps.put(key, "description", description);
        }
        if(status != null) {
            hashOps.put(key, "status", status);
        }
        if(priority != null) {
            hashOps.put(key, "priority", priority);
        }
        if(deadline != null) {
            hashOps.put(key, "deadline", deadline);
        }
        redisTemplate.expire(key, TTL);
    }

    @Override
    public TaskSummaryResult getTaskCache(UUID taskId) {
        String key = keyPrefix + taskId.toString();
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        if (!redisTemplate.hasKey(key)) {
            return null;
        }
        return new TaskSummaryResult(
            taskId.toString(),
            hashOps.get(key, "title"),
            hashOps.get(key, "description"),
            hashOps.get(key, "ownerId"),
            hashOps.get(key, "status"),
            hashOps.get(key, "priority"),
            hashOps.get(key, "deadline")
        );

    }

}
