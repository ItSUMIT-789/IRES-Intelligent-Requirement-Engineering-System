package com.ires.project.dto;

import com.ires.project.entity.Project;
import com.ires.project.entity.ProjectStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        ProjectStatus status,
        LocalDate startDate,
        LocalDate endDate,
        UserSummary client,
        Instant createdAt,
        Instant updatedAt
) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getStartDate(),
                project.getEndDate(),
                UserSummary.from(project.getClient()),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
