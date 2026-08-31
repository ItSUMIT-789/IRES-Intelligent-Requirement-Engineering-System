package com.ires.traceability.dto;

import com.ires.traceability.entity.TraceabilityEntityType;
import com.ires.traceability.entity.TraceabilityLink;

import java.time.Instant;
import java.util.UUID;

public record TraceabilityLinkResponse(
        UUID id,
        UUID requirementId,
        TraceabilityEntityType sourceType,
        UUID sourceId,
        TraceabilityEntityType targetType,
        UUID targetId,
        Instant createdAt
) {

    public static TraceabilityLinkResponse from(TraceabilityLink link) {
        return new TraceabilityLinkResponse(
                link.getId(),
                link.getRequirementId(),
                link.getSourceType(),
                link.getSourceId(),
                link.getTargetType(),
                link.getTargetId(),
                link.getCreatedAt()
        );
    }
}
