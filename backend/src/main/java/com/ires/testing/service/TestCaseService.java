package com.ires.testing.service;

import com.ires.common.exception.BadRequestException;
import com.ires.common.exception.ForbiddenException;
import com.ires.common.exception.NotFoundException;
import com.ires.common.exception.ConflictException;
import com.ires.project.entity.Project;
import com.ires.project.service.ProjectService;
import com.ires.requirement.entity.Requirement;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.requirement.service.RequirementService;
import com.ires.requirement.entity.RequirementStatus;
import com.ires.requirement.workflow.RequirementWorkflowService;
import com.ires.story.entity.UserStory;
import com.ires.story.repository.UserStoryRepository;
import com.ires.testing.dto.TestCaseCreateRequest;
import com.ires.testing.dto.TestCaseExecutionRequest;
import com.ires.testing.dto.TestCaseExecutionResponse;
import com.ires.testing.dto.TestCaseResponse;
import com.ires.testing.dto.TestCaseUpdateRequest;
import com.ires.testing.entity.TestCase;
import com.ires.testing.entity.TestCaseExecution;
import com.ires.testing.entity.TestCaseStatus;
import com.ires.testing.repository.TestCaseExecutionRepository;
import com.ires.testing.repository.TestCaseRepository;
import com.ires.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final TestCaseExecutionRepository executionRepository;
    private final ProjectService projectService;
    private final RequirementService requirementService;
    private final UserStoryRepository userStoryRepository;
    private final RequirementWorkflowService workflowService;

    @Transactional
    public TestCaseResponse create(UUID projectId, TestCaseCreateRequest request, UserDetails principal) {
        Project project = projectService.findProject(projectId);
        assertCanManage(project, principal);
        Requirement requirement = findRequirementForProject(request.requirementId(), projectId, principal);
        if (hasRole(principal, "ROLE_DEVELOPER")) assertDeveloperRequirement(requirement, principal);
        if (request.assignedTo() != null)
            throw new ForbiddenException("Tester assignment must use the requirement workflow assignment action.");
        UserStory story = findStoryForProject(request.userStoryId(), projectId);
        TestCase testCase = new TestCase(
                project, requirement, story, request.title().trim(), request.description(), request.preconditions(),
                request.expectedResult().trim(), defaultPriority(request.priority()), defaultStatus(request.status()),
                projectService.currentUser(principal), null
        );
        return TestCaseResponse.from(testCaseRepository.save(testCase));
    }

    public Page<TestCaseResponse> list(UUID projectId, RequirementPriority priority, TestCaseStatus status,
                                       UUID assigneeId, Pageable pageable, UserDetails principal) {
        Project project = projectService.findProject(projectId);
        projectService.assertCanView(project, principal);
        UUID developerId = null;
        if (hasRole(principal, "ROLE_TESTER")) {
            UUID currentUserId = projectService.currentUser(principal).getId();
            if (assigneeId != null && !assigneeId.equals(currentUserId)) {
                throw new ForbiddenException("Testers can only view test cases assigned to them.");
            }
            assigneeId = currentUserId;
        } else if (hasRole(principal, "ROLE_DEVELOPER")) {
            developerId = projectService.currentUser(principal).getId();
        }
        Specification<TestCase> specification = byProjectAndFilters(projectId, priority, status, assigneeId, developerId);
        return testCaseRepository.findAll(specification, pageable).map(TestCaseResponse::from);
    }

    public TestCaseResponse get(UUID id, UserDetails principal) {
        TestCase testCase = findTestCase(id);
        projectService.assertCanView(testCase.getProject(), principal);
        if (hasRole(principal, "ROLE_DEVELOPER")) assertDeveloperOwner(testCase.getRequirement(), principal);
        assertAssignedTester(testCase, principal);
        return TestCaseResponse.from(testCase);
    }

    @Transactional
    public TestCaseResponse update(UUID id, TestCaseUpdateRequest request, UserDetails principal) {
        TestCase testCase = findTestCase(id);
        assertCanManage(testCase.getProject(), principal);
        Requirement requirement = findRequirementForProject(request.requirementId(), testCase.getProject().getId(), principal);
        if (hasRole(principal, "ROLE_DEVELOPER")) assertDeveloperRequirement(requirement, principal);
        UserStory story = findStoryForProject(request.userStoryId(), testCase.getProject().getId());
        testCase.setRequirement(requirement);
        testCase.setUserStory(story);
        testCase.setTitle(request.title().trim());
        testCase.setDescription(request.description());
        testCase.setPreconditions(request.preconditions());
        testCase.setExpectedResult(request.expectedResult().trim());
        testCase.setPriority(defaultPriority(request.priority()));
        testCase.setStatus(defaultStatus(request.status()));
        if (request.assignedTo() != null && (testCase.getAssignedTo() == null
                || !request.assignedTo().equals(testCase.getAssignedTo().getId()))) {
            throw new ForbiddenException("Tester assignment must use the requirement workflow assignment action.");
        }
        return TestCaseResponse.from(testCase);
    }

    @Transactional
    public void delete(UUID id, UserDetails principal) {
        TestCase testCase = findTestCase(id);
        assertCanManage(testCase.getProject(), principal);
        testCaseRepository.delete(testCase);
    }

    @Transactional
    public TestCaseExecutionResponse execute(UUID id, TestCaseExecutionRequest request, UserDetails principal) {
        TestCase testCase = findTestCase(id);
        projectService.assertCanView(testCase.getProject(), principal);
        assertAssignedTester(testCase, principal);
        Requirement requirement = testCase.getRequirement();
        if (requirement == null) throw new BadRequestException("The test case must be linked to a requirement.");
        if (requirement.getStatus() != RequirementStatus.IN_TESTING)
            throw new ConflictException("Tests can only be executed while the requirement is in testing.");
        if (request.executionStatus() == com.ires.testing.entity.ExecutionStatus.FAIL
                && (blank(request.actualResult()) && blank(request.notes())))
            throw new BadRequestException("A failed execution requires an actual result or notes.");
        User executor = projectService.currentUser(principal);
        TestCaseExecution execution = new TestCaseExecution(
                testCase, executor, request.executionStatus(), request.actualResult(), request.notes());
        TestCaseExecution saved = executionRepository.save(execution);
        testCase.setStatus(TestCaseStatus.COMPLETED);
        testCaseRepository.save(testCase);
        if (request.executionStatus() == com.ires.testing.entity.ExecutionStatus.FAIL) {
            workflowService.transition(requirement.getId(), RequirementStatus.TEST_FAILED, principal);
        } else if (request.executionStatus() == com.ires.testing.entity.ExecutionStatus.PASS
                && allLatestExecutionsPass(requirement.getId())) {
            workflowService.transition(requirement.getId(), RequirementStatus.TEST_PASSED, principal);
            workflowService.transition(requirement.getId(), RequirementStatus.WAITING_FOR_ADMIN_APPROVAL, principal);
        }
        return TestCaseExecutionResponse.from(saved);
    }

    private boolean allLatestExecutionsPass(UUID requirementId) {
        List<TestCase> testCases = testCaseRepository.findByRequirementId(requirementId);
        return !testCases.isEmpty() && testCases.stream().allMatch(testCase -> executionRepository
                .findFirstByTestCaseIdOrderByExecutedAtDesc(testCase.getId())
                .map(execution -> execution.getExecutionStatus() == com.ires.testing.entity.ExecutionStatus.PASS)
                .orElse(false));
    }

    private boolean blank(String value) { return value == null || value.isBlank(); }

    public List<TestCaseExecutionResponse> executions(UUID id, UserDetails principal) {
        TestCase testCase = findTestCase(id);
        projectService.assertCanView(testCase.getProject(), principal);
        assertAssignedTester(testCase, principal);
        return executionRepository.findByTestCaseIdOrderByExecutedAtDesc(id).stream()
                .map(TestCaseExecutionResponse::from)
                .toList();
    }

    public TestCase findTestCase(UUID id) {
        return testCaseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Test case not found."));
    }

    private Requirement findRequirementForProject(UUID requirementId, UUID projectId, UserDetails principal) {
        if (requirementId == null) {
            return null;
        }
        Requirement requirement = requirementService.findAccessibleRequirement(requirementId, principal);
        if (!requirement.getProject().getId().equals(projectId)) {
            throw new BadRequestException("Requirement does not belong to the project.");
        }
        return requirement;
    }

    private UserStory findStoryForProject(UUID storyId, UUID projectId) {
        if (storyId == null) {
            return null;
        }
        UserStory story = userStoryRepository.findById(storyId)
                .orElseThrow(() -> new NotFoundException("User story not found."));
        if (!story.getRequirement().getProject().getId().equals(projectId)) {
            throw new BadRequestException("User story does not belong to the project.");
        }
        return story;
    }

    private void assertCanManage(Project project, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }
        if (hasRole(principal, "ROLE_BUSINESS_ANALYST") || hasRole(principal, "ROLE_DEVELOPER")) {
            projectService.assertCanView(project, principal);
            return;
        }
        throw new ForbiddenException("Only developers, business analysts, or admins can manage test cases.");
    }

    private void assertAssignedTester(TestCase testCase, UserDetails principal) {
        if (!hasRole(principal, "ROLE_TESTER")) return;
        User currentUser = projectService.currentUser(principal);
        if (testCase.getAssignedTo() == null || !testCase.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Testers can only access test cases assigned to them.");
        }
    }

    private void assertDeveloperRequirement(Requirement requirement, UserDetails principal) {
        assertDeveloperOwner(requirement, principal);
        if (requirement.getStatus() != com.ires.requirement.entity.RequirementStatus.IN_DEVELOPMENT
                && requirement.getStatus() != com.ires.requirement.entity.RequirementStatus.READY_FOR_TESTING
                && requirement.getStatus() != com.ires.requirement.entity.RequirementStatus.TEST_FAILED)
            throw new BadRequestException("Test cases can only be prepared during development or rework.");
    }

    private void assertDeveloperOwner(Requirement requirement, UserDetails principal) {
        User user = projectService.currentUser(principal);
        if (requirement == null || requirement.getAssignedTo() == null
                || !requirement.getAssignedTo().getId().equals(user.getId()))
            throw new ForbiddenException("Developers can only manage test cases for requirements assigned to them.");
    }

    private boolean isAdmin(UserDetails principal) {
        return hasRole(principal, "ROLE_ADMIN");
    }

    private boolean hasRole(UserDetails principal, String role) {
        return principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }

    private RequirementPriority defaultPriority(RequirementPriority priority) {
        return priority == null ? RequirementPriority.MEDIUM : priority;
    }

    private TestCaseStatus defaultStatus(TestCaseStatus status) {
        return status == null ? TestCaseStatus.DRAFT : status;
    }

    private Specification<TestCase> byProjectAndFilters(UUID projectId, RequirementPriority priority,
                                                         TestCaseStatus status, UUID assigneeId, UUID developerId) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("project").get("id"), projectId));
            if (priority != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (assigneeId != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignedTo").get("id"), assigneeId));
            }
            if (developerId != null) predicates.add(criteriaBuilder.equal(
                    root.get("requirement").get("assignedTo").get("id"), developerId));
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
