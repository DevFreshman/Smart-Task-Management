package com.github.hoangducmanh.smart_task_management.infrastructure.persistence.task.adapter;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.github.hoangducmanh.smart_task_management.application.task.dto.command.TaskFilterCommand;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.PageResult;
import com.github.hoangducmanh.smart_task_management.application.task.dto.result.TaskSummaryResult;
import com.github.hoangducmanh.smart_task_management.application.task.port.out.TaskQueryServicePort;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.task.repository.TaskJpaRepository;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.task.specification.TaskSpecification;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Repository
public class JpaTaskQueryServiceAdapter implements TaskQueryServicePort {

    private final TaskJpaRepository taskJpaRepository;

    @Override
    public PageResult<TaskSummaryResult> findTasksByFilter(TaskFilterCommand filter, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<TaskSummaryResult> taskSummaries = taskJpaRepository.findAllByOwnerId(TaskSpecification.hasFilter(filter), pageRequest)
                .map(taskEntity -> new TaskSummaryResult(
                        taskEntity.getTaskId().toString(),
                        taskEntity.getTitle(),
                        taskEntity.getDescription(),
                        taskEntity.getOwnerId().toString(),
                        taskEntity.getTaskStatus(),
                        taskEntity.getTaskPriority(),
                        taskEntity.getDeadline() != null ? taskEntity.getDeadline().toString() : null
                ));
        return new PageResult<TaskSummaryResult>(
            taskSummaries.getContent(),
             taskSummaries.getTotalElements(), 
            taskSummaries.getNumber(), 
            taskSummaries.getSize(),  
            taskSummaries.getTotalPages());
    }

    @Override
    @SuppressWarnings("null")
    public TaskSummaryResult findTaskById(UUID taskId) {
        return taskJpaRepository.findById(taskId)
                .map(taskEntity -> new TaskSummaryResult(
                        taskEntity.getTaskId().toString(),
                        taskEntity.getTitle(),
                        taskEntity.getDescription(),
                        taskEntity.getOwnerId().toString(),
                        taskEntity.getTaskStatus(),
                        taskEntity.getTaskPriority(),
                        taskEntity.getDeadline() != null ? taskEntity.getDeadline().toString() : null
                ))
                .orElse(null);
    }
}