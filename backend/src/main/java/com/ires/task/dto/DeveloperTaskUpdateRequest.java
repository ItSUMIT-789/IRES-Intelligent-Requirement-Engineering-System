package com.ires.task.dto;

import com.ires.requirement.entity.RequirementPriority;
import com.ires.task.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record DeveloperTaskUpdateRequest(
        UUID requirementId,
        UUID userStoryId,
        @NotBlank @Size(max = 300) String title,
        @Size(max = 10000) String description,
        UUID assignedTo,
        RequirementPriority priority,
        TaskStatus status,
        LocalDate dueDate
) {
}
