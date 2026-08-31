package com.ires.requirement.clarification.dto;
import jakarta.validation.constraints.*;
public record ClarificationRequest(@NotBlank @Size(max = 10000) String message) {}
