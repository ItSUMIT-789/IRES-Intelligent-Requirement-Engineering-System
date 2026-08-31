package com.ires.requirement.dto;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record RequirementAssignmentRequest(@NotNull UUID developerId) {}
