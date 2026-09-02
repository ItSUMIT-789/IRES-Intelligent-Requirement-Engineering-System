package com.ires.testing.service;

import com.ires.common.exception.NotFoundException;
import com.ires.common.exception.ForbiddenException;
import com.ires.common.exception.BadRequestException;
import com.ires.project.entity.Project;
import com.ires.project.entity.ProjectStatus;
import com.ires.project.service.ProjectService;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.requirement.entity.Requirement;
import com.ires.requirement.entity.RequirementStatus;
import com.ires.requirement.entity.RequirementType;
import com.ires.story.repository.UserStoryRepository;
import com.ires.testing.dto.TestCaseCreateRequest;
import com.ires.testing.dto.TestCaseExecutionRequest;
import com.ires.testing.entity.ExecutionStatus;
import com.ires.testing.entity.TestCase;
import com.ires.testing.entity.TestCaseStatus;
import com.ires.testing.repository.TestCaseExecutionRepository;
import com.ires.testing.repository.TestCaseRepository;
import com.ires.requirement.workflow.RequirementWorkflowService;
import com.ires.user.entity.User;
import com.ires.user.entity.Role;
import com.ires.user.repository.UserRepository;
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
class TestCaseServiceTest {

    @Mock
    private TestCaseRepository testCaseRepository;
    @Mock
    private TestCaseExecutionRepository executionRepository;
    @Mock
    private ProjectService projectService;
    @Mock
    private com.ires.requirement.service.RequirementService requirementService;
    @Mock
    private UserStoryRepository userStoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDetails principal;
    @Mock
    private RequirementWorkflowService workflowService;

    @InjectMocks
    private TestCaseService testCaseService;

