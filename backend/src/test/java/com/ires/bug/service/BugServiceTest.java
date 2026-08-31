package com.ires.bug.service;

import com.ires.bug.dto.BugCreateRequest;
import com.ires.bug.entity.Bug;
import com.ires.bug.entity.BugSeverity;
import com.ires.bug.entity.BugStatus;
import com.ires.bug.repository.BugRepository;
import com.ires.common.exception.BadRequestException;
import com.ires.project.entity.Project;
import com.ires.project.entity.ProjectStatus;
import com.ires.project.service.ProjectService;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.requirement.service.RequirementService;
import com.ires.testing.entity.ExecutionStatus;
import com.ires.testing.entity.TestCase;
import com.ires.testing.entity.TestCaseExecution;
import com.ires.testing.repository.TestCaseExecutionRepository;
import com.ires.testing.service.TestCaseService;
import com.ires.user.entity.User;
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
class BugServiceTest {

    @Mock private BugRepository bugRepository;
    @Mock private ProjectService projectService;
    @Mock private RequirementService requirementService;
    @Mock private TestCaseService testCaseService;
    @Mock private TestCaseExecutionRepository executionRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserDetails principal;

    @InjectMocks
    private BugService bugService;

    @Test
    void createsBugWithAuthenticatedReporter() {
        Project project = project();
        when(projectService.findProject(project.getId())).thenReturn(project);
        when(projectService.currentUser(principal)).thenReturn(project.getClient());
        when(bugRepository.save(any(Bug.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = bugService.create(project.getId(), new BugCreateRequest(
                null, null, "Checkout failure", "Payment failed", BugSeverity.HIGH,
                RequirementPriority.HIGH, BugStatus.OPEN, null), principal);

        assertThat(response.title()).isEqualTo("Checkout failure");
        assertThat(response.reportedBy().email()).isEqualTo("owner@example.com");
    }

    @Test
    void resolvesAndReopensBug() {
        Project project = project();
        Bug bug = new Bug(project, null, null, "Failure", "Details", BugSeverity.HIGH,
                RequirementPriority.HIGH, BugStatus.OPEN, project.getClient(), null);
        UUID bugId = UUID.randomUUID();
        bug.setId(bugId);
        when(bugRepository.findById(bugId)).thenReturn(Optional.of(bug));
        when(projectService.currentUser(principal)).thenReturn(project.getClient());

        assertThat(bugService.resolve(bugId, principal).status()).isEqualTo(BugStatus.RESOLVED);
        assertThat(bugService.reopen(bugId, principal).status()).isEqualTo(BugStatus.REOPENED);
        assertThat(bug.getResolvedAt()).isNull();
    }

    @Test
    void createsBugFromFailedExecution() {
        Project project = project();
        TestCase testCase = new TestCase(project, null, null, "Checkout", null, null,
                "Completes", RequirementPriority.MEDIUM, com.ires.testing.entity.TestCaseStatus.READY,
                project.getClient(), null);
        testCase.setId(UUID.randomUUID());
        TestCaseExecution execution = new TestCaseExecution(testCase, project.getClient(), ExecutionStatus.FAIL, "Failed", null);
        UUID executionId = UUID.randomUUID();
        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));
        when(projectService.currentUser(principal)).thenReturn(project.getClient());
        when(projectService.findProject(project.getId())).thenReturn(project);
        when(bugRepository.save(any(Bug.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = bugService.createFromFailedExecution(executionId, new BugCreateRequest(
                null, null, "Failed checkout", "Observed failure", null, null, null, null), principal);

        assertThat(response.testCaseId()).isEqualTo(testCase.getId());
        assertThat(response.title()).isEqualTo("Failed checkout");
    }

    @Test
    void rejectsBugFromNonFailedExecution() {
        Project project = project();
        TestCase testCase = new TestCase(project, null, null, "Checkout", null, null,
                "Completes", RequirementPriority.MEDIUM, com.ires.testing.entity.TestCaseStatus.READY,
                project.getClient(), null);
        TestCaseExecution execution = new TestCaseExecution(testCase, project.getClient(), ExecutionStatus.PASS, "Passed", null);
        UUID executionId = UUID.randomUUID();
        when(executionRepository.findById(executionId)).thenReturn(Optional.of(execution));

        assertThatThrownBy(() -> bugService.createFromFailedExecution(executionId, new BugCreateRequest(
                null, null, "Bug", "Details", null, null, null, null), principal))
                .isInstanceOf(BadRequestException.class);
    }

    private Project project() {
        User owner = new User("Test", "Owner", "owner@example.com", "hash", null);
        owner.setId(UUID.randomUUID());
        Project project = new Project("Checkout", "Revamp", ProjectStatus.ACTIVE, null, null, owner);
        project.setId(UUID.randomUUID());
        return project;
    }
}
