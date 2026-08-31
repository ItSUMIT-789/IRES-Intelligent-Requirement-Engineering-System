package com.ires.bug.repository;

import com.ires.bug.entity.Bug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;
import com.ires.bug.entity.BugStatus;
import java.util.Collection;

public interface BugRepository extends JpaRepository<Bug, UUID>, JpaSpecificationExecutor<Bug> {
    long countByStatusIn(Collection<BugStatus> statuses);
    long countByAssignedToIdAndStatusIn(UUID userId, Collection<BugStatus> statuses);
    long countByReportedByIdAndStatusIn(UUID userId, Collection<BugStatus> statuses);
    boolean existsByRequirementIdAndStatusIn(UUID requirementId, Collection<BugStatus> statuses);
}
