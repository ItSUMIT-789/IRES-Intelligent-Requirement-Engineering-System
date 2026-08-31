package com.ires.requirement.criteria.repository;

import com.ires.requirement.criteria.entity.AcceptanceCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AcceptanceCriteriaRepository extends JpaRepository<AcceptanceCriteria, UUID> {

    Page<AcceptanceCriteria> findByRequirementId(UUID requirementId, Pageable pageable);
}
