package com.ires.requirement.criteria.dto;

import com.ires.requirement.criteria.entity.AcceptanceCriteria;
import com.ires.requirement.criteria.entity.CriteriaStatus;
import com.ires.requirement.criteria.entity.CriteriaType;

import java.time.Instant;
import java.util.UUID;

public record AcceptanceCriteriaResponse(
        UUID id,
        UUID requirementId,
        UUID userStoryId,
        String title,
        String description,
        CriteriaType criteriaType,
        CriteriaStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static AcceptanceCriteriaResponse from(AcceptanceCriteria criteria) {
        return new AcceptanceCriteriaResponse(
                criteria.getId(),
                criteria.getRequirement().getId(),
                criteria.getUserStory() == null ? null : criteria.getUserStory().getId(),
                criteria.getTitle(),
                criteria.getDescription(),
                criteria.getCriteriaType(),
                criteria.getStatus(),
                criteria.getCreatedAt(),
                criteria.getUpdatedAt()
        );
    }
}
