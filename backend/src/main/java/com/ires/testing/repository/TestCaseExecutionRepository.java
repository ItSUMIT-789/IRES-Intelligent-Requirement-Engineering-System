package com.ires.testing.repository;

import com.ires.testing.entity.TestCaseExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import com.ires.testing.entity.ExecutionStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TestCaseExecutionRepository extends JpaRepository<TestCaseExecution, UUID> {

    List<TestCaseExecution> findByTestCaseIdOrderByExecutedAtDesc(UUID testCaseId);
    long countByExecutionStatus(ExecutionStatus status);
    long countByExecutedByIdAndExecutionStatus(UUID userId, ExecutionStatus status);
    boolean existsByTestCaseRequirementIdAndExecutionStatus(UUID requirementId, ExecutionStatus status);
    boolean existsByTestCaseRequirementId(UUID requirementId);
    Optional<TestCaseExecution> findFirstByTestCaseIdOrderByExecutedAtDesc(UUID testCaseId);

    @Query("select count(tc) from TestCase tc where tc.assignedTo.id = :userId and not exists " +
            "(select e.id from TestCaseExecution e where e.testCase = tc)")
    long countAssignedWithoutExecution(@Param("userId") UUID userId);

    @Query("select count(e) from TestCaseExecution e where e.testCase.assignedTo.id = :userId " +
            "and e.executionStatus = :status and e.executedAt = " +
            "(select max(latest.executedAt) from TestCaseExecution latest where latest.testCase = e.testCase)")
    long countLatestAssignedByStatus(@Param("userId") UUID userId, @Param("status") ExecutionStatus status);
}
