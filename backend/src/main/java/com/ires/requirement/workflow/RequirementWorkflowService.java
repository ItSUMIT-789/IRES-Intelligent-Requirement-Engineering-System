package com.ires.requirement.workflow;

import com.ires.bug.entity.BugStatus;
import com.ires.bug.repository.BugRepository;
import com.ires.common.exception.*;
import com.ires.project.repository.ProjectMemberRepository;
import com.ires.project.service.ProjectService;
import com.ires.requirement.clarification.dto.*;
import com.ires.requirement.clarification.entity.*;
import com.ires.requirement.clarification.repository.RequirementClarificationRepository;
import com.ires.requirement.dto.*;
import com.ires.requirement.entity.*;
import com.ires.requirement.repository.RequirementRepository;
import com.ires.requirement.service.RequirementService;
import com.ires.task.entity.TaskStatus;
import com.ires.task.repository.DeveloperTaskRepository;
import com.ires.testing.entity.ExecutionStatus;
import com.ires.testing.repository.*;
import com.ires.user.entity.*;
import com.ires.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class RequirementWorkflowService {
    private static final List<BugStatus> OPEN_BUGS = List.of(BugStatus.OPEN, BugStatus.ASSIGNED, BugStatus.IN_PROGRESS, BugStatus.REOPENED);
    private final RequirementService requirementService;
    private final RequirementRepository requirementRepository;
    private final RequirementClarificationRepository clarificationRepository;
    private final ProjectService projectService;
    private final ProjectMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final DeveloperTaskRepository taskRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestCaseExecutionRepository executionRepository;
    private final BugRepository bugRepository;

    @Transactional public RequirementResponse transition(UUID id, RequirementStatus target, UserDetails principal) {
        Requirement requirement = requirementService.findAccessibleRequirement(id, principal);
        RequirementStatus source = requirement.getStatus();
        authorize(requirement, source, target, principal);
        validateConditions(requirement, target);
        requirement.setStatus(target);
        return RequirementResponse.from(requirementRepository.save(requirement));
    }

    @Transactional public RequirementResponse assignDeveloper(UUID id, UUID developerId, UserDetails principal) {
        Requirement requirement = requirementService.findAccessibleRequirement(id, principal);
        requireAnyRole(principal, "ROLE_ADMIN", "ROLE_BUSINESS_ANALYST");
        if (requirement.getStatus() != RequirementStatus.APPROVED_FOR_DEVELOPMENT) conflict("Developer assignment requires an approved requirement.");
        User developer = userRepository.findById(developerId).orElseThrow(() -> new NotFoundException("Developer not found."));
        if (!developer.isActive() || developer.getRoles().stream().noneMatch(r -> RoleName.DEVELOPER.name().equals(r.getName())))
            throw new BadRequestException("Only an active DEVELOPER can be assigned.");
        if (!memberRepository.existsByProjectIdAndUserId(requirement.getProject().getId(), developerId))
            throw new BadRequestException("The developer must be a member of the requirement project.");
        requirement.setAssignedTo(developer);
        return RequirementResponse.from(requirementRepository.save(requirement));
    }

    @Transactional public ClarificationResponse requestClarification(UUID id, ClarificationRequest request, UserDetails principal) {
        Requirement requirement = requirementService.findAccessibleRequirement(id, principal);
        requireAnyRole(principal, "ROLE_ADMIN", "ROLE_BUSINESS_ANALYST");
        if (requirement.getStatus() != RequirementStatus.IN_ANALYSIS) conflict("Clarification can only be requested during analysis.");
        if (clarificationRepository.findByRequirementIdAndStatus(id, ClarificationStatus.OPEN).isPresent()) conflict("An open clarification already exists.");
        RequirementClarification clarification = clarificationRepository.save(new RequirementClarification(
                requirement, projectService.currentUser(principal), request.message().trim()));
        requirement.setStatus(RequirementStatus.NEEDS_CLARIFICATION);
        requirementRepository.save(requirement);
        return ClarificationResponse.from(clarification);
    }

    @Transactional public ClarificationResponse respond(UUID id, ClarificationRequest request, UserDetails principal) {
        Requirement requirement = requirementService.findAccessibleRequirement(id, principal);
        User user = projectService.currentUser(principal);
        if (!projectService.isAdmin(principal) && (!hasRole(principal, "ROLE_CLIENT") || !requirement.getProject().getClient().getId().equals(user.getId())))
            throw new ForbiddenException("Only the owning client can respond to this clarification.");
        if (requirement.getStatus() != RequirementStatus.NEEDS_CLARIFICATION) conflict("This requirement is not awaiting clarification.");
        RequirementClarification clarification = clarificationRepository.findByRequirementIdAndStatus(id, ClarificationStatus.OPEN)
                .orElseThrow(() -> new ConflictException("No open clarification exists."));
        clarification.setResponse(request.message().trim()); clarification.setRespondedBy(user);
        clarification.setRespondedAt(Instant.now()); clarification.setStatus(ClarificationStatus.RESPONDED);
        requirement.setStatus(RequirementStatus.IN_ANALYSIS); requirementRepository.save(requirement);
        return ClarificationResponse.from(clarificationRepository.save(clarification));
    }

    public List<ClarificationResponse> clarifications(UUID id, UserDetails principal) {
        requirementService.findAccessibleRequirement(id, principal);
        return clarificationRepository.findByRequirementIdOrderByRequestedAtDesc(id).stream().map(ClarificationResponse::from).toList();
    }

    private void authorize(Requirement r, RequirementStatus source, RequirementStatus target, UserDetails p) {
        boolean admin = projectService.isAdmin(p);
        boolean validSource = switch (target) {
            case SUBMITTED -> source == RequirementStatus.DRAFT;
            case IN_ANALYSIS -> source == RequirementStatus.SUBMITTED;
            case APPROVED_FOR_DEVELOPMENT -> source == RequirementStatus.IN_ANALYSIS;
            case IN_DEVELOPMENT -> source == RequirementStatus.APPROVED_FOR_DEVELOPMENT || source == RequirementStatus.FAILED;
            case READY_FOR_TESTING -> source == RequirementStatus.IN_DEVELOPMENT;
            case IN_TESTING -> source == RequirementStatus.READY_FOR_TESTING;
            case PASSED, FAILED -> source == RequirementStatus.IN_TESTING;
            case COMPLETED -> source == RequirementStatus.PASSED;
            default -> false;
        };
        if (!validSource) conflict("Invalid requirement status transition from " + source + " to " + target + ".");
        boolean allowed = switch (target) {
            case SUBMITTED -> (admin || hasRole(p, "ROLE_CLIENT")) && source == RequirementStatus.DRAFT && owns(r, p);
            case IN_ANALYSIS -> (admin || hasRole(p, "ROLE_BUSINESS_ANALYST")) && source == RequirementStatus.SUBMITTED;
            case APPROVED_FOR_DEVELOPMENT -> (admin || hasRole(p, "ROLE_BUSINESS_ANALYST")) && source == RequirementStatus.IN_ANALYSIS;
            case IN_DEVELOPMENT -> (admin || hasRole(p, "ROLE_DEVELOPER")) && (source == RequirementStatus.APPROVED_FOR_DEVELOPMENT || source == RequirementStatus.FAILED) && assigned(r, p, admin);
            case READY_FOR_TESTING -> (admin || hasRole(p, "ROLE_DEVELOPER")) && source == RequirementStatus.IN_DEVELOPMENT && assigned(r, p, admin);
            case IN_TESTING -> (admin || hasRole(p, "ROLE_TESTER")) && source == RequirementStatus.READY_FOR_TESTING && testerAssigned(r, p, admin);
            case PASSED, FAILED -> (admin || hasRole(p, "ROLE_TESTER")) && source == RequirementStatus.IN_TESTING && testerAssigned(r, p, admin);
            case COMPLETED -> (admin || hasRole(p, "ROLE_TESTER")) && source == RequirementStatus.PASSED;
            default -> false;
        };
        if (!allowed) throw new ForbiddenException("You cannot perform this workflow transition.");
    }

    private void validateConditions(Requirement r, RequirementStatus target) {
        if (target == RequirementStatus.IN_DEVELOPMENT && r.getAssignedTo() == null) conflict("Assign a developer before starting development.");
        if (target == RequirementStatus.READY_FOR_TESTING && taskRepository.existsByRequirementIdAndStatusNot(r.getId(), TaskStatus.COMPLETED)) conflict("All developer tasks must be completed first.");
        if (target == RequirementStatus.COMPLETED) {
            if (!executionRepository.existsByTestCaseRequirementIdAndExecutionStatus(r.getId(), ExecutionStatus.PASS)) conflict("A passing test execution is required.");
            if (bugRepository.existsByRequirementIdAndStatusIn(r.getId(), OPEN_BUGS)) conflict("Open bugs must be resolved before completion.");
        }
    }

    private boolean owns(Requirement r, UserDetails p) { return r.getProject().getClient().getId().equals(projectService.currentUser(p).getId()); }
    private boolean assigned(Requirement r, UserDetails p, boolean admin) { return admin || r.getAssignedTo() != null && r.getAssignedTo().getId().equals(projectService.currentUser(p).getId()); }
    private boolean testerAssigned(Requirement r, UserDetails p, boolean admin) { return admin || testCaseRepository.existsByRequirementIdAndAssignedToId(r.getId(), projectService.currentUser(p).getId()); }
    private boolean hasRole(UserDetails p, String role) { return p.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority())); }
    private void requireAnyRole(UserDetails p, String... roles) { if (Arrays.stream(roles).noneMatch(r -> hasRole(p, r))) throw new ForbiddenException("You cannot perform this action."); }
    private void conflict(String message) { throw new ConflictException(message); }
}
