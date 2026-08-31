package com.ires.story.dto;

import com.ires.project.dto.UserSummary;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.story.entity.StoryStatus;
import com.ires.story.entity.UserStory;

import java.time.Instant;
import java.util.UUID;

public record UserStoryResponse(
        UUID id,
        UUID requirementId,
        String title,
        String description,
        String storyText,
        RequirementPriority priority,
        StoryStatus status,
        UserSummary createdBy,
        Instant createdAt,
        Instant updatedAt
) {

    public static UserStoryResponse from(UserStory story) {
        return new UserStoryResponse(
                story.getId(),
                story.getRequirement().getId(),
                story.getTitle(),
                story.getDescription(),
                story.getStoryText(),
                story.getPriority(),
                story.getStatus(),
                UserSummary.from(story.getCreatedBy()),
                story.getCreatedAt(),
                story.getUpdatedAt()
        );
    }
}
