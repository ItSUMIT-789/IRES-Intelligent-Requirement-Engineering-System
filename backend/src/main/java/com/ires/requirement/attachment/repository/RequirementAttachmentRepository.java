package com.ires.requirement.attachment.repository;

import com.ires.requirement.attachment.entity.RequirementAttachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RequirementAttachmentRepository extends JpaRepository<RequirementAttachment, UUID> {

    Page<RequirementAttachment> findByRequirementId(UUID requirementId, Pageable pageable);
}