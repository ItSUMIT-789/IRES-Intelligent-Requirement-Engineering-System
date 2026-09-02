package com.ires.requirement.service;

import com.ires.common.exception.ForbiddenException;
import com.ires.common.exception.NotFoundException;
import com.ires.project.entity.Project;
import com.ires.project.entity.ProjectStatus;
import com.ires.project.service.ProjectService;
import com.ires.requirement.dto.RequirementCreateRequest;
import com.ires.requirement.dto.RequirementUpdateRequest;
import com.ires.requirement.entity.Requirement;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.requirement.entity.RequirementStatus;
import com.ires.requirement.entity.RequirementType;
import com.ires.requirement.repository.RequirementRepository;
import com.ires.user.entity.User;
import com.ires.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class RequirementServiceTest {

    @Mock
    private RequirementRepository requirementRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails principal;

    @InjectMocks
    private RequirementService requirementService;

    @Test
    void createsRequirementForAVisibleProject() {
        UUID projectId = UUID.randomUUID();
        User creator = user("creator@example.com");
        Project project = new Project("Checkout", "Revamp", ProjectStatus.ACTIVE, null, null, creator);
        when(projectService.findProject(projectId)).thenReturn(project);
        when(projectService.currentUser(principal)).thenReturn(creator);
        when(requirementRepository.save(any(Requirement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RequirementCreateRequest request = new RequirementCreateRequest(
                "Guest checkout", "Allow checkout without registration.", RequirementType.FUNCTIONAL,
                RequirementPriority.HIGH, RequirementStatus.SUBMITTED, "client", null);

        var response = requirementService.create(projectId, request, principal);

        assertThat(response.title()).isEqualTo("Guest checkout");
        assertThat(response.status()).isEqualTo(RequirementStatus.DRAFT);
        assertThat(response.createdBy().email()).isEqualTo("creator@example.com");
    }

    @Test
    void ignoresAssignmentFieldsDuringCreation() {
        UUID projectId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();
        User creator = user("creator@example.com");
        when(projectService.findProject(projectId)).thenReturn(new Project(
                "Checkout", "Revamp", ProjectStatus.ACTIVE, null, null, creator));
        when(projectService.currentUser(principal)).thenReturn(creator);
        when(requirementRepository.save(any(Requirement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RequirementCreateRequest request = new RequirementCreateRequest(
                "Guest checkout", null, null, null, null, null, assigneeId);

        var response = requirementService.create(projectId, request, principal);

        assertThat(response.assignedTo()).isNull();
        assertThat(response.status()).isEqualTo(RequirementStatus.DRAFT);
    }

    @Test
    void preventsUnrelatedUserFromUpdatingRequirement() {
        UUID requirementId = UUID.randomUUID();
        User owner = user("owner@example.com");
        User outsider = user("outsider@example.com");
        Project project = new Project("Checkout", "Revamp", ProjectStatus.ACTIVE, null, null, owner);
        Requirement requirement = new Requirement(project, "Guest checkout", "Details",
                RequirementType.FUNCTIONAL, RequirementPriority.MEDIUM, RequirementStatus.DRAFT,
                "client", owner, null);
        when(requirementRepository.findById(requirementId)).thenReturn(Optional.of(requirement));
        when(projectService.isAdmin(principal)).thenReturn(false);
        when(projectService.currentUser(principal)).thenReturn(outsider);

        RequirementUpdateRequest request = new RequirementUpdateRequest(
                "Changed", "Details", RequirementType.FUNCTIONAL, RequirementPriority.HIGH,
                RequirementStatus.SUBMITTED, "client", null);

        assertThatThrownBy(() -> requirementService.update(requirementId, request, principal))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void preventsOwnerFromChangingStatusThroughGenericUpdate() {
        UUID requirementId = UUID.randomUUID();
        User owner = user("owner@example.com");
        Project project = new Project("Checkout", "Revamp", ProjectStatus.ACTIVE, null, null, owner);
        Requirement requirement = new Requirement(project, "Guest checkout", "Details",
                RequirementType.FUNCTIONAL, RequirementPriority.MEDIUM, RequirementStatus.DRAFT,
                "client", owner, null);
        when(requirementRepository.findById(requirementId)).thenReturn(Optional.of(requirement));
        when(projectService.isAdmin(principal)).thenReturn(false);
        when(projectService.currentUser(principal)).thenReturn(owner);

        RequirementUpdateRequest request = new RequirementUpdateRequest(
                "Guest checkout", "Details", RequirementType.FUNCTIONAL, RequirementPriority.MEDIUM,
                RequirementStatus.COMPLETED, "client", null);

        assertThatThrownBy(() -> requirementService.update(requirementId, request, principal))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("workflow action");
        assertThat(requirement.getStatus()).isEqualTo(RequirementStatus.DRAFT);
    }

    @Test
    void clientCannotCreateRequirementForAnotherClientsProject() {
        UUID projectId = UUID.randomUUID();
        User owner = user("owner@example.com");
        Project project = new Project("Private", "Details", ProjectStatus.ACTIVE, null, null, owner);
        when(projectService.findProject(projectId)).thenReturn(project);
        doThrow(new ForbiddenException("Only the project client or an admin can manage this project."))
                .when(projectService).assertCanManage(project, principal);
        RequirementCreateRequest request = new RequirementCreateRequest(
                "Spoofed", null, RequirementType.FUNCTIONAL, RequirementPriority.MEDIUM, null, null, null);

        assertThatThrownBy(() -> requirementService.create(projectId, request, principal))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void clientCanEditOwnDraftWithoutChangingLifecycle() {
        UUID requirementId = UUID.randomUUID();
        User owner = user("owner@example.com");
        Project project = new Project("Checkout", "Revamp", ProjectStatus.ACTIVE, null, null, owner);
        Requirement requirement = new Requirement(project, "Original", "Details", RequirementType.FUNCTIONAL,
                RequirementPriority.MEDIUM, RequirementStatus.DRAFT, "client", owner, null);
        when(requirementRepository.findById(requirementId)).thenReturn(Optional.of(requirement));
        when(projectService.currentUser(principal)).thenReturn(owner);

        var response = requirementService.update(requirementId, new RequirementUpdateRequest(
                "Edited", "Updated", RequirementType.BUSINESS, RequirementPriority.HIGH,
                RequirementStatus.DRAFT, "client", null), principal);

        assertThat(response.title()).isEqualTo("Edited");
        assertThat(response.status()).isEqualTo(RequirementStatus.DRAFT);
    }

    private User user(String email) {
        User user = new User("Test", "User", email, "hash", null);
        user.setId(UUID.randomUUID());
        return user;
    }
}
