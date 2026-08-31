package com.ires.requirement.criteria.dto;

import com.ires.requirement.criteria.entity.CriteriaStatus;
import com.ires.requirement.criteria.entity.CriteriaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AcceptanceCriteriaCreateRequest(
        UUID userStoryId,
        @NotBlank @Size(max = 300) String title,
        @Size(max = 10000) String description,
        CriteriaType criteriaType,
        CriteriaStatus status
) {
}
