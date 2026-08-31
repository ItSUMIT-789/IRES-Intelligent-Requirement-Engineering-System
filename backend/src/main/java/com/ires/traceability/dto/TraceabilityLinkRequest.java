package com.ires.traceability.dto;

import com.ires.traceability.entity.TraceabilityEntityType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TraceabilityLinkRequest(
        @NotNull TraceabilityEntityType sourceType,
        @NotNull UUID sourceId,
        @NotNull TraceabilityEntityType targetType,
        @NotNull UUID targetId
) {
}
