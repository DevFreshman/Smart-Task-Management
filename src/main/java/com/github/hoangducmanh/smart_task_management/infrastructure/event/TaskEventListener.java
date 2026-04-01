package com.github.hoangducmanh.smart_task_management.infrastructure.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.github.hoangducmanh.smart_task_management.application.task.dto.event.TaskCreateEvent;
import com.github.hoangducmanh.smart_task_management.application.task.dto.event.TaskDeleteEvent;
import com.github.hoangducmanh.smart_task_management.application.task.dto.event.TaskUpdateEvent;
import com.github.hoangducmanh.smart_task_management.infrastructure.cache.store.TaskCacheStoreAdapter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class TaskEventListener {
    private final TaskCacheStoreAdapter taskCacheStoreAdapter;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskCreate(TaskCreateEvent event) {
        taskCacheStoreAdapter.evictTaskCache(event.taskId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskUpdate(TaskUpdateEvent event) {
        taskCacheStoreAdapter.evictTaskCache(event.taskId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskDelete(TaskDeleteEvent event) {
        taskCacheStoreAdapter.evictTaskCache(event.taskId());
    }

}
