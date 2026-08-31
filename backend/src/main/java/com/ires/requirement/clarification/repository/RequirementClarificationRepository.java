package com.ires.requirement.clarification.repository;

import com.ires.requirement.clarification.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface RequirementClarificationRepository extends JpaRepository<RequirementClarification, UUID> {
    List<RequirementClarification> findByRequirementIdOrderByRequestedAtDesc(UUID requirementId);
    Optional<RequirementClarification> findByRequirementIdAndStatus(UUID requirementId, ClarificationStatus status);
}
