package com.ires.testing.dto;

import com.ires.project.dto.UserSummary;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.testing.entity.TestCase;
import com.ires.testing.entity.TestCaseStatus;

import java.time.Instant;
import java.util.UUID;

public record TestCaseResponse(
        UUID id,
        UUID projectId,
        UUID requirementId,
        UUID userStoryId,
        String title,
        String description,
        String preconditions,
        String expectedResult,
        RequirementPriority priority,
        TestCaseStatus status,
        UserSummary createdBy,
        UserSummary assignedTo,
        Instant createdAt,
        Instant updatedAt
) {

    public static TestCaseResponse from(TestCase testCase) {
        return new TestCaseResponse(
                testCase.getId(),
                testCase.getProject().getId(),
                testCase.getRequirement() == null ? null : testCase.getRequirement().getId(),
                testCase.getUserStory() == null ? null : testCase.getUserStory().getId(),
                testCase.getTitle(),
                testCase.getDescription(),
                testCase.getPreconditions(),
                testCase.getExpectedResult(),
                testCase.getPriority(),
                testCase.getStatus(),
                UserSummary.from(testCase.getCreatedBy()),
                testCase.getAssignedTo() == null ? null : UserSummary.from(testCase.getAssignedTo()),
                testCase.getCreatedAt(),
                testCase.getUpdatedAt()
        );
    }
}
