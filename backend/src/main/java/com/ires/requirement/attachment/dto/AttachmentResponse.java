package com.ires.requirement.attachment.dto;

import com.ires.project.dto.UserSummary;
import com.ires.requirement.attachment.entity.RequirementAttachment;

import java.time.Instant;
import java.util.UUID;

public record AttachmentResponse(
        UUID id,
        UUID requirementId,
        UserSummary uploadedBy,
        String originalFileName,
        String contentType,
        long fileSize,
        Instant createdAt
) {

    public static AttachmentResponse from(RequirementAttachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getRequirement().getId(),
                UserSummary.from(attachment.getUploadedBy()),
                attachment.getOriginalFileName(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getCreatedAt()
        );
    }
}