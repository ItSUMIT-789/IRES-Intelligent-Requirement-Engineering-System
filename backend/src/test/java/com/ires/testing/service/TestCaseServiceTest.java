package com.ires.testing.service;

import com.ires.common.exception.NotFoundException;
import com.ires.common.exception.ForbiddenException;
import com.ires.common.exception.BadRequestException;
import com.ires.project.entity.Project;
import com.ires.project.entity.ProjectStatus;
import com.ires.project.service.ProjectService;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.story.repository.UserStoryRepository;
import com.ires.testing.dto.TestCaseCreateRequest;
import com.ires.testing.dto.TestCaseExecutionRequest;
import com.ires.testing.entity.ExecutionStatus;
import com.ires.testing.entity.TestCase;
import com.ires.testing.entity.TestCaseStatus;
import com.ires.testing.repository.TestCaseExecutionRepository;
import com.ires.testing.repository.TestCaseRepository;
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

    @InjectMocks
    private TestCaseService testCaseService;

    @Test
    void createsTestCaseForProjectTester() {
        Project project = project();
        UserDetails developerPrincipal = securityUser("developer@example.com", "DEVELOPER");
        when(projectService.findProject(project.getId())).thenReturn(project);
        when(projectService.currentUser(developerPrincipal)).thenReturn(project.getClient());
        when(testCaseRepository.save(any(TestCase.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = testCaseService.create(project.getId(), new TestCaseCreateRequest(
                null, null, "Guest checkout works", "Verify flow", "Cart contains an item.",
                "Checkout completes.", RequirementPriority.HIGH, TestCaseStatus.READY, null), developerPrincipal);

        assertThat(response.title()).isEqualTo("Guest checkout works");
        assertThat(response.status()).isEqualTo(TestCaseStatus.READY);
    }

    @Test
    void rejectsMissingAssignee() {
        Project project = project();
        UserDetails developerPrincipal = securityUser("developer@example.com", "DEVELOPER");
        UUID assigneeId = UUID.randomUUID();
        when(projectService.findProject(project.getId())).thenReturn(project);
        when(projectService.currentUser(developerPrincipal)).thenReturn(project.getClient());
        when(userRepository.findById(assigneeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> testCaseService.create(project.getId(), new TestCaseCreateRequest(
                null, null, "Test", null, null, "Expected", null, null, assigneeId), developerPrincipal))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void recordsExecutionByAuthenticatedUser() {
        Project project = project();
        TestCase testCase = new TestCase(project, null, null, "Checkout", null, null,
                "Completes", RequirementPriority.MEDIUM, TestCaseStatus.READY,
                project.getClient(), null);
        UUID testCaseId = UUID.randomUUID();
        testCase.setId(testCaseId);
        when(testCaseRepository.findById(testCaseId)).thenReturn(Optional.of(testCase));
        when(projectService.currentUser(principal)).thenReturn(project.getClient());
        when(executionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = testCaseService.execute(testCaseId,
                new TestCaseExecutionRequest(ExecutionStatus.PASS, "Passed", "Verified"), principal);

        assertThat(response.executionStatus()).isEqualTo(ExecutionStatus.PASS);
        assertThat(response.actualResult()).isEqualTo("Passed");
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
        when(projectService.findProject(project.getId())).thenReturn(project);
        when(userRepository.findById(developer.getId())).thenReturn(Optional.of(developer));

        assertThatThrownBy(() -> testCaseService.create(project.getId(), new TestCaseCreateRequest(
                null, null, "Test", null, null, "Expected", null, null, developer.getId()), developerPrincipal))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("TESTER");
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

    private UserDetails securityUser(String email, String role) {
        return org.springframework.security.core.userdetails.User.withUsername(email)
                .password("hash").roles(role).build();
    }
}
