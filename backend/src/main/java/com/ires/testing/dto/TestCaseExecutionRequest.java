package com.ires.testing.dto;

import com.ires.testing.entity.ExecutionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TestCaseExecutionRequest(
        @NotNull ExecutionStatus executionStatus,
        @Size(max = 10000) String actualResult,
        @Size(max = 10000) String notes
) {
}
