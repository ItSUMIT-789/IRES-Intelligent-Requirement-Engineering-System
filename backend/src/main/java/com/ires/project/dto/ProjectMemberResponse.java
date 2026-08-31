package com.ires.project.dto;

import com.ires.project.entity.ProjectMember;
import com.ires.project.entity.ProjectMemberRole;

import java.time.Instant;
import java.util.UUID;

public record ProjectMemberResponse(
        UUID id,
        UUID projectId,
        UserSummary user,
        ProjectMemberRole projectRole,
        Instant joinedAt
) {

    public static ProjectMemberResponse from(ProjectMember member) {
        return new ProjectMemberResponse(
                member.getId(),
                member.getProject().getId(),
                UserSummary.from(member.getUser()),
                member.getProjectRole(),
                member.getJoinedAt()
        );
    }
}
