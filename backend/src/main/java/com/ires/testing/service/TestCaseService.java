package com.ires.testing.service;

import com.ires.common.exception.BadRequestException;
import com.ires.common.exception.ForbiddenException;
import com.ires.common.exception.NotFoundException;
import com.ires.project.entity.Project;
import com.ires.project.service.ProjectService;
import com.ires.requirement.entity.Requirement;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.requirement.service.RequirementService;
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
import com.ires.user.entity.RoleName;
import com.ires.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public TestCaseResponse create(UUID projectId, TestCaseCreateRequest request, UserDetails principal) {
        Project project = projectService.findProject(projectId);
        assertCanManage(project, principal);
        Requirement requirement = findRequirementForProject(request.requirementId(), projectId, principal);
        UserStory story = findStoryForProject(request.userStoryId(), projectId);
        TestCase testCase = new TestCase(
                project, requirement, story, request.title().trim(), request.description(), request.preconditions(),
                request.expectedResult().trim(), defaultPriority(request.priority()), defaultStatus(request.status()),
                projectService.currentUser(principal), findOptionalUser(request.assignedTo())
        );
        return TestCaseResponse.from(testCaseRepository.save(testCase));
    }

    public Page<TestCaseResponse> list(UUID projectId, RequirementPriority priority, TestCaseStatus status,
                                       UUID assigneeId, Pageable pageable, UserDetails principal) {
        Project project = projectService.findProject(projectId);
        projectService.assertCanView(project, principal);
        if (hasRole(principal, "ROLE_TESTER")) {
            UUID currentUserId = projectService.currentUser(principal).getId();
            if (assigneeId != null && !assigneeId.equals(currentUserId)) {
                throw new ForbiddenException("Testers can only view test cases assigned to them.");
            }
            assigneeId = currentUserId;
        }
        Specification<TestCase> specification = byProjectAndFilters(projectId, priority, status, assigneeId);
        return testCaseRepository.findAll(specification, pageable).map(TestCaseResponse::from);
    }

    public TestCaseResponse get(UUID id, UserDetails principal) {
        TestCase testCase = findTestCase(id);
        projectService.assertCanView(testCase.getProject(), principal);
        assertAssignedTester(testCase, principal);
        return TestCaseResponse.from(testCase);
    }

    @Transactional
    public TestCaseResponse update(UUID id, TestCaseUpdateRequest request, UserDetails principal) {
        TestCase testCase = findTestCase(id);
        assertCanManage(testCase.getProject(), principal);
        Requirement requirement = findRequirementForProject(request.requirementId(), testCase.getProject().getId(), principal);
        UserStory story = findStoryForProject(request.userStoryId(), testCase.getProject().getId());
        testCase.setRequirement(requirement);
        testCase.setUserStory(story);
        testCase.setTitle(request.title().trim());
        testCase.setDescription(request.description());
        testCase.setPreconditions(request.preconditions());
        testCase.setExpectedResult(request.expectedResult().trim());
        testCase.setPriority(defaultPriority(request.priority()));
        testCase.setStatus(defaultStatus(request.status()));
        testCase.setAssignedTo(findOptionalUser(request.assignedTo()));
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
        User executor = projectService.currentUser(principal);
        TestCaseExecution execution = new TestCaseExecution(
                testCase, executor, request.executionStatus(), request.actualResult(), request.notes());
        return TestCaseExecutionResponse.from(executionRepository.save(execution));
    }

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

    private User findOptionalUser(UUID userId) {
        if (userId == null) {
            return null;
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Assigned user not found."));
        boolean tester = user.getRoles().stream().anyMatch(role -> RoleName.TESTER.name().equals(role.getName()));
        if (!tester || !user.isActive()) {
            throw new BadRequestException("Test cases can only be assigned to TESTER users.");
        }
        return user;
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
                                                         TestCaseStatus status, UUID assigneeId) {
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
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
