package com.ires.requirement.clarification.dto;

import com.ires.project.dto.UserSummary;
import com.ires.requirement.clarification.entity.*;
import java.time.Instant;
import java.util.UUID;

public record ClarificationResponse(UUID id, UUID requirementId, UserSummary requestedBy, String question,
                                    Instant requestedAt, String response, UserSummary respondedBy,
                                    Instant respondedAt, ClarificationStatus status) {
    public static ClarificationResponse from(RequirementClarification value) {
        return new ClarificationResponse(value.getId(), value.getRequirement().getId(), UserSummary.from(value.getRequestedBy()),
                value.getQuestion(), value.getRequestedAt(), value.getResponse(),
                value.getRespondedBy() == null ? null : UserSummary.from(value.getRespondedBy()), value.getRespondedAt(), value.getStatus());
    }
}
