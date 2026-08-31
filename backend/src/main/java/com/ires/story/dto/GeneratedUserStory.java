package com.ires.story.dto;

import com.ires.requirement.entity.RequirementPriority;

public record GeneratedUserStory(
        String title,
        String description,
        String storyText,
        RequirementPriority priority
) {
}
