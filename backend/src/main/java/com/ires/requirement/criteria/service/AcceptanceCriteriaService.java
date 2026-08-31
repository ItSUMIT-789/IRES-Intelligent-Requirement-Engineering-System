package com.ires.requirement.criteria.service;

import com.ires.common.exception.BadRequestException;
import com.ires.common.exception.NotFoundException;
import com.ires.project.service.ProjectService;
import com.ires.requirement.criteria.dto.AcceptanceCriteriaCreateRequest;
import com.ires.requirement.criteria.dto.AcceptanceCriteriaResponse;
import com.ires.requirement.criteria.dto.AcceptanceCriteriaUpdateRequest;
import com.ires.requirement.criteria.entity.AcceptanceCriteria;
import com.ires.requirement.criteria.entity.CriteriaStatus;
import com.ires.requirement.criteria.entity.CriteriaType;
import com.ires.requirement.criteria.repository.AcceptanceCriteriaRepository;
import com.ires.requirement.entity.Requirement;
import com.ires.requirement.service.RequirementService;
import com.ires.story.entity.UserStory;
import com.ires.story.repository.UserStoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcceptanceCriteriaService {

    private final AcceptanceCriteriaRepository criteriaRepository;
    private final RequirementService requirementService;
    private final ProjectService projectService;
    private final UserStoryRepository userStoryRepository;

    @Transactional
    public AcceptanceCriteriaResponse create(
            UUID requirementId,
            AcceptanceCriteriaCreateRequest request,
            UserDetails principal
    ) {
        Requirement requirement = requirementService.findAccessibleRequirement(requirementId, principal);
        projectService.assertCanManage(requirement.getProject(), principal);
        UserStory userStory = findStoryForRequirement(request.userStoryId(), requirementId);
        AcceptanceCriteria criteria = new AcceptanceCriteria(
                requirement,
                userStory,
                request.title().trim(),
                request.description(),
                defaultType(request.criteriaType()),
                defaultStatus(request.status())
        );
        return AcceptanceCriteriaResponse.from(criteriaRepository.save(criteria));
    }

    public Page<AcceptanceCriteriaResponse> list(
            UUID requirementId,
            Pageable pageable,
            UserDetails principal
    ) {
        requirementService.findAccessibleRequirement(requirementId, principal);
        return criteriaRepository.findByRequirementId(requirementId, pageable).map(AcceptanceCriteriaResponse::from);
    }

    @Transactional
    public AcceptanceCriteriaResponse update(
            UUID id,
            AcceptanceCriteriaUpdateRequest request,
            UserDetails principal
    ) {
        AcceptanceCriteria criteria = findCriteria(id);
        projectService.assertCanManage(criteria.getRequirement().getProject(), principal);
        UserStory userStory = findStoryForRequirement(request.userStoryId(), criteria.getRequirement().getId());
        criteria.setUserStory(userStory);
        criteria.setTitle(request.title().trim());
        criteria.setDescription(request.description());
        criteria.setCriteriaType(defaultType(request.criteriaType()));
        criteria.setStatus(defaultStatus(request.status()));
        return AcceptanceCriteriaResponse.from(criteria);
    }

    @Transactional
    public void delete(UUID id, UserDetails principal) {
        AcceptanceCriteria criteria = findCriteria(id);
        projectService.assertCanManage(criteria.getRequirement().getProject(), principal);
        criteriaRepository.delete(criteria);
    }

    private AcceptanceCriteria findCriteria(UUID id) {
        return criteriaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Acceptance criteria not found."));
    }

    private UserStory findStoryForRequirement(UUID storyId, UUID requirementId) {
        if (storyId == null) {
            return null;
        }
        UserStory story = userStoryRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("User story not found."));
        if (!story.getRequirement().getId().equals(requirementId)) {
            throw new BadRequestException("User story does not belong to the requirement.");
        }
        return story;
    }

    private CriteriaType defaultType(CriteriaType type) {
        return type == null ? CriteriaType.FUNCTIONAL : type;
    }

    private CriteriaStatus defaultStatus(CriteriaStatus status) {
        return status == null ? CriteriaStatus.DRAFT : status;
    }
}
