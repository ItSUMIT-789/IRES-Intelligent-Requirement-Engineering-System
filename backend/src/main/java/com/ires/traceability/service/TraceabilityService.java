package com.ires.traceability.service;

import com.ires.common.exception.BadRequestException;
import com.ires.common.exception.ConflictException;
import com.ires.common.exception.NotFoundException;
import com.ires.common.exception.ForbiddenException;
import com.ires.requirement.criteria.repository.AcceptanceCriteriaRepository;
import com.ires.requirement.entity.Requirement;
import com.ires.requirement.repository.RequirementRepository;
import com.ires.requirement.service.RequirementService;
import com.ires.story.repository.UserStoryRepository;
import com.ires.task.repository.DeveloperTaskRepository;
import com.ires.testing.repository.TestCaseRepository;
import com.ires.testing.repository.TestCaseExecutionRepository;
import com.ires.bug.repository.BugRepository;
import com.ires.traceability.dto.TraceabilityLinkRequest;
import com.ires.traceability.dto.TraceabilityLinkResponse;
import com.ires.traceability.entity.TraceabilityEntityType;
import com.ires.traceability.entity.TraceabilityLink;
import com.ires.traceability.repository.TraceabilityLinkRepository;
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
public class TraceabilityService {

    private final TraceabilityLinkRepository linkRepository;
    private final RequirementService requirementService;
    private final RequirementRepository requirementRepository;
    private final UserStoryRepository userStoryRepository;
    private final AcceptanceCriteriaRepository criteriaRepository;
    private final DeveloperTaskRepository developerTaskRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestCaseExecutionRepository testCaseExecutionRepository;
    private final BugRepository bugRepository;

    public Page<TraceabilityLinkResponse> list(UUID requirementId, Pageable pageable, UserDetails principal) {
        requirementService.findAccessibleRequirement(requirementId, principal);
        return linkRepository.findByRequirementId(requirementId, pageable).map(TraceabilityLinkResponse::from);
    }

    @Transactional
    public TraceabilityLinkResponse create(
            UUID requirementId,
            TraceabilityLinkRequest request,
            UserDetails principal
    ) {
        Requirement requirement = requirementService.findAccessibleRequirement(requirementId, principal);
        assertAnalystOrAdmin(principal);
        validateSupportedType(request.sourceType());
        validateSupportedType(request.targetType());
        validateEndpoint(requirementId, request.sourceType(), request.sourceId());
        validateEndpoint(requirementId, request.targetType(), request.targetId());
        if (request.sourceType() == request.targetType() && request.sourceId().equals(request.targetId())) {
            throw new BadRequestException("A traceability link cannot connect an entity to itself.");
        }
        if (linkRepository.existsByRequirementIdAndSourceTypeAndSourceIdAndTargetTypeAndTargetId(
            requirementId,
            request.sourceType(),
            request.sourceId(),
            request.targetType(),
            request.targetId())) {
            throw new ConflictException("This traceability link already exists.");
        }
        TraceabilityLink link = new TraceabilityLink(
                requirementId,
                request.sourceType(),
                request.sourceId(),
                request.targetType(),
                request.targetId()
        );
        return TraceabilityLinkResponse.from(linkRepository.save(link));
    }

    @Transactional
    public void delete(UUID id, UserDetails principal) {
        TraceabilityLink link = linkRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Traceability link not found."));
        Requirement requirement = requirementService.findAccessibleRequirement(link.getRequirementId(), principal);
        assertAnalystOrAdmin(principal);
        linkRepository.delete(link);
    }

    private void validateSupportedType(TraceabilityEntityType type) {
    }

    private void assertAnalystOrAdmin(UserDetails principal) {
        boolean allowed = principal.getAuthorities().stream().anyMatch(authority ->
                "ROLE_ADMIN".equals(authority.getAuthority())
                        || "ROLE_BUSINESS_ANALYST".equals(authority.getAuthority()));
        if (!allowed) throw new ForbiddenException("Only business analysts or admins can manage traceability links.");
    }

    private void validateEndpoint(UUID requirementId, TraceabilityEntityType type, UUID entityId) {
        UUID owningRequirementId;
        switch (type) {
            case REQUIREMENT -> owningRequirementId = requirementRepository.findById(entityId)
                    .orElseThrow(() -> new NotFoundException("Requirement endpoint not found."))
                    .getId();
            case USER_STORY -> owningRequirementId = userStoryRepository.findById(entityId)
                    .orElseThrow(() -> new NotFoundException("User story endpoint not found."))
                    .getRequirement().getId();
            case ACCEPTANCE_CRITERIA -> owningRequirementId = criteriaRepository.findById(entityId)
                    .orElseThrow(() -> new NotFoundException("Acceptance criteria endpoint not found."))
                    .getRequirement().getId();
                case DEVELOPER_TASK -> {
                var task = developerTaskRepository.findById(entityId)
                    .orElseThrow(() -> new NotFoundException("Developer task endpoint not found."));
                if (!task.getProject().getId().equals(
                    requirementRepository.findById(requirementId)
                        .orElseThrow(() -> new NotFoundException("Requirement endpoint not found."))
                        .getProject().getId())) {
                    throw new BadRequestException("Developer task does not belong to the requirement project.");
                }
                owningRequirementId = task.getRequirement() == null
                    ? requirementId
                    : task.getRequirement().getId();
                }
                case TEST_CASE -> {
                var testCase = testCaseRepository.findById(entityId)
                    .orElseThrow(() -> new NotFoundException("Test case endpoint not found."));
                if (!testCase.getProject().getId().equals(
                    requirementRepository.findById(requirementId)
                        .orElseThrow(() -> new NotFoundException("Requirement endpoint not found."))
                        .getProject().getId())) {
                    throw new BadRequestException("Test case does not belong to the requirement project.");
                }
                owningRequirementId = testCase.getRequirement() == null
                    ? requirementId
                    : testCase.getRequirement().getId();
                }
                case TEST_EXECUTION -> {
                var execution = testCaseExecutionRepository.findById(entityId)
                    .orElseThrow(() -> new NotFoundException("Test execution endpoint not found."));
                var testCase = execution.getTestCase();
                if (!testCase.getProject().getId().equals(
                    requirementRepository.findById(requirementId)
                        .orElseThrow(() -> new NotFoundException("Requirement endpoint not found."))
                        .getProject().getId())) {
                    throw new BadRequestException("Test execution does not belong to the requirement project.");
                }
                owningRequirementId = testCase.getRequirement() == null
                    ? requirementId
                    : testCase.getRequirement().getId();
                }
                case BUG -> {
                var bug = bugRepository.findById(entityId)
                    .orElseThrow(() -> new NotFoundException("Bug endpoint not found."));
                if (!bug.getProject().getId().equals(
                    requirementRepository.findById(requirementId)
                        .orElseThrow(() -> new NotFoundException("Requirement endpoint not found."))
                        .getProject().getId())) {
                    throw new BadRequestException("Bug does not belong to the requirement project.");
                }
                owningRequirementId = bug.getRequirement() == null
                    ? requirementId
                    : bug.getRequirement().getId();
                }
            default -> throw new BadRequestException("Traceability endpoint type is not available.");
        }
        if (!owningRequirementId.equals(requirementId)) {
            throw new BadRequestException("Traceability endpoints must belong to the requested requirement.");
        }
    }
}
