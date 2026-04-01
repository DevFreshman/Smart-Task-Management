package com.github.hoangducmanh.smart_task_management.infrastructure.persistence.task.adapter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.github.hoangducmanh.smart_task_management.domain.task.model.Task;
import com.github.hoangducmanh.smart_task_management.domain.task.model.TaskId;
import com.github.hoangducmanh.smart_task_management.domain.task.repository.TaskRepository;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.task.TaskPersistenceMapper;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.task.entity.TaskEntity;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.task.repository.TaskAssigneeJpaRepository;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.task.repository.TaskJpaRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class TaskRepositoryAdapter implements TaskRepository {

    private final TaskJpaRepository taskJpaRepository;
    private final TaskAssigneeJpaRepository taskAssigneeRepository;
    private final TaskPersistenceMapper taskPersistenceMapper;

    @SuppressWarnings("null")
    @Override
    public Optional<Task> findById(TaskId id) {
        TaskEntity entity = taskJpaRepository.findById(id.value()).orElse(null);
        return Optional.ofNullable(entity).map(taskPersistenceMapper::toDomainWithoutAssignee);
    }

    @SuppressWarnings("null")
    @Override
    public Task save(Task task) {
        TaskEntity entity = taskPersistenceMapper.toEntity(task);
        TaskEntity savedEntity = taskJpaRepository.save(entity);
        return taskPersistenceMapper.toDomainWithoutAssignee(savedEntity);
    }

    @SuppressWarnings("null")
    @Override
    public boolean existsById(TaskId id) {
        return taskJpaRepository.existsById(id.value());
    }

    @Override
    public void softDeleteTask(TaskId taskId, Instant deletedAt) {
        taskJpaRepository.softDeleteById(taskId.value(), deletedAt);
    }

    @Override
    @SuppressWarnings("null")
    @Transactional
    public Optional<Task> findByIdWithAssignees(TaskId id) {
        TaskEntity entity = taskJpaRepository.findById(id.value()).orElse(null);
        if (entity == null) {
            return Optional.empty();
        }
        Set<UUID> assignees = taskAssigneeRepository.findAssigneeIdsByTaskId(id.value());
        HashSet<UserId> assigneeIds = assignees.stream()
        .map(i -> UserId.of(i))
        .collect(Collectors.toCollection(HashSet::new));
        return Optional.of(taskPersistenceMapper.toDomainWithAssignees(entity, assigneeIds));
    }

}
