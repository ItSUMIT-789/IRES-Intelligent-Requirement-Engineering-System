package com.ires.bug.dto;

import com.ires.bug.entity.Bug;
import com.ires.bug.entity.BugSeverity;
import com.ires.bug.entity.BugStatus;
import com.ires.project.dto.UserSummary;
import com.ires.requirement.entity.RequirementPriority;

import java.time.Instant;
import java.util.UUID;

public record BugResponse(
        UUID id,
        UUID projectId,
        UUID requirementId,
        UUID testCaseId,
        String title,
        String description,
        BugSeverity severity,
        RequirementPriority priority,
        BugStatus status,
        UserSummary reportedBy,
        UserSummary assignedTo,
        Instant createdAt,
        Instant updatedAt,
        Instant resolvedAt
) {

    public static BugResponse from(Bug bug) {
        return new BugResponse(
                bug.getId(),
                bug.getProject().getId(),
                bug.getRequirement() == null ? null : bug.getRequirement().getId(),
                bug.getTestCase() == null ? null : bug.getTestCase().getId(),
                bug.getTitle(),
                bug.getDescription(),
                bug.getSeverity(),
                bug.getPriority(),
                bug.getStatus(),
                UserSummary.from(bug.getReportedBy()),
                bug.getAssignedTo() == null ? null : UserSummary.from(bug.getAssignedTo()),
                bug.getCreatedAt(),
                bug.getUpdatedAt(),
                bug.getResolvedAt()
        );
    }
}
