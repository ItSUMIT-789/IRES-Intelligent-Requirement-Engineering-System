package com.ires.task.repository;

import com.ires.task.entity.DeveloperTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;
import com.ires.task.entity.TaskStatus;
import java.util.Collection;

public interface DeveloperTaskRepository extends JpaRepository<DeveloperTask, UUID>, JpaSpecificationExecutor<DeveloperTask> {
    long countByStatusIn(Collection<TaskStatus> statuses);
    long countByAssignedToIdAndStatusIn(UUID userId, Collection<TaskStatus> statuses);
    long countByAssignedToIdAndStatus(UUID userId, TaskStatus status);
    boolean existsByRequirementIdAndStatusNot(UUID requirementId, TaskStatus status);
}
