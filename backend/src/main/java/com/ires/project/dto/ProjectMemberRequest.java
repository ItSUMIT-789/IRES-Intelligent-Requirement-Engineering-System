package com.ires.project.dto;

import com.ires.project.entity.ProjectMemberRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProjectMemberRequest(
        @NotNull UUID userId,
        @NotNull ProjectMemberRole projectRole
) {
}
