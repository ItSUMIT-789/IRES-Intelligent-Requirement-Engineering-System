package com.ires.bug.service;

import com.ires.bug.dto.BugCreateRequest;
import com.ires.bug.dto.BugResponse;
import com.ires.bug.dto.BugUpdateRequest;
import com.ires.bug.entity.Bug;
import com.ires.bug.entity.BugSeverity;
import com.ires.bug.entity.BugStatus;
import com.ires.bug.repository.BugRepository;
import com.ires.common.exception.BadRequestException;
import com.ires.common.exception.ForbiddenException;
import com.ires.common.exception.NotFoundException;
import com.ires.project.entity.Project;
import com.ires.project.service.ProjectService;
import com.ires.requirement.entity.Requirement;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.requirement.service.RequirementService;
import com.ires.testing.entity.ExecutionStatus;
import com.ires.testing.entity.TestCase;
import com.ires.testing.entity.TestCaseExecution;
import com.ires.testing.repository.TestCaseExecutionRepository;
import com.ires.testing.service.TestCaseService;
import com.ires.user.entity.User;
import com.ires.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BugService {

    private final BugRepository bugRepository;
    private final ProjectService projectService;
    private final RequirementService requirementService;
    private final TestCaseService testCaseService;
    private final TestCaseExecutionRepository executionRepository;
    private final UserRepository userRepository;

    @Transactional
    public BugResponse create(UUID projectId, BugCreateRequest request, UserDetails principal) {
        Project project = projectService.findProject(projectId);
        assertCanReport(project, principal);
        Requirement requirement = findRequirementForProject(request.requirementId(), projectId, principal);
        TestCase testCase = findTestCaseForProject(request.testCaseId(), projectId, principal);
        if (requirement == null && testCase != null) {
            requirement = testCase.getRequirement();
        }
        validateTestCaseRequirement(testCase, requirement);
        Bug bug = new Bug(
                project, requirement, testCase, request.title().trim(), request.description().trim(),
                defaultSeverity(request.severity()), defaultPriority(request.priority()), defaultStatus(request.status()),
                projectService.currentUser(principal), findOptionalUser(request.assignedTo())
        );
        return BugResponse.from(bugRepository.save(bug));
    }

    @Transactional
    public BugResponse createFromFailedExecution(UUID executionId, BugCreateRequest request, UserDetails principal) {
        TestCaseExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new NotFoundException("Test case execution not found."));
        if (execution.getExecutionStatus() != ExecutionStatus.FAIL) {
            throw new BadRequestException("A bug can only be created from a failed test execution.");
        }
        TestCase testCase = execution.getTestCase();
        projectService.assertCanView(testCase.getProject(), principal);
        BugCreateRequest linkedRequest = new BugCreateRequest(
                request.requirementId(), testCase.getId(), request.title(), request.description(),
                request.severity(), request.priority(), request.status(), request.assignedTo());
        return create(testCase.getProject().getId(), linkedRequest, principal);
    }

    public Page<BugResponse> list(UUID projectId, BugStatus status, BugSeverity severity,
                                  RequirementPriority priority, UUID assigneeId, Pageable pageable,
                                  UserDetails principal) {
        Project project = projectService.findProject(projectId);
        projectService.assertCanView(project, principal);
        Specification<Bug> specification = byProjectAndFilters(projectId, status, severity, priority, assigneeId);
        return bugRepository.findAll(specification, pageable).map(BugResponse::from);
    }

    public BugResponse get(UUID id, UserDetails principal) {
        Bug bug = findBug(id);
        projectService.assertCanView(bug.getProject(), principal);
        return BugResponse.from(bug);
    }

    @Transactional
    public BugResponse update(UUID id, BugUpdateRequest request, UserDetails principal) {
        Bug bug = findBug(id);
        assertCanModify(bug, principal);
        Requirement requirement = findRequirementForProject(request.requirementId(), bug.getProject().getId(), principal);
        TestCase testCase = findTestCaseForProject(request.testCaseId(), bug.getProject().getId(), principal);
        if (requirement == null && testCase != null) {
            requirement = testCase.getRequirement();
        }
        validateTestCaseRequirement(testCase, requirement);
        bug.setRequirement(requirement);
        bug.setTestCase(testCase);
        bug.setTitle(request.title().trim());
        bug.setDescription(request.description().trim());
        bug.setSeverity(request.severity() == null ? bug.getSeverity() : request.severity());
        bug.setPriority(request.priority() == null ? bug.getPriority() : request.priority());
        setStatus(bug, request.status());
        bug.setAssignedTo(findOptionalUser(request.assignedTo()));
        return BugResponse.from(bug);
    }

    @Transactional
    public void delete(UUID id, UserDetails principal) {
        Bug bug = findBug(id);
        assertCanModify(bug, principal);
        bugRepository.delete(bug);
    }

    @Transactional
    public BugResponse resolve(UUID id, UserDetails principal) {
        Bug bug = findBug(id);
        assertCanModify(bug, principal);
        bug.setStatus(BugStatus.RESOLVED);
        bug.setResolvedAt(Instant.now());
        return BugResponse.from(bug);
    }

    @Transactional
    public BugResponse reopen(UUID id, UserDetails principal) {
        Bug bug = findBug(id);
        assertCanModify(bug, principal);
        bug.setStatus(BugStatus.REOPENED);
        bug.setResolvedAt(null);
        return BugResponse.from(bug);
    }

    public Bug findBug(UUID id) {
        return bugRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bug not found."));
    }

    private Requirement findRequirementForProject(UUID id, UUID projectId, UserDetails principal) {
        if (id == null) return null;
        Requirement requirement = requirementService.findAccessibleRequirement(id, principal);
        if (!requirement.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Requirement does not belong to the project.");
        }
        return requirement;
    }

    private TestCase findTestCaseForProject(UUID id, UUID projectId, UserDetails principal) {
        if (id == null) return null;
        TestCase testCase = testCaseService.findTestCase(id);
        if (!testCase.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Test case does not belong to the project.");
        }
        projectService.assertCanView(testCase.getProject(), principal);
        return testCase;
    }

    private void validateTestCaseRequirement(TestCase testCase, Requirement requirement) {
        if (testCase != null && requirement != null && testCase.getRequirement() != null
                && !testCase.getRequirement().getId().equals(requirement.getId())) {
            throw new BadRequestException("Test case and requirement do not match.");
        }
    }

    private User findOptionalUser(UUID id) {
        if (id == null) return null;
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assigned user not found."));
    }

    private void assertCanReport(Project project, UserDetails principal) {
        if (isAdmin(principal)) return;
        projectService.assertCanView(project, principal);
        if (hasRole(principal, "ROLE_BUSINESS_ANALYST")
                || hasRole(principal, "ROLE_TESTER") || hasRole(principal, "ROLE_DEVELOPER")) return;
        throw new ForbiddenException("Only delivery team members or admins can report bugs.");
    }

    private void assertCanModify(Bug bug, UserDetails principal) {
        if (isAdmin(principal)) return;
        if (hasRole(principal, "ROLE_BUSINESS_ANALYST") || hasRole(principal, "ROLE_TESTER")) {
            projectService.assertCanView(bug.getProject(), principal);
            return;
        }
        User currentUser = projectService.currentUser(principal);
        boolean assigned = bug.getAssignedTo() != null && bug.getAssignedTo().getId().equals(currentUser.getId());
        boolean reporter = bug.getReportedBy().getId().equals(currentUser.getId());
        if (!assigned && !reporter) throw new ForbiddenException("You cannot modify this bug.");
        projectService.assertCanView(bug.getProject(), principal);
    }

    private boolean isAdmin(UserDetails principal) { return hasRole(principal, "ROLE_ADMIN"); }

    private boolean hasRole(UserDetails principal, String role) {
        return principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(role::equals);
    }

    private BugSeverity defaultSeverity(BugSeverity value) { return value == null ? BugSeverity.MEDIUM : value; }
    private RequirementPriority defaultPriority(RequirementPriority value) { return value == null ? RequirementPriority.MEDIUM : value; }
    private BugStatus defaultStatus(BugStatus value) { return value == null ? BugStatus.OPEN : value; }

    private void setStatus(Bug bug, BugStatus status) {
        if (status != null) bug.setStatus(status);
        if (bug.getStatus() == BugStatus.RESOLVED || bug.getStatus() == BugStatus.CLOSED) {
            bug.setResolvedAt(bug.getResolvedAt() == null ? Instant.now() : bug.getResolvedAt());
        } else {
            bug.setResolvedAt(null);
        }
    }

    private Specification<Bug> byProjectAndFilters(UUID projectId, BugStatus status, BugSeverity severity,
                                                     RequirementPriority priority, UUID assigneeId) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("project").get("id"), projectId));
            if (status != null) predicates.add(criteriaBuilder.equal(root.get("status"), status));
            if (severity != null) predicates.add(criteriaBuilder.equal(root.get("severity"), severity));
            if (priority != null) predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            if (assigneeId != null) predicates.add(criteriaBuilder.equal(root.get("assignedTo").get("id"), assigneeId));
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
