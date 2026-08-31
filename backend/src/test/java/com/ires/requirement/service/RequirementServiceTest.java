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
        assertThat(response.status()).isEqualTo(RequirementStatus.SUBMITTED);
        assertThat(response.createdBy().email()).isEqualTo("creator@example.com");
    }

    @Test
    void rejectsMissingAssignee() {
        UUID projectId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();
        User creator = user("creator@example.com");
        when(projectService.findProject(projectId)).thenReturn(new Project(
                "Checkout", "Revamp", ProjectStatus.ACTIVE, null, null, creator));
        when(projectService.currentUser(principal)).thenReturn(creator);
        when(userRepository.findById(assigneeId)).thenReturn(Optional.empty());

        RequirementCreateRequest request = new RequirementCreateRequest(
                "Guest checkout", null, null, null, null, null, assigneeId);

        assertThatThrownBy(() -> requirementService.create(projectId, request, principal))
                .isInstanceOf(NotFoundException.class);
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

    private User user(String email) {
        User user = new User("Test", "User", email, "hash", null);
        user.setId(UUID.randomUUID());
        return user;
    }
}
