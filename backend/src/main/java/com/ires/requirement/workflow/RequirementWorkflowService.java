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
import com.ires.notification.service.NotificationService;
import com.ires.notification.entity.NotificationType;
import com.ires.project.entity.ProjectMemberRole;
import com.ires.project.entity.ProjectMember;
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
    private final NotificationService notificationService;

    @Transactional public RequirementResponse transition(UUID id, RequirementStatus target, UserDetails principal) {
        Requirement requirement = target == RequirementStatus.IN_ANALYSIS && hasRole(principal, "ROLE_BUSINESS_ANALYST")
                ? requirementService.findForAnalystClaim(id, principal)
                : requirementService.findAccessibleRequirement(id, principal);
        RequirementStatus source = requirement.getStatus();
        authorize(requirement, source, target, principal);
        validateConditions(requirement, target);
        if (target == RequirementStatus.IN_ANALYSIS && source == RequirementStatus.SUBMITTED) {
            User analyst = projectService.currentUser(principal);
            if (!memberRepository.existsByProjectIdAndUserId(requirement.getProject().getId(), analyst.getId())) {
                memberRepository.save(new ProjectMember(requirement.getProject(), analyst, ProjectMemberRole.BUSINESS_ANALYST));
            }
        }
        requirement.setStatus(target);
        Requirement saved = requirementRepository.save(requirement);
        notifyTransition(saved, target);
        return RequirementResponse.from(saved);
    }

    @Transactional public RequirementResponse resumeAnalysis(UUID id, UserDetails principal) {
        requireAnyRole(principal, "ROLE_BUSINESS_ANALYST");
        Requirement requirement = requirementService.findAccessibleRequirement(id, principal);
        if (requirement.getStatus() != RequirementStatus.NEEDS_CLARIFICATION) {
            conflict("Resume Analysis requires a requirement awaiting clarification.");
        }
        if (clarificationRepository.findByRequirementIdAndStatus(id, ClarificationStatus.OPEN).isPresent()
                || !clarificationRepository.existsByRequirementIdAndStatus(id, ClarificationStatus.RESPONDED)) {
            conflict("The client must respond before analysis can resume.");
        }
        requirement.setStatus(RequirementStatus.IN_ANALYSIS);
        return RequirementResponse.from(requirementRepository.save(requirement));
    }

    @Transactional public RequirementResponse returnToDevelopment(UUID id, UserDetails principal) {
        requireAnyRole(principal, "ROLE_DEVELOPER");
        Requirement requirement = requirementService.findAccessibleRequirement(id, principal);
        if (requirement.getStatus() != RequirementStatus.READY_FOR_TESTING) {
            conflict("Return to Development requires a requirement ready for testing.");
        }
        if (!assigned(requirement, principal, false)) {
            throw new ForbiddenException("Only the assigned developer can return this requirement to development.");
        }
        List<com.ires.testing.entity.TestCase> testCases = testCaseRepository.findByRequirementId(id);
        if (testCases.stream().anyMatch(testCase -> testCase.getAssignedTo() != null)) {
            conflict("A requirement with an assigned tester cannot return to development.");
        }
        if (executionRepository.existsByTestCaseRequirementId(id)) {
            conflict("A requirement with test executions cannot return to development.");
        }
        requirement.setStatus(RequirementStatus.IN_DEVELOPMENT);
        return RequirementResponse.from(requirementRepository.save(requirement));
    }

    @Transactional public RequirementResponse assignDeveloper(UUID id, UUID developerId, UserDetails principal) {
        Requirement requirement = requirementService.findAccessibleRequirement(id, principal);
        requireAnyRole(principal, "ROLE_ADMIN");
        if (requirement.getStatus() != RequirementStatus.WAITING_FOR_ADMIN_ASSIGNMENT) conflict("Developer assignment requires a requirement waiting for admin assignment.");
        User developer = userRepository.findById(developerId).orElseThrow(() -> new BadRequestException("Developer not found."));
        if (!isActiveRole(developer, RoleName.DEVELOPER))
            throw new BadRequestException("Only an active DEVELOPER can be assigned.");
        if (!memberRepository.existsByProjectIdAndUserId(requirement.getProject().getId(), developerId))
            throw new BadRequestException("The developer must be a member of the requirement project.");
        requirement.setAssignedTo(developer);
        requirement.setStatus(RequirementStatus.ASSIGNED_TO_DEVELOPER);
        Requirement saved = requirementRepository.save(requirement);
        notificationService.notifyUser(developer, NotificationType.DEVELOPER_ASSIGNED, "Requirement Assigned",
                "You were assigned " + requirement.getTitle() + ".", requirement);
        return RequirementResponse.from(saved);
    }

    @Transactional public RequirementResponse assignTester(UUID id, UUID testerId, UserDetails principal) {
        Requirement requirement = requirementService.findAccessibleRequirement(id, principal);
        requireAnyRole(principal, "ROLE_DEVELOPER");
        if (requirement.getStatus() != RequirementStatus.READY_FOR_TESTING) conflict("Tester assignment requires a requirement ready for testing.");
        if (!assigned(requirement, principal, false)) throw new ForbiddenException("Only the assigned developer can assign testing.");
        User tester = userRepository.findById(testerId).orElseThrow(() -> new BadRequestException("Tester not found."));
        if (!isActiveRole(tester, RoleName.TESTER)) throw new BadRequestException("Only an active TESTER can be assigned.");
        if (!memberRepository.existsByProjectIdAndUserId(requirement.getProject().getId(), testerId))
            throw new BadRequestException("The tester must be a member of the requirement project.");
        List<com.ires.testing.entity.TestCase> testCases = testCaseRepository.findByRequirementId(id);
        if (testCases.isEmpty()) throw new BadRequestException("Create at least one test case before assigning a tester.");
        testCases.forEach(testCase -> testCase.setAssignedTo(tester));
        testCaseRepository.saveAll(testCases);
        requirement.setStatus(RequirementStatus.ASSIGNED_TO_TESTER);
        Requirement saved = requirementRepository.save(requirement);
        notificationService.notifyUser(tester, NotificationType.TESTER_ASSIGNED, "Testing Assigned",
                "You were assigned testing for " + requirement.getTitle() + ".", requirement);
        return RequirementResponse.from(saved);
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
        notificationService.notifyUser(requirement.getProject().getClient(), NotificationType.CLARIFICATION_REQUESTED,
                "Clarification Requested", "A clarification was requested for " + requirement.getTitle() + ".", requirement);
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
        ClarificationResponse response = ClarificationResponse.from(clarificationRepository.save(clarification));
        notificationService.notifyProjectRole(requirement, ProjectMemberRole.BUSINESS_ANALYST,
                NotificationType.CLARIFICATION_RESPONDED, "Clarification Responded",
                "The client responded to a clarification for " + requirement.getTitle() + ".");
        return response;
    }

    public List<ClarificationResponse> clarifications(UUID id, UserDetails principal) {
        requirementService.findAccessibleRequirement(id, principal);
        return clarificationRepository.findByRequirementIdOrderByRequestedAtDesc(id).stream().map(ClarificationResponse::from).toList();
    }

    private void authorize(Requirement r, RequirementStatus source, RequirementStatus target, UserDetails p) {
        boolean admin = projectService.isAdmin(p);
        boolean validSource = switch (target) {
            case SUBMITTED -> source == RequirementStatus.DRAFT;
            case IN_ANALYSIS -> source == RequirementStatus.SUBMITTED || source == RequirementStatus.NEEDS_CLARIFICATION;
            case ANALYSIS_COMPLETED -> source == RequirementStatus.IN_ANALYSIS;
            case WAITING_FOR_ADMIN_ASSIGNMENT -> source == RequirementStatus.ANALYSIS_COMPLETED;
            case IN_DEVELOPMENT -> source == RequirementStatus.ASSIGNED_TO_DEVELOPER || source == RequirementStatus.TEST_FAILED;
            case READY_FOR_TESTING -> source == RequirementStatus.IN_DEVELOPMENT;
            case IN_TESTING -> source == RequirementStatus.ASSIGNED_TO_TESTER;
            case TEST_PASSED, TEST_FAILED -> source == RequirementStatus.IN_TESTING;
            case WAITING_FOR_ADMIN_APPROVAL -> source == RequirementStatus.TEST_PASSED;
            case COMPLETED -> source == RequirementStatus.WAITING_FOR_ADMIN_APPROVAL;
            default -> false;
        };
        if (!validSource) conflict("Invalid requirement status transition from " + source + " to " + target + ".");
        boolean allowed = switch (target) {
            case SUBMITTED -> (admin || hasRole(p, "ROLE_CLIENT")) && source == RequirementStatus.DRAFT && owns(r, p);
            case IN_ANALYSIS -> (admin || hasRole(p, "ROLE_BUSINESS_ANALYST")) && (source == RequirementStatus.SUBMITTED || source == RequirementStatus.NEEDS_CLARIFICATION);
            case ANALYSIS_COMPLETED, WAITING_FOR_ADMIN_ASSIGNMENT -> (admin || hasRole(p, "ROLE_BUSINESS_ANALYST"));
            case IN_DEVELOPMENT -> (admin || hasRole(p, "ROLE_DEVELOPER")) && assigned(r, p, admin);
            case READY_FOR_TESTING -> (admin || hasRole(p, "ROLE_DEVELOPER")) && source == RequirementStatus.IN_DEVELOPMENT && assigned(r, p, admin);
            case IN_TESTING, TEST_PASSED, TEST_FAILED, WAITING_FOR_ADMIN_APPROVAL -> hasRole(p, "ROLE_TESTER") && testerAssigned(r, p, false);
            case COMPLETED -> admin;
            default -> false;
        };
        if (!allowed) throw new ForbiddenException("You cannot perform this workflow transition.");
    }

    private void validateConditions(Requirement r, RequirementStatus target) {
        if (target == RequirementStatus.ANALYSIS_COMPLETED
                && clarificationRepository.findByRequirementIdAndStatus(r.getId(), ClarificationStatus.OPEN).isPresent())
            conflict("Resolve the open clarification before completing analysis.");
        if (target == RequirementStatus.IN_ANALYSIS && r.getStatus() == RequirementStatus.NEEDS_CLARIFICATION
                && clarificationRepository.findByRequirementIdAndStatus(r.getId(), ClarificationStatus.OPEN).isPresent())
            conflict("The client must respond before analysis can resume.");
        if (target == RequirementStatus.IN_DEVELOPMENT && r.getAssignedTo() == null) conflict("Assign a developer before starting development.");
        if (target == RequirementStatus.READY_FOR_TESTING && taskRepository.existsByRequirementIdAndStatusNot(r.getId(), TaskStatus.COMPLETED)) conflict("All developer tasks must be completed first.");
        if (target == RequirementStatus.WAITING_FOR_ADMIN_APPROVAL || target == RequirementStatus.COMPLETED) {
            if (!executionRepository.existsByTestCaseRequirementIdAndExecutionStatus(r.getId(), ExecutionStatus.PASS)) conflict("A passing test execution is required.");
            if (bugRepository.existsByRequirementIdAndStatusIn(r.getId(), OPEN_BUGS)) conflict("Open bugs must be resolved before completion.");
        }
        if (target == RequirementStatus.COMPLETED) {
            if (r.getAssignedTo() == null) conflict("A completed requirement must have an assigned developer.");
            List<com.ires.testing.entity.TestCase> testCases = testCaseRepository.findByRequirementId(r.getId());
            if (testCases.isEmpty()) conflict("At least one test case is required before completion.");
            boolean allPass = testCases.stream().allMatch(testCase -> executionRepository
                    .findFirstByTestCaseIdOrderByExecutedAtDesc(testCase.getId())
                    .map(execution -> execution.getExecutionStatus() == ExecutionStatus.PASS).orElse(false));
            if (!allPass) conflict("Every test case must have a latest PASS execution before completion.");
        }
    }

    private void notifyTransition(Requirement r, RequirementStatus target) {
        switch (target) {
            case SUBMITTED -> notificationService.notifyProjectRole(r, ProjectMemberRole.BUSINESS_ANALYST,
                    NotificationType.REQUIREMENT_SUBMITTED, "Requirement Submitted", r.getTitle() + " is ready for analysis.");
            case WAITING_FOR_ADMIN_ASSIGNMENT -> notificationService.notifyAdmins(NotificationType.ANALYSIS_COMPLETED,
                    "Requirement Ready for Assignment", r.getTitle() + " completed analysis.", r);
            case TEST_FAILED -> notificationService.notifyUser(r.getAssignedTo(), NotificationType.TEST_FAILED,
                    "Testing Failed", r.getTitle() + " requires rework.", r);
            case WAITING_FOR_ADMIN_APPROVAL -> notificationService.notifyAdmins(NotificationType.TEST_PASSED,
                    "Requirement Awaiting Final Approval", r.getTitle() + " passed all tests.", r);
            case COMPLETED -> {
                notificationService.notifyUser(r.getProject().getClient(), NotificationType.REQUIREMENT_COMPLETED,
                        "Requirement Completed", r.getTitle() + " completed final approval.", r);
                notificationService.notifyUser(r.getAssignedTo(), NotificationType.REQUIREMENT_COMPLETED,
                        "Requirement Completed", r.getTitle() + " was completed.", r);
                notificationService.notifyAssignedTesters(r, NotificationType.REQUIREMENT_COMPLETED,
                        "Requirement Completed", r.getTitle() + " was completed.");
                notificationService.notifyProjectRole(r, ProjectMemberRole.BUSINESS_ANALYST,
                        NotificationType.REQUIREMENT_COMPLETED, "Requirement Completed", r.getTitle() + " was completed.");
            }
            default -> { }
        }
    }

    private boolean owns(Requirement r, UserDetails p) { return r.getProject().getClient().getId().equals(projectService.currentUser(p).getId()); }
    private boolean assigned(Requirement r, UserDetails p, boolean admin) { return admin || r.getAssignedTo() != null && r.getAssignedTo().getId().equals(projectService.currentUser(p).getId()); }
    private boolean testerAssigned(Requirement r, UserDetails p, boolean admin) { return admin || testCaseRepository.existsByRequirementIdAndAssignedToId(r.getId(), projectService.currentUser(p).getId()); }
    private boolean isActiveRole(User user, RoleName role) { return user.isActive() && user.getRoles().stream().anyMatch(r -> role.name().equals(r.getName())); }
    private boolean hasRole(UserDetails p, String role) { return p.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority())); }
    private void requireAnyRole(UserDetails p, String... roles) { if (Arrays.stream(roles).noneMatch(r -> hasRole(p, r))) throw new ForbiddenException("You cannot perform this action."); }
    private void conflict(String message) { throw new ConflictException(message); }
}
