package com.ires.requirement.dto;

import com.ires.requirement.entity.RequirementPriority;
import com.ires.requirement.entity.RequirementStatus;
import com.ires.requirement.entity.RequirementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RequirementUpdateRequest(
        @NotBlank @Size(max = 300) String title,
        @Size(max = 10000) String description,
        RequirementType requirementType,
        RequirementPriority priority,
        RequirementStatus status,
        @Size(max = 100) String source,
        UUID assignedTo
) {
}
