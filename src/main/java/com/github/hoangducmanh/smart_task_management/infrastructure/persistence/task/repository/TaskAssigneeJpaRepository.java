package com.github.hoangducmanh.smart_task_management.infrastructure.persistence.task.repository;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.task.entity.TaskAssigneeEntity;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.task.entity.TaskAssigneeId;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface TaskAssigneeJpaRepository extends JpaRepository<TaskAssigneeEntity, TaskAssigneeId> {
    @Query("SELECT a.id.assigneeId FROM TaskAssigneeEntity a WHERE a.id.taskId = :taskId")
    Set<UUID> findAssigneeIdsByTaskId(@Param("taskId") UUID taskId);
}
