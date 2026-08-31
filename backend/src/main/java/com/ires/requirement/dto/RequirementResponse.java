package com.ires.requirement.dto;

import com.ires.project.dto.UserSummary;
import com.ires.requirement.entity.Requirement;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.requirement.entity.RequirementStatus;
import com.ires.requirement.entity.RequirementType;

import java.time.Instant;
import java.util.UUID;

public record RequirementResponse(
        UUID id,
        UUID projectId,
        String title,
        String description,
        RequirementType requirementType,
        RequirementPriority priority,
        RequirementStatus status,
        String source,
        UserSummary createdBy,
        UserSummary assignedTo,
        Instant createdAt,
        Instant updatedAt
) {

    public static RequirementResponse from(Requirement requirement) {
        return new RequirementResponse(
                requirement.getId(),
                requirement.getProject().getId(),
                requirement.getTitle(),
                requirement.getDescription(),
                requirement.getRequirementType(),
                requirement.getPriority(),
                requirement.getStatus(),
                requirement.getSource(),
                UserSummary.from(requirement.getCreatedBy()),
                requirement.getAssignedTo() == null ? null : UserSummary.from(requirement.getAssignedTo()),
                requirement.getCreatedAt(),
                requirement.getUpdatedAt()
        );
    }
}
