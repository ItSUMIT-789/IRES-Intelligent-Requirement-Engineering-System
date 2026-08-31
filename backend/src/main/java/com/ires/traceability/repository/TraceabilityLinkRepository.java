package com.ires.traceability.repository;

import com.ires.traceability.entity.TraceabilityLink;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TraceabilityLinkRepository extends JpaRepository<TraceabilityLink, UUID> {

    Page<TraceabilityLink> findByRequirementId(UUID requirementId, Pageable pageable);

    boolean existsByRequirementIdAndSourceTypeAndSourceIdAndTargetTypeAndTargetId(
            UUID requirementId,
            com.ires.traceability.entity.TraceabilityEntityType sourceType,
            UUID sourceId,
            com.ires.traceability.entity.TraceabilityEntityType targetType,
            UUID targetId
    );
}
