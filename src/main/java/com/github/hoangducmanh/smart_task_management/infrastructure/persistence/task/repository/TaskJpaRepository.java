package com.github.hoangducmanh.smart_task_management.infrastructure.persistence.task.repository;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.task.entity.TaskEntity;

@Repository
public interface TaskJpaRepository extends JpaRepository<TaskEntity, UUID>, JpaSpecificationExecutor<TaskEntity> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE TaskEntity t SET t.deletedAt = :deletedAt WHERE t.id = :id") 
    void softDeleteById(@Param("id") UUID id, @Param("deletedAt") Instant deletedAt);
    
    Page<TaskEntity> findAllByOwnerId(Specification<TaskEntity> spec, PageRequest pageable);


}
