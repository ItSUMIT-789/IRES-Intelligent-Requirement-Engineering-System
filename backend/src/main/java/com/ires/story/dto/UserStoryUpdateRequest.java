package com.ires.story.dto;

import com.ires.requirement.entity.RequirementPriority;
import com.ires.story.entity.StoryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserStoryUpdateRequest(
        @NotBlank @Size(max = 300) String title,
        @Size(max = 10000) String description,
        @NotBlank @Size(max = 10000) String storyText,
        RequirementPriority priority,
        StoryStatus status
) {
}
