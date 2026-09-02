package com.ires.requirement.criteria.service;

import com.ires.common.exception.BadRequestException;
import com.ires.common.exception.NotFoundException;
import com.ires.project.entity.Project;
import com.ires.project.entity.ProjectStatus;
import com.ires.project.service.ProjectService;
import com.ires.requirement.criteria.dto.AcceptanceCriteriaCreateRequest;
import com.ires.requirement.criteria.entity.AcceptanceCriteria;
import com.ires.requirement.criteria.entity.CriteriaStatus;
import com.ires.requirement.criteria.entity.CriteriaType;
import com.ires.requirement.criteria.repository.AcceptanceCriteriaRepository;
import com.ires.requirement.entity.Requirement;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.requirement.entity.RequirementStatus;
import com.ires.requirement.entity.RequirementType;
import com.ires.requirement.service.RequirementService;
import com.ires.story.entity.StoryStatus;
import com.ires.story.entity.UserStory;
import com.ires.story.repository.UserStoryRepository;
import com.ires.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class AcceptanceCriteriaServiceTest {

    @Mock
    private AcceptanceCriteriaRepository criteriaRepository;

    @Mock
    private RequirementService requirementService;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserStoryRepository userStoryRepository;

    @Mock
    private UserDetails principal;

    @InjectMocks
    private AcceptanceCriteriaService criteriaService;

    @BeforeEach
    void businessAnalystPrincipal() {
        doReturn(java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_BUSINESS_ANALYST")))
                .when(principal).getAuthorities();
    }

    @Test
    void createsCriteriaLinkedToAnOptionalStory() {
        Requirement requirement = requirement();
        UserStory story = new UserStory(requirement, "Checkout story", "Description", "As a shopper...",
                RequirementPriority.HIGH, StoryStatus.READY, requirement.getCreatedBy());
        story.setId(UUID.randomUUID());
        when(requirementService.findAccessibleRequirement(requirement.getId(), principal)).thenReturn(requirement);
        when(userStoryRepository.findById(story.getId())).thenReturn(Optional.of(story));
        when(criteriaRepository.save(any(AcceptanceCriteria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = criteriaService.create(requirement.getId(), new AcceptanceCriteriaCreateRequest(
                story.getId(), "Checkout succeeds", "Payment is accepted.", CriteriaType.BEHAVIORAL,
                CriteriaStatus.READY), principal);

        assertThat(response.requirementId()).isEqualTo(requirement.getId());
        assertThat(response.userStoryId()).isEqualTo(story.getId());
        assertThat(response.criteriaType()).isEqualTo(CriteriaType.BEHAVIORAL);
    }

    @Test
    void rejectsStoryFromAnotherRequirement() {
        Requirement requirement = requirement();
        Requirement otherRequirement = requirement();
        UserStory story = new UserStory(otherRequirement, "Other story", "Description", "As a user...",
                RequirementPriority.MEDIUM, StoryStatus.DRAFT, otherRequirement.getCreatedBy());
        UUID storyId = UUID.randomUUID();
        story.setId(storyId);
        when(requirementService.findAccessibleRequirement(requirement.getId(), principal)).thenReturn(requirement);
        when(userStoryRepository.findById(storyId)).thenReturn(Optional.of(story));

        assertThatThrownBy(() -> criteriaService.create(requirement.getId(), new AcceptanceCriteriaCreateRequest(
                storyId, "Criterion", null, null, null), principal))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsMissingStory() {
        Requirement requirement = requirement();
        UUID storyId = UUID.randomUUID();
        when(requirementService.findAccessibleRequirement(requirement.getId(), principal)).thenReturn(requirement);
        when(userStoryRepository.findById(storyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> criteriaService.create(requirement.getId(), new AcceptanceCriteriaCreateRequest(
                storyId, "Criterion", null, null, null), principal))
                .isInstanceOf(NotFoundException.class);
    }

    private Requirement requirement() {
        User owner = new User("Test", "Owner", "owner@example.com", "hash", null);
        owner.setId(UUID.randomUUID());
        Project project = new Project("Checkout", "Revamp", ProjectStatus.ACTIVE, null, null, owner);
        Requirement requirement = new Requirement(project, "Guest checkout", "Details",
                RequirementType.FUNCTIONAL, RequirementPriority.MEDIUM, RequirementStatus.ANALYSIS_COMPLETED,
                "client", owner, null);
        requirement.setId(UUID.randomUUID());
        return requirement;
    }
}
