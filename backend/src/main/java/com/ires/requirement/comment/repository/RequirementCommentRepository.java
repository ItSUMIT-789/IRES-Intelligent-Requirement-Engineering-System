package com.ires.requirement.comment.repository;

import com.ires.requirement.comment.entity.RequirementComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RequirementCommentRepository extends JpaRepository<RequirementComment, UUID> {

    Page<RequirementComment> findByRequirementId(UUID requirementId, Pageable pageable);
}
