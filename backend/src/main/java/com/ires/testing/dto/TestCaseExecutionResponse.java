package com.ires.testing.dto;

import com.ires.project.dto.UserSummary;
import com.ires.testing.entity.ExecutionStatus;
import com.ires.testing.entity.TestCaseExecution;

import java.time.Instant;
import java.util.UUID;

public record TestCaseExecutionResponse(
        UUID id,
        UUID testCaseId,
        UserSummary executedBy,
        ExecutionStatus executionStatus,
        String actualResult,
        Instant executedAt,
        String notes
) {

    public static TestCaseExecutionResponse from(TestCaseExecution execution) {
        return new TestCaseExecutionResponse(
                execution.getId(),
                execution.getTestCase().getId(),
                UserSummary.from(execution.getExecutedBy()),
                execution.getExecutionStatus(),
                execution.getActualResult(),
                execution.getExecutedAt(),
                execution.getNotes()
        );
    }
}