    @Test
    void createsTestCaseForProjectTester() {
        Project project = project();
        UserDetails developerPrincipal = securityUser("developer@example.com", "DEVELOPER");
        Requirement requirement = assignedRequirement(project);
        when(projectService.findProject(project.getId())).thenReturn(project);
        when(projectService.currentUser(developerPrincipal)).thenReturn(project.getClient());
        when(requirementService.findAccessibleRequirement(requirement.getId(), developerPrincipal)).thenReturn(requirement);
        when(testCaseRepository.save(any(TestCase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = testCaseService.create(project.getId(), new TestCaseCreateRequest(
                requirement.getId(), null, "Guest checkout works", "Verify flow", "Cart contains an item.",
                "Checkout completes.", RequirementPriority.HIGH, TestCaseStatus.READY, null), developerPrincipal);

        assertThat(response.title()).isEqualTo("Guest checkout works");
        assertThat(response.status()).isEqualTo(TestCaseStatus.READY);
    }

    @Test
    void rejectsDirectTesterAssignmentDuringCreation() {
        Project project = project();
        UserDetails developerPrincipal = securityUser("developer@example.com", "DEVELOPER");
        Requirement requirement = assignedRequirement(project);
        UUID assigneeId = UUID.randomUUID();
        when(projectService.findProject(project.getId())).thenReturn(project);
        when(projectService.currentUser(developerPrincipal)).thenReturn(project.getClient());
        when(requirementService.findAccessibleRequirement(requirement.getId(), developerPrincipal)).thenReturn(requirement);

        assertThatThrownBy(() -> testCaseService.create(project.getId(), new TestCaseCreateRequest(
                requirement.getId(), null, "Test", null, null, "Expected", null, null, assigneeId), developerPrincipal))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void recordsExecutionByAuthenticatedUser() {
        Project project = project();
        Requirement requirement = new Requirement(project, "Checkout", "Details", RequirementType.FUNCTIONAL,
                RequirementPriority.MEDIUM, RequirementStatus.IN_TESTING, "client", project.getClient(), project.getClient());
        requirement.setId(UUID.randomUUID());
        TestCase testCase = new TestCase(project, requirement, null, "Checkout", null, null,
                "Completes", RequirementPriority.MEDIUM, TestCaseStatus.READY,
                project.getClient(), null);
        TestCase other = new TestCase(project, requirement, null, "Other", null, null,
                "Completes", RequirementPriority.MEDIUM, TestCaseStatus.READY, project.getClient(), null);
        UUID testCaseId = UUID.randomUUID();
        testCase.setId(testCaseId);
        other.setId(UUID.randomUUID());
        when(testCaseRepository.findById(testCaseId)).thenReturn(Optional.of(testCase));
        when(projectService.currentUser(principal)).thenReturn(project.getClient());
        when(executionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(testCaseRepository.save(testCase)).thenReturn(testCase);
        when(testCaseRepository.findByRequirementId(requirement.getId())).thenReturn(java.util.List.of(testCase, other));
        when(executionRepository.findFirstByTestCaseIdOrderByExecutedAtDesc(testCaseId))
                .thenReturn(Optional.of(new com.ires.testing.entity.TestCaseExecution(
                        testCase, project.getClient(), ExecutionStatus.PASS, "Passed", null)));
        when(executionRepository.findFirstByTestCaseIdOrderByExecutedAtDesc(other.getId())).thenReturn(Optional.empty());

        var response = testCaseService.execute(testCaseId,
                new TestCaseExecutionRequest(ExecutionStatus.PASS, "Passed", "Verified"), principal);

        assertThat(response.executionStatus()).isEqualTo(ExecutionStatus.PASS);
        assertThat(response.actualResult()).isEqualTo("Passed");
        org.mockito.Mockito.verifyNoInteractions(workflowService);
    }

    @Test
    void testerCannotExecuteAnotherTestersTestCase() {
        Project project = project();
        User assignedTester = userWithRole("Assigned", "assigned@test.local", "TESTER");
        User otherTester = userWithRole("Other", "other@test.local", "TESTER");
        TestCase testCase = new TestCase(project, null, null, "Checkout", null, null,
                "Completes", RequirementPriority.MEDIUM, TestCaseStatus.READY,
                project.getClient(), assignedTester);
        UUID testCaseId = UUID.randomUUID();
        testCase.setId(testCaseId);
        UserDetails testerPrincipal = securityUser(otherTester.getEmail(), "TESTER");
        when(testCaseRepository.findById(testCaseId)).thenReturn(Optional.of(testCase));
        when(projectService.currentUser(testerPrincipal)).thenReturn(otherTester);

        assertThatThrownBy(() -> testCaseService.execute(testCaseId,
                new TestCaseExecutionRequest(ExecutionStatus.PASS, "Passed", null), testerPrincipal))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void rejectsAssigningTestCaseToNonTester() {
        Project project = project();
        UserDetails developerPrincipal = securityUser("developer@example.com", "DEVELOPER");
        User developer = userWithRole("Dev", "dev@test.local", "DEVELOPER");
        Requirement requirement = assignedRequirement(project);
        when(projectService.findProject(project.getId())).thenReturn(project);
        when(projectService.currentUser(developerPrincipal)).thenReturn(project.getClient());
        when(requirementService.findAccessibleRequirement(requirement.getId(), developerPrincipal)).thenReturn(requirement);

        assertThatThrownBy(() -> testCaseService.create(project.getId(), new TestCaseCreateRequest(
                requirement.getId(), null, "Test", null, null, "Expected", null, null, developer.getId()), developerPrincipal))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("workflow");
    }

    @Test
    void allLatestPassesAdvanceToAdminApproval() {
        Project project = project();
        User tester = userWithRole("Tester", "tester@ires.test", "TESTER");
        UserDetails testerPrincipal = securityUser(tester.getEmail(), "TESTER");
        Requirement requirement = new Requirement(project, "Release", "Details", RequirementType.FUNCTIONAL,
                RequirementPriority.HIGH, RequirementStatus.IN_TESTING, "client", project.getClient(), project.getClient());
        requirement.setId(UUID.randomUUID());
        TestCase first = testCase(project, requirement, tester, "First");
        TestCase second = testCase(project, requirement, tester, "Second");
        when(testCaseRepository.findById(first.getId())).thenReturn(Optional.of(first));
        when(projectService.currentUser(testerPrincipal)).thenReturn(tester);
        when(executionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(testCaseRepository.save(first)).thenReturn(first);
        when(testCaseRepository.findByRequirementId(requirement.getId())).thenReturn(java.util.List.of(first, second));
        when(executionRepository.findFirstByTestCaseIdOrderByExecutedAtDesc(first.getId()))
                .thenReturn(Optional.of(new com.ires.testing.entity.TestCaseExecution(first, tester, ExecutionStatus.PASS, "Pass", null)));
        when(executionRepository.findFirstByTestCaseIdOrderByExecutedAtDesc(second.getId()))
                .thenReturn(Optional.of(new com.ires.testing.entity.TestCaseExecution(second, tester, ExecutionStatus.PASS, "Pass", null)));

        testCaseService.execute(first.getId(), new TestCaseExecutionRequest(ExecutionStatus.PASS, "Pass", null), testerPrincipal);

        org.mockito.Mockito.verify(workflowService).transition(requirement.getId(), RequirementStatus.TEST_PASSED, testerPrincipal);
        org.mockito.Mockito.verify(workflowService).transition(requirement.getId(), RequirementStatus.WAITING_FOR_ADMIN_APPROVAL, testerPrincipal);
    }

    @Test
    void failedExecutionTransitionsRequirementAndPreservesExecution() {
        Project project = project();
        User tester = userWithRole("Tester", "tester-fail@ires.test", "TESTER");
        UserDetails testerPrincipal = securityUser(tester.getEmail(), "TESTER");
        Requirement requirement = new Requirement(project, "Release", "Details", RequirementType.FUNCTIONAL,
                RequirementPriority.HIGH, RequirementStatus.IN_TESTING, "client", project.getClient(), project.getClient());
        requirement.setId(UUID.randomUUID());
        TestCase testCase = testCase(project, requirement, tester, "Failure");
        when(testCaseRepository.findById(testCase.getId())).thenReturn(Optional.of(testCase));
        when(projectService.currentUser(testerPrincipal)).thenReturn(tester);
        when(executionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(testCaseRepository.save(testCase)).thenReturn(testCase);

        var response = testCaseService.execute(testCase.getId(),
                new TestCaseExecutionRequest(ExecutionStatus.FAIL, "Observed failure", "Reproduce"), testerPrincipal);

        assertThat(response.executedBy().id()).isEqualTo(tester.getId());
        org.mockito.Mockito.verify(executionRepository).save(any(com.ires.testing.entity.TestCaseExecution.class));
        org.mockito.Mockito.verify(workflowService).transition(requirement.getId(), RequirementStatus.TEST_FAILED, testerPrincipal);
    }

    private Project project() {
        User owner = new User("Test", "Owner", "owner@example.com", "hash", null);
        owner.setId(UUID.randomUUID());
        Project project = new Project("Checkout", "Revamp", ProjectStatus.ACTIVE, null, null, owner);
        project.setId(UUID.randomUUID());
        return project;
    }

    private User userWithRole(String firstName, String email, String roleName) {
        User user = new User(firstName, "User", email, "hash", new Role(roleName));
        user.setId(UUID.randomUUID());
        return user;
    }

    private Requirement assignedRequirement(Project project) {
        Requirement requirement = new Requirement(project, "Checkout", "Details", RequirementType.FUNCTIONAL,
                RequirementPriority.HIGH, RequirementStatus.IN_DEVELOPMENT, "client", project.getClient(), project.getClient());
        requirement.setId(UUID.randomUUID());
        return requirement;
    }

    private TestCase testCase(Project project, Requirement requirement, User tester, String title) {
        TestCase testCase = new TestCase(project, requirement, null, title, null, null, "Expected",
                RequirementPriority.HIGH, TestCaseStatus.READY, project.getClient(), tester);
        testCase.setId(UUID.randomUUID());
        return testCase;
    }

    private UserDetails securityUser(String email, String role) {
        return org.springframework.security.core.userdetails.User.withUsername(email)
                .password("hash").roles(role).build();
    }
}
