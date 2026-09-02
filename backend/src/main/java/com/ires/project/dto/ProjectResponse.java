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
        long requirementCount,
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
                0,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    public static ProjectResponse from(Project project, long requirementCount) {
        ProjectResponse response = from(project);
        return new ProjectResponse(response.id(), response.name(), response.description(), response.status(),
                response.startDate(), response.endDate(), response.client(), requirementCount,
                response.createdAt(), response.updatedAt());
    }
}
