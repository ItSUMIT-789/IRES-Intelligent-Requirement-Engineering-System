package com.ires.traceability.service;

import com.ires.project.entity.Project;
import com.ires.project.entity.ProjectStatus;
import com.ires.project.service.ProjectService;
import com.ires.requirement.entity.Requirement;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.requirement.entity.RequirementStatus;
import com.ires.requirement.entity.RequirementType;
import com.ires.requirement.repository.RequirementRepository;
import com.ires.requirement.service.RequirementService;
import com.ires.story.repository.UserStoryRepository;
import com.ires.task.entity.DeveloperTask;
import com.ires.task.entity.TaskStatus;
import com.ires.task.repository.DeveloperTaskRepository;
import com.ires.traceability.dto.TraceabilityLinkRequest;
import com.ires.traceability.entity.TraceabilityEntityType;
import com.ires.traceability.entity.TraceabilityLink;
import com.ires.traceability.repository.TraceabilityLinkRepository;
import com.ires.user.entity.User;
import com.ires.requirement.criteria.repository.AcceptanceCriteriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraceabilityTaskLinkTest {

    @Mock
    private TraceabilityLinkRepository linkRepository;
    @Mock
    private RequirementService requirementService;
    @Mock
    private ProjectService projectService;
    @Mock
    private RequirementRepository requirementRepository;
    @Mock
    private UserStoryRepository userStoryRepository;
    @Mock
    private AcceptanceCriteriaRepository criteriaRepository;
    @Mock
    private DeveloperTaskRepository developerTaskRepository;
    @Mock
    private UserDetails principal;

    @InjectMocks
    private TraceabilityService traceabilityService;

    @Test
    void createsRequirementToDeveloperTaskLink() {
        Requirement requirement = requirement();
        DeveloperTask task = new DeveloperTask(requirement.getProject(), requirement, null, "Implement checkout",
                "Details", null, RequirementPriority.MEDIUM, TaskStatus.TODO, null, requirement.getCreatedBy());
        UUID taskId = UUID.randomUUID();
        task.setId(taskId);
        when(requirementService.findAccessibleRequirement(requirement.getId(), principal)).thenReturn(requirement);
        when(developerTaskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(requirementRepository.findById(requirement.getId())).thenReturn(Optional.of(requirement));
        when(linkRepository.existsByRequirementIdAndSourceTypeAndSourceIdAndTargetTypeAndTargetId(
                any(), any(), any(), any(), any())).thenReturn(false);
        when(linkRepository.save(any(TraceabilityLink.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = traceabilityService.create(requirement.getId(), new TraceabilityLinkRequest(
                TraceabilityEntityType.REQUIREMENT, requirement.getId(),
                TraceabilityEntityType.DEVELOPER_TASK, taskId), principal);

        assertThat(response.targetType()).isEqualTo(TraceabilityEntityType.DEVELOPER_TASK);
        assertThat(response.targetId()).isEqualTo(taskId);
    }

    private Requirement requirement() {
        User owner = new User("Test", "Owner", "owner@example.com", "hash", null);
        owner.setId(UUID.randomUUID());
        Project project = new Project("Checkout", "Revamp", ProjectStatus.ACTIVE, null, null, owner);
        project.setId(UUID.randomUUID());
        Requirement requirement = new Requirement(project, "Guest checkout", "Details",
                RequirementType.FUNCTIONAL, RequirementPriority.MEDIUM, RequirementStatus.APPROVED_FOR_DEVELOPMENT,
                "client", owner, null);
        requirement.setId(UUID.randomUUID());
        return requirement;
    }
}
