package com.ires.task.service;

import com.ires.common.exception.BadRequestException;
import com.ires.common.exception.NotFoundException;
import com.ires.project.entity.Project;
import com.ires.project.entity.ProjectStatus;
import com.ires.project.service.ProjectService;
import com.ires.requirement.entity.Requirement;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.requirement.entity.RequirementStatus;
import com.ires.requirement.entity.RequirementType;
import com.ires.requirement.service.RequirementService;
import com.ires.story.repository.UserStoryRepository;
import com.ires.task.dto.DeveloperTaskCreateRequest;
import com.ires.task.entity.DeveloperTask;
import com.ires.task.entity.TaskStatus;
import com.ires.task.repository.DeveloperTaskRepository;
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
class DeveloperTaskServiceTest {

    @Mock
    private DeveloperTaskRepository taskRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private RequirementService requirementService;

    @Mock
    private UserStoryRepository userStoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails principal;

    @InjectMocks
    private DeveloperTaskService taskService;

    @Test
    void createsTaskForProjectManager() {
        Project project = project();
        when(projectService.findProject(project.getId())).thenReturn(project);
        when(projectService.currentUser(principal)).thenReturn(project.getClient());
        when(taskRepository.save(any(DeveloperTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = taskService.create(project.getId(), new DeveloperTaskCreateRequest(
                null, null, "Implement checkout", "Build flow", null,
                RequirementPriority.HIGH, TaskStatus.TODO, null), principal);

        assertThat(response.title()).isEqualTo("Implement checkout");
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
        assertThat(response.createdBy().email()).isEqualTo("owner@example.com");
    }

    @Test
    void rejectsRequirementFromAnotherProject() {
        Project project = project();
        Requirement requirement = requirement();
        when(projectService.findProject(project.getId())).thenReturn(project);
        when(projectService.currentUser(principal)).thenReturn(project.getClient());
        when(requirementService.findAccessibleRequirement(requirement.getId(), principal)).thenReturn(requirement);

        assertThatThrownBy(() -> taskService.create(project.getId(), new DeveloperTaskCreateRequest(
                requirement.getId(), null, "Task", null, null, null, null, null), principal))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsMissingAssignee() {
        Project project = project();
        UUID assigneeId = UUID.randomUUID();
        when(projectService.findProject(project.getId())).thenReturn(project);
        when(projectService.currentUser(principal)).thenReturn(project.getClient());
        when(userRepository.findById(assigneeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.create(project.getId(), new DeveloperTaskCreateRequest(
                null, null, "Task", null, assigneeId, null, null, null), principal))
                .isInstanceOf(NotFoundException.class);
    }

    private Project project() {
        User owner = user("owner@example.com");
        return new Project("Checkout", "Revamp", ProjectStatus.ACTIVE, null, null, owner);
    }

    private Requirement requirement() {
        User owner = user("other@example.com");
        Project project = new Project("Other", "Project", ProjectStatus.ACTIVE, null, null, owner);
        Requirement requirement = new Requirement(project, "Other requirement", "Details",
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
