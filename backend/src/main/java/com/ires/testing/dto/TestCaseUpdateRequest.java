package com.ires.testing.dto;

import com.ires.requirement.entity.RequirementPriority;
import com.ires.testing.entity.TestCaseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TestCaseUpdateRequest(
        UUID requirementId,
        UUID userStoryId,
        @NotBlank @Size(max = 300) String title,
        @Size(max = 10000) String description,
        @Size(max = 10000) String preconditions,
        @NotBlank @Size(max = 10000) String expectedResult,
        RequirementPriority priority,
        TestCaseStatus status,
        UUID assignedTo
) {
}
