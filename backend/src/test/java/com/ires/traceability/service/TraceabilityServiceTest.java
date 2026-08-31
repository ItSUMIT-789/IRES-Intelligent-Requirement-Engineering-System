package com.ires.traceability.service;

import com.ires.common.exception.BadRequestException;
import com.ires.common.exception.ConflictException;
import com.ires.project.entity.Project;
import com.ires.project.entity.ProjectStatus;
import com.ires.project.service.ProjectService;
import com.ires.requirement.criteria.repository.AcceptanceCriteriaRepository;
import com.ires.requirement.entity.Requirement;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.requirement.entity.RequirementStatus;
import com.ires.requirement.entity.RequirementType;
import com.ires.requirement.repository.RequirementRepository;
import com.ires.requirement.service.RequirementService;
import com.ires.story.entity.StoryStatus;
import com.ires.story.entity.UserStory;
import com.ires.story.repository.UserStoryRepository;
import com.ires.task.repository.DeveloperTaskRepository;
import com.ires.testing.repository.TestCaseRepository;
import com.ires.testing.repository.TestCaseExecutionRepository;
import com.ires.bug.repository.BugRepository;
import com.ires.traceability.dto.TraceabilityLinkRequest;
import com.ires.traceability.entity.TraceabilityEntityType;
import com.ires.traceability.entity.TraceabilityLink;
import com.ires.traceability.repository.TraceabilityLinkRepository;
import com.ires.user.entity.User;
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
class TraceabilityServiceTest {

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
        private TestCaseRepository testCaseRepository;

        @Mock
        private TestCaseExecutionRepository testCaseExecutionRepository;

        @Mock
        private BugRepository bugRepository;

    @Mock
    private UserDetails principal;

    @InjectMocks
    private TraceabilityService traceabilityService;

    @Test
    void createsRequirementToStoryLink() {
        Requirement requirement = requirement();
        UserStory story = new UserStory(requirement, "Checkout story", "Details", "As a shopper...",
                RequirementPriority.MEDIUM, StoryStatus.READY, requirement.getCreatedBy());
        story.setId(UUID.randomUUID());
        when(requirementService.findAccessibleRequirement(requirement.getId(), principal)).thenReturn(requirement);
        when(userStoryRepository.findById(story.getId())).thenReturn(Optional.of(story));
        when(linkRepository.existsByRequirementIdAndSourceTypeAndSourceIdAndTargetTypeAndTargetId(
                requirement.getId(), TraceabilityEntityType.REQUIREMENT, requirement.getId(),
                TraceabilityEntityType.USER_STORY, story.getId())).thenReturn(false);
        when(linkRepository.save(any(TraceabilityLink.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = traceabilityService.create(requirement.getId(), new TraceabilityLinkRequest(
                TraceabilityEntityType.REQUIREMENT, requirement.getId(),
                TraceabilityEntityType.USER_STORY, story.getId()), principal);

        assertThat(response.requirementId()).isEqualTo(requirement.getId());
        assertThat(response.sourceType()).isEqualTo(TraceabilityEntityType.REQUIREMENT);
        assertThat(response.targetType()).isEqualTo(TraceabilityEntityType.USER_STORY);
    }

    @Test
    void rejectsDuplicateLink() {
        Requirement requirement = requirement();
        UUID storyId = UUID.randomUUID();
        UserStory story = new UserStory(requirement, "Checkout story", "Details", "As a shopper...",
                RequirementPriority.MEDIUM, StoryStatus.READY, requirement.getCreatedBy());
        story.setId(storyId);
        when(requirementService.findAccessibleRequirement(requirement.getId(), principal)).thenReturn(requirement);
        when(userStoryRepository.findById(storyId)).thenReturn(Optional.of(story));
        when(linkRepository.existsByRequirementIdAndSourceTypeAndSourceIdAndTargetTypeAndTargetId(
                any(), any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> traceabilityService.create(requirement.getId(), new TraceabilityLinkRequest(
                TraceabilityEntityType.REQUIREMENT, requirement.getId(),
                TraceabilityEntityType.USER_STORY, storyId), principal))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsFutureModuleLink() {
        Requirement requirement = requirement();
        when(requirementService.findAccessibleRequirement(requirement.getId(), principal)).thenReturn(requirement);

        assertThatThrownBy(() -> traceabilityService.create(requirement.getId(), new TraceabilityLinkRequest(
                TraceabilityEntityType.REQUIREMENT, requirement.getId(),
                TraceabilityEntityType.TEST_CASE, UUID.randomUUID()), principal))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsEndpointFromAnotherRequirement() {
        Requirement requirement = requirement();
        Requirement other = requirement();
        UserStory story = new UserStory(other, "Other story", "Details", "As a user...",
                RequirementPriority.MEDIUM, StoryStatus.DRAFT, other.getCreatedBy());
        UUID storyId = UUID.randomUUID();
        story.setId(storyId);
        when(requirementService.findAccessibleRequirement(requirement.getId(), principal)).thenReturn(requirement);
        when(userStoryRepository.findById(storyId)).thenReturn(Optional.of(story));

        assertThatThrownBy(() -> traceabilityService.create(requirement.getId(), new TraceabilityLinkRequest(
                TraceabilityEntityType.REQUIREMENT, requirement.getId(),
                TraceabilityEntityType.USER_STORY, storyId), principal))
                .isInstanceOf(BadRequestException.class);
    }

    private Requirement requirement() {
        User owner = new User("Test", "Owner", UUID.randomUUID() + "@example.com", "hash", null);
        owner.setId(UUID.randomUUID());
        Project project = new Project("Checkout", "Revamp", ProjectStatus.ACTIVE, null, null, owner);
        Requirement requirement = new Requirement(project, "Guest checkout", "Details",
                RequirementType.FUNCTIONAL, RequirementPriority.MEDIUM, RequirementStatus.APPROVED_FOR_DEVELOPMENT,
                "client", owner, null);
        requirement.setId(UUID.randomUUID());
        return requirement;
    }
}
