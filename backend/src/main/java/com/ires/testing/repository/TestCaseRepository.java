package com.ires.testing.repository;

import com.ires.testing.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;
import java.util.List;
import com.ires.testing.entity.TestCaseStatus;
import java.util.Collection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TestCaseRepository extends JpaRepository<TestCase, UUID>, JpaSpecificationExecutor<TestCase> {
    long countByStatusIn(Collection<TestCaseStatus> statuses);
    long countByAssignedToId(UUID userId);
    long countByAssignedToIdAndStatusIn(UUID userId, Collection<TestCaseStatus> statuses);

    @Query("select count(distinct t) from TestCase t left join t.project.members m " +
            "where (t.project.client.id = :userId or m.user.id = :userId) and t.status in :statuses")
    long countAccessibleToByStatusIn(@Param("userId") UUID userId, @Param("statuses") Collection<TestCaseStatus> statuses);
    boolean existsByRequirementIdAndAssignedToId(UUID requirementId, UUID userId);
    List<TestCase> findByRequirementId(UUID requirementId);
}
