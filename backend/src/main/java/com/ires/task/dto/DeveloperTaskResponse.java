package com.ires.task.dto;

import com.ires.project.dto.UserSummary;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.task.entity.DeveloperTask;
import com.ires.task.entity.TaskStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DeveloperTaskResponse(
        UUID id,
        UUID projectId,
        UUID requirementId,
        UUID userStoryId,
        String title,
        String description,
        UserSummary assignedTo,
        RequirementPriority priority,
        TaskStatus status,
        LocalDate dueDate,
        UserSummary createdBy,
        Instant createdAt,
        Instant updatedAt
) {

    public static DeveloperTaskResponse from(DeveloperTask task) {
        return new DeveloperTaskResponse(
                task.getId(),
                task.getProject().getId(),
                task.getRequirement() == null ? null : task.getRequirement().getId(),
                task.getUserStory() == null ? null : task.getUserStory().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getAssignedTo() == null ? null : UserSummary.from(task.getAssignedTo()),
                task.getPriority(),
                task.getStatus(),
                task.getDueDate(),
                UserSummary.from(task.getCreatedBy()),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
