package com.ires.requirement.attachment.controller;

import com.ires.common.response.ApiResponse;
import com.ires.requirement.attachment.dto.AttachmentResponse;
import com.ires.requirement.attachment.service.RequirementAttachmentService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RequirementAttachmentController {

    private final RequirementAttachmentService attachmentService;

    @PostMapping(value = "/api/v1/requirements/{requirementId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'BUSINESS_ANALYST')")
    public ResponseEntity<ApiResponse<AttachmentResponse>> upload(
            @PathVariable UUID requirementId,
            @RequestPart("file") @NotNull MultipartFile file,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Attachment uploaded.", attachmentService.upload(requirementId, file, principal)));
    }

    @GetMapping("/api/v1/requirements/{requirementId}/attachments")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<AttachmentResponse>> list(
            @PathVariable UUID requirementId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("Attachments loaded.", attachmentService.list(requirementId, pageable, principal));
    }

    @GetMapping("/api/v1/attachments/{id}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> download(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        var download = attachmentService.download(id, principal);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(download.originalFileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(download.resource());
    }

    @DeleteMapping("/api/v1/attachments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'BUSINESS_ANALYST')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        attachmentService.delete(id, principal);
        return ResponseEntity.noContent().build();
    }
}
