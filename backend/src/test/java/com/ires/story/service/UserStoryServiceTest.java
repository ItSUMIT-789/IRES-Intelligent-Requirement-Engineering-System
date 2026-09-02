package com.ires.story.service;

import com.ires.ai.service.AIAnalysisProvider;
import com.ires.common.exception.ServiceUnavailableException;
import com.ires.project.entity.Project;
import com.ires.project.entity.ProjectStatus;
import com.ires.project.service.ProjectService;
import com.ires.requirement.entity.Requirement;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.requirement.entity.RequirementStatus;
import com.ires.requirement.entity.RequirementType;
import com.ires.requirement.service.RequirementService;
import com.ires.story.dto.UserStoryCreateRequest;
import com.ires.story.entity.StoryStatus;
import com.ires.story.entity.UserStory;
import com.ires.story.repository.UserStoryRepository;
import com.ires.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStoryServiceTest {

    @Mock
    private UserStoryRepository userStoryRepository;

    @Mock
    private RequirementService requirementService;

    @Mock
    private ProjectService projectService;

    @Mock
    private AIAnalysisProvider analysisProvider;

    @Mock
    private UserDetails principal;

    @InjectMocks
    private UserStoryService userStoryService;

    @Test
    void createsManualStoryLinkedToRequirement() {
        Requirement requirement = requirement();
        User creator = requirement.getCreatedBy();
        when(requirementService.findAccessibleRequirement(requirement.getId(), principal)).thenReturn(requirement);
        when(projectService.currentUser(principal)).thenReturn(creator);
        when(userStoryRepository.save(any(UserStory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userStoryService.create(requirement.getId(), new UserStoryCreateRequest(
                "Guest checkout story", "Description", "As a shopper, I want guest checkout.",
                RequirementPriority.HIGH, StoryStatus.READY), principal);

        assertThat(response.requirementId()).isEqualTo(requirement.getId());
        assertThat(response.status()).isEqualTo(StoryStatus.READY);
        assertThat(response.createdBy().email()).isEqualTo("creator@example.com");
    }

    @Test
    void generatesStoryThroughAIProvider() {
        Requirement requirement = requirement();
        when(requirementService.findAccessibleRequirement(requirement.getId(), principal)).thenReturn(requirement);
        when(projectService.currentUser(principal)).thenReturn(requirement.getCreatedBy());
        when(analysisProvider.analyze(requirement)).thenReturn(new AIAnalysisProvider.AIAnalysisResult(
                "AI summary", new BigDecimal("10"), new BigDecimal("90"), new BigDecimal("85"), "Suggestions"));
        when(userStoryRepository.save(any(UserStory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userStoryService.generate(requirement.getId(), principal);

        assertThat(response.title()).contains("Guest checkout");
        assertThat(response.storyText()).contains("Guest checkout");
        assertThat(response.status()).isEqualTo(StoryStatus.DRAFT);
    }

    @Test
    void reportsUnavailableAIProviderAsServiceUnavailable() {
        Requirement requirement = requirement();
        when(requirementService.findAccessibleRequirement(requirement.getId(), principal)).thenReturn(requirement);
        when(analysisProvider.analyze(requirement)).thenThrow(new IllegalStateException("offline"));

        assertThatThrownBy(() -> userStoryService.generate(requirement.getId(), principal))
                .isInstanceOf(ServiceUnavailableException.class);
    }

    @Test
    void missingRequirementIsRejected() {
        UUID requirementId = UUID.randomUUID();
        when(requirementService.findAccessibleRequirement(requirementId, principal))
                .thenThrow(new com.ires.common.exception.NotFoundException("Requirement not found."));

        assertThatThrownBy(() -> userStoryService.generate(requirementId, principal))
                .isInstanceOf(com.ires.common.exception.NotFoundException.class);
    }

    private Requirement requirement() {
        User creator = new User("Test", "Creator", "creator@example.com", "hash", null);
        creator.setId(UUID.randomUUID());
        Project project = new Project("Checkout", "Revamp", ProjectStatus.ACTIVE, null, null, creator);
        Requirement requirement = new Requirement(project, "Guest checkout", "Allow guest checkout.",
                RequirementType.FUNCTIONAL, RequirementPriority.MEDIUM, RequirementStatus.ANALYSIS_COMPLETED,
                "client", creator, null);
        requirement.setId(UUID.randomUUID());
        return requirement;
    }
}
