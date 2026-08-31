package com.ires.bug.dto;

import com.ires.bug.entity.BugSeverity;
import com.ires.bug.entity.BugStatus;
import com.ires.requirement.entity.RequirementPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record BugCreateRequest(
        UUID requirementId,
        UUID testCaseId,
        @NotBlank @Size(max = 300) String title,
        @NotBlank @Size(max = 10000) String description,
        BugSeverity severity,
        RequirementPriority priority,
        BugStatus status,
        UUID assignedTo
) {
}
