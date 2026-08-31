package com.ires.testing.repository;

import com.ires.testing.entity.TestCaseExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import com.ires.testing.entity.ExecutionStatus;

public interface TestCaseExecutionRepository extends JpaRepository<TestCaseExecution, UUID> {

    List<TestCaseExecution> findByTestCaseIdOrderByExecutedAtDesc(UUID testCaseId);
    long countByExecutionStatus(ExecutionStatus status);
    long countByExecutedByIdAndExecutionStatus(UUID userId, ExecutionStatus status);
    boolean existsByTestCaseRequirementIdAndExecutionStatus(UUID requirementId, ExecutionStatus status);
}
