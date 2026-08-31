package com.ires.requirement.attachment.service;

import com.ires.common.exception.FileStorageException;
import com.ires.common.exception.ForbiddenException;
import com.ires.common.exception.NotFoundException;
import com.ires.project.service.ProjectService;
import com.ires.requirement.attachment.dto.AttachmentResponse;
import com.ires.requirement.attachment.entity.RequirementAttachment;
import com.ires.requirement.attachment.repository.RequirementAttachmentRepository;
import com.ires.requirement.entity.Requirement;
import com.ires.requirement.service.RequirementService;
import com.ires.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RequirementAttachmentService {

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            ".pdf", "application/pdf",
            ".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            ".csv", "text/csv"
    );

    private final RequirementAttachmentRepository attachmentRepository;
    private final RequirementService requirementService;
    private final ProjectService projectService;
    private final Path uploadRoot;
    private final long maxFileSizeBytes;

    public RequirementAttachmentService(
            RequirementAttachmentRepository attachmentRepository,
            RequirementService requirementService,
            ProjectService projectService,
            @Value("${app.storage.upload-dir}") String uploadDir,
            @Value("${app.attachments.max-file-size-bytes}") long maxFileSizeBytes
    ) {
        this.attachmentRepository = attachmentRepository;
        this.requirementService = requirementService;
        this.projectService = projectService;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    @Transactional
    public AttachmentResponse upload(UUID requirementId, MultipartFile file, UserDetails principal) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("An attachment file is required.");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new IllegalArgumentException("Attachment exceeds the maximum allowed size.");
        }

        Requirement requirement = requirementService.findAccessibleRequirement(requirementId, principal);
        User uploader = projectService.currentUser(principal);
        String originalFileName = safeOriginalFileName(file.getOriginalFilename());
        String extension = extensionOf(originalFileName);
        String contentType = file.getContentType();
        validateType(extension, contentType);

        String storedFileName = UUID.randomUUID() + "_" + originalFileName;
        Path requirementDirectory = uploadRoot.resolve(requirementId.toString()).normalize();
        Path target = requirementDirectory.resolve(storedFileName).normalize();
        ensureWithinUploadRoot(target);

        try {
            Files.createDirectories(requirementDirectory);
            Files.copy(file.getInputStream(), target);
            String relativePath = uploadRoot.relativize(target).toString().replace(java.io.File.separatorChar, '/');
            RequirementAttachment attachment = new RequirementAttachment(
                    requirement,
                    uploader,
                    originalFileName,
                    storedFileName,
                    relativePath,
                    contentType,
                    file.getSize()
            );
            return AttachmentResponse.from(attachmentRepository.save(attachment));
        } catch (IOException exception) {
            deleteQuietly(target);
            throw new FileStorageException("Unable to store attachment.", exception);
        } catch (RuntimeException exception) {
            deleteQuietly(target);
            throw exception;
        }
    }

    public Page<AttachmentResponse> list(UUID requirementId, Pageable pageable, UserDetails principal) {
        requirementService.findAccessibleRequirement(requirementId, principal);
        Pageable newestFirst = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return attachmentRepository.findByRequirementId(requirementId, newestFirst).map(AttachmentResponse::from);
    }

    public AttachmentDownload download(UUID id, UserDetails principal) {
        RequirementAttachment attachment = findAttachment(id);
        requirementService.findAccessibleRequirement(attachment.getRequirement().getId(), principal);
        Path file = resolveStoredPath(attachment.getFilePath());
        try {
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new NotFoundException("Attachment file not found.");
            }
            return new AttachmentDownload(resource, attachment.getOriginalFileName(), attachment.getContentType());
        } catch (IOException exception) {
            throw new FileStorageException("Unable to read attachment.", exception);
        }
    }

    @Transactional
    public void delete(UUID id, UserDetails principal) {
        RequirementAttachment attachment = findAttachment(id);
        requirementService.findAccessibleRequirement(attachment.getRequirement().getId(), principal);
        assertCanDelete(attachment, principal);
        attachmentRepository.delete(attachment);
        deleteQuietly(resolveStoredPath(attachment.getFilePath()));
    }

    private RequirementAttachment findAttachment(UUID id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Attachment not found."));
    }

    private void assertCanDelete(RequirementAttachment attachment, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }
        User currentUser = projectService.currentUser(principal);
        if (!attachment.getUploadedBy().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only the uploader or an admin can delete this attachment.");
        }
    }

    private boolean isAdmin(UserDetails principal) {
        return principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private String safeOriginalFileName(String originalFileName) {
        if (!StringUtils.hasText(originalFileName)) {
            throw new IllegalArgumentException("Attachment filename is required.");
        }
        String filename = originalFileName.replace('\\', '/');
        filename = Paths.get(filename).getFileName().toString();
        String cleaned = StringUtils.cleanPath(filename).replaceAll("[^a-zA-Z0-9._-]", "_");
        if (!StringUtils.hasText(cleaned) || cleaned.equals(".") || cleaned.equals("..") || cleaned.contains("..")) {
            throw new IllegalArgumentException("Attachment filename is invalid.");
        }
        return cleaned;
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0) {
            throw new IllegalArgumentException("Attachment must have a supported file extension.");
        }
        return filename.substring(dot).toLowerCase(Locale.ROOT);
    }

    private void validateType(String extension, String contentType) {
        String expectedType = ALLOWED_TYPES.get(extension);
        if (expectedType == null || !expectedType.equalsIgnoreCase(contentType)) {
            throw new IllegalArgumentException("Attachment type is not supported.");
        }
    }

    private Path resolveStoredPath(String relativePath) {
        Path resolved = uploadRoot.resolve(relativePath).normalize();
        ensureWithinUploadRoot(resolved);
        return resolved;
    }

    private void ensureWithinUploadRoot(Path path) {
        if (!path.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Attachment path is invalid.");
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    public record AttachmentDownload(Resource resource, String originalFileName, String contentType) {
    }
}
