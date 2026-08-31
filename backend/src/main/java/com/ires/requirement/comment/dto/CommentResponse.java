package com.ires.requirement.comment.dto;

import com.ires.project.dto.UserSummary;
import com.ires.requirement.comment.entity.RequirementComment;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID requirementId,
        UserSummary author,
        String comment,
        Instant createdAt,
        Instant updatedAt
) {

    public static CommentResponse from(RequirementComment requirementComment) {
        return new CommentResponse(
                requirementComment.getId(),
                requirementComment.getRequirement().getId(),
                UserSummary.from(requirementComment.getUser()),
                requirementComment.getComment(),
                requirementComment.getCreatedAt(),
                requirementComment.getUpdatedAt()
        );
    }
}
