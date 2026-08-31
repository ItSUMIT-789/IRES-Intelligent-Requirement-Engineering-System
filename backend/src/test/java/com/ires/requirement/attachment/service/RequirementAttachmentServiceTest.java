package com.ires.requirement.attachment.service;

import com.ires.project.entity.Project;
import com.ires.project.entity.ProjectStatus;
import com.ires.project.service.ProjectService;
import com.ires.requirement.attachment.dto.AttachmentResponse;
import com.ires.requirement.attachment.entity.RequirementAttachment;
import com.ires.requirement.attachment.repository.RequirementAttachmentRepository;
import com.ires.requirement.entity.Requirement;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.requirement.entity.RequirementStatus;
import com.ires.requirement.entity.RequirementType;
import com.ires.requirement.service.RequirementService;
import com.ires.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequirementAttachmentServiceTest {

    @Mock
    private RequirementAttachmentRepository attachmentRepository;

    @Mock
    private RequirementService requirementService;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserDetails principal;

    @TempDir
    Path temporaryDirectory;

    @Test
    void storesFileAndReturnsMetadataWithoutBinaryContent() throws Exception {
        Requirement requirement = requirement();
        User uploader = user("uploader@example.com");
        UUID requirementId = requirement.getId();
        when(requirementService.findAccessibleRequirement(requirementId, principal)).thenReturn(requirement);
        when(projectService.currentUser(principal)).thenReturn(uploader);
        when(attachmentRepository.save(any(RequirementAttachment.class))).thenAnswer(invocation -> {
            RequirementAttachment attachment = invocation.getArgument(0);
            attachment.setId(UUID.randomUUID());
            return attachment;
        });

        RequirementAttachmentService service = new RequirementAttachmentService(
                attachmentRepository, requirementService, projectService, temporaryDirectory.toString(), 1024);
        MockMultipartFile file = new MockMultipartFile(
                "file", "requirements.pdf", "application/pdf", "requirements".getBytes());

        AttachmentResponse response = service.upload(requirementId, file, principal);

        assertThat(response.originalFileName()).isEqualTo("requirements.pdf");
        assertThat(response.contentType()).isEqualTo("application/pdf");
        assertThat(response.fileSize()).isEqualTo(file.getSize());
        assertThat(Files.list(temporaryDirectory.resolve(requirementId.toString())).count()).isEqualTo(1);
    }

    @Test
    void rejectsUnsupportedContentType() {
        Requirement requirement = requirement();
        when(requirementService.findAccessibleRequirement(requirement.getId(), principal)).thenReturn(requirement);
        when(projectService.currentUser(principal)).thenReturn(user("uploader@example.com"));
        RequirementAttachmentService service = new RequirementAttachmentService(
                attachmentRepository, requirementService, projectService, temporaryDirectory.toString(), 1024);
        MockMultipartFile file = new MockMultipartFile(
                "file", "requirements.pdf", "text/plain", "requirements".getBytes());

        assertThatThrownBy(() -> service.upload(requirement.getId(), file, principal))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void downloadsStoredFileUsingMetadataPath() throws Exception {
        Requirement requirement = requirement();
        User uploader = user("uploader@example.com");
        Path requirementDirectory = temporaryDirectory.resolve(requirement.getId().toString());
        Files.createDirectories(requirementDirectory);
        Path storedFile = requirementDirectory.resolve("stored.pdf");
        Files.writeString(storedFile, "requirements");
        RequirementAttachment attachment = new RequirementAttachment(
                requirement, uploader, "requirements.pdf", "stored.pdf",
                requirement.getId() + "/stored.pdf", "application/pdf", 12);
        UUID attachmentId = UUID.randomUUID();
        attachment.setId(attachmentId);
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
        when(requirementService.findAccessibleRequirement(requirement.getId(), principal)).thenReturn(requirement);

        RequirementAttachmentService service = new RequirementAttachmentService(
                attachmentRepository, requirementService, projectService, temporaryDirectory.toString(), 1024);

        assertThat(service.download(attachmentId, principal).resource().exists()).isTrue();
    }

    private Requirement requirement() {
        User owner = user("owner@example.com");
        Project project = new Project("Checkout", "Revamp", ProjectStatus.ACTIVE, null, null, owner);
        Requirement requirement = new Requirement(project, "Guest checkout", "Details",
                RequirementType.FUNCTIONAL, RequirementPriority.MEDIUM, RequirementStatus.DRAFT,
                "client", owner, null);
        requirement.setId(UUID.randomUUID());
        return requirement;
    }

    private User user(String email) {
        User user = new User("Test", "User", email, "hash", null);
        user.setId(UUID.randomUUID());
        return user;
    }
}
