package com.ires.requirement.workflow;

import com.ires.bug.repository.BugRepository;
import com.ires.common.exception.BadRequestException;
import com.ires.common.exception.ConflictException;
import com.ires.common.exception.ForbiddenException;
import com.ires.project.entity.Project;
import com.ires.project.entity.ProjectStatus;
import com.ires.project.repository.ProjectMemberRepository;
import com.ires.project.service.ProjectService;
import com.ires.notification.service.NotificationService;
import com.ires.requirement.entity.*;
import com.ires.requirement.repository.RequirementRepository;
import com.ires.requirement.service.RequirementService;
import com.ires.task.repository.DeveloperTaskRepository;
import com.ires.testing.entity.ExecutionStatus;
import com.ires.testing.entity.TestCase;
import com.ires.testing.entity.TestCaseExecution;
import com.ires.testing.repository.TestCaseExecutionRepository;
import com.ires.testing.repository.TestCaseRepository;
import com.ires.user.entity.Role;
import com.ires.user.entity.RoleName;
import com.ires.user.entity.User;
import com.ires.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequirementWorkflowServiceTest {
    @Mock RequirementService requirementService;
    @Mock RequirementRepository requirementRepository;
    @Mock com.ires.requirement.clarification.repository.RequirementClarificationRepository clarificationRepository;
    @Mock ProjectService projectService;
    @Mock ProjectMemberRepository memberRepository;
    @Mock UserRepository userRepository;
    @Mock DeveloperTaskRepository taskRepository;
    @Mock TestCaseRepository testCaseRepository;
    @Mock TestCaseExecutionRepository executionRepository;
    @Mock BugRepository bugRepository;
    @Mock NotificationService notificationService;
    @InjectMocks RequirementWorkflowService workflow;

    private User client;
    private User analyst;
    private User admin;
    private User developer;
    private User tester;
    private Requirement requirement;
    private Map<String, User> users;

    @BeforeEach
    void setUp() {
        client = user("client@ires.test", RoleName.CLIENT);
        analyst = user("analyst@ires.test", RoleName.BUSINESS_ANALYST);
        admin = user("admin@ires.test", RoleName.ADMIN);
        developer = user("developer@ires.test", RoleName.DEVELOPER);
        tester = user("tester@ires.test", RoleName.TESTER);
        users = Map.of(client.getEmail(), client, analyst.getEmail(), analyst, admin.getEmail(), admin,
                developer.getEmail(), developer, tester.getEmail(), tester);
        Project project = new Project("IRES", "Workflow", ProjectStatus.ACTIVE, null, null, client);
        project.setId(UUID.randomUUID());
        requirement = new Requirement(project, "Final workflow", "Phase 1", RequirementType.FUNCTIONAL,
                RequirementPriority.HIGH, RequirementStatus.DRAFT, "client", client, null);
        requirement.setId(UUID.randomUUID());
        lenient().when(requirementService.findAccessibleRequirement(eq(requirement.getId()), any())).thenReturn(requirement);
        lenient().when(requirementService.findForAnalystClaim(eq(requirement.getId()), any())).thenReturn(requirement);
        lenient().when(requirementRepository.save(requirement)).thenReturn(requirement);
        lenient().when(projectService.currentUser(any())).thenAnswer(invocation -> users.get(((UserDetails) invocation.getArgument(0)).getUsername()));
        lenient().when(projectService.isAdmin(any())).thenAnswer(invocation -> hasRole(invocation.getArgument(0), RoleName.ADMIN));
    }

    @Test
    void supportsTheCompleteControlledLifecycle() {
        workflow.transition(requirement.getId(), RequirementStatus.SUBMITTED, principal(client));
        workflow.transition(requirement.getId(), RequirementStatus.IN_ANALYSIS, principal(analyst));
        workflow.transition(requirement.getId(), RequirementStatus.ANALYSIS_COMPLETED, principal(analyst));
        workflow.transition(requirement.getId(), RequirementStatus.WAITING_FOR_ADMIN_ASSIGNMENT, principal(analyst));

        when(userRepository.findById(developer.getId())).thenReturn(Optional.of(developer));
        when(memberRepository.existsByProjectIdAndUserId(requirement.getProject().getId(), developer.getId())).thenReturn(true);
        workflow.assignDeveloper(requirement.getId(), developer.getId(), principal(admin));
        workflow.transition(requirement.getId(), RequirementStatus.IN_DEVELOPMENT, principal(developer));
        workflow.transition(requirement.getId(), RequirementStatus.READY_FOR_TESTING, principal(developer));

        TestCase testCase = mock(TestCase.class);
        UUID testCaseId = UUID.randomUUID();
        when(testCase.getId()).thenReturn(testCaseId);
        when(userRepository.findById(tester.getId())).thenReturn(Optional.of(tester));
        when(memberRepository.existsByProjectIdAndUserId(requirement.getProject().getId(), tester.getId())).thenReturn(true);
        when(testCaseRepository.findByRequirementId(requirement.getId())).thenReturn(List.of(testCase));
        workflow.assignTester(requirement.getId(), tester.getId(), principal(developer));
        verify(testCase).setAssignedTo(tester);

        when(testCaseRepository.existsByRequirementIdAndAssignedToId(requirement.getId(), tester.getId())).thenReturn(true);
        workflow.transition(requirement.getId(), RequirementStatus.IN_TESTING, principal(tester));
        workflow.transition(requirement.getId(), RequirementStatus.TEST_PASSED, principal(tester));
        when(executionRepository.existsByTestCaseRequirementIdAndExecutionStatus(requirement.getId(), ExecutionStatus.PASS)).thenReturn(true);
        workflow.transition(requirement.getId(), RequirementStatus.WAITING_FOR_ADMIN_APPROVAL, principal(tester));
        TestCaseExecution passed = mock(TestCaseExecution.class);
        when(passed.getExecutionStatus()).thenReturn(ExecutionStatus.PASS);
        when(executionRepository.findFirstByTestCaseIdOrderByExecutedAtDesc(testCaseId)).thenReturn(Optional.of(passed));
        workflow.transition(requirement.getId(), RequirementStatus.COMPLETED, principal(admin));

        assertThat(requirement.getStatus()).isEqualTo(RequirementStatus.COMPLETED);
    }

    @Test
    void rejectsArbitraryStatusJumpWithConflict() {
        assertThatThrownBy(() -> workflow.transition(requirement.getId(), RequirementStatus.COMPLETED, principal(client)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsWrongRoleForOtherwiseValidTransition() {
        requirement.setStatus(RequirementStatus.SUBMITTED);
        assertThatThrownBy(() -> workflow.transition(requirement.getId(), RequirementStatus.IN_ANALYSIS, principal(developer)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void rejectsInactiveTesterAssignmentAsBadInput() {
        requirement.setStatus(RequirementStatus.READY_FOR_TESTING);
        requirement.setAssignedTo(developer);
        tester.setActive(false);
        when(userRepository.findById(tester.getId())).thenReturn(Optional.of(tester));

        assertThatThrownBy(() -> workflow.assignTester(requirement.getId(), tester.getId(), principal(developer)))
                .isInstanceOf(BadRequestException.class);
        verify(testCaseRepository, never()).saveAll(any());
    }

    @Test
    void assignedDeveloperCanReturnReadyRequirementToDevelopmentWithoutChangingArtifacts() {
        requirement.setStatus(RequirementStatus.READY_FOR_TESTING);
        requirement.setAssignedTo(developer);
        TestCase unassignedTestCase = mock(TestCase.class);
        when(testCaseRepository.findByRequirementId(requirement.getId())).thenReturn(List.of(unassignedTestCase));

        var response = workflow.returnToDevelopment(requirement.getId(), principal(developer));

        assertThat(requirement.getStatus()).isEqualTo(RequirementStatus.IN_DEVELOPMENT);
        assertThat(requirement.getAssignedTo()).isEqualTo(developer);
        assertThat(response.status()).isEqualTo(RequirementStatus.IN_DEVELOPMENT);
        verify(requirementRepository).save(requirement);
        verify(testCaseRepository, never()).delete(any(TestCase.class));
    }

    @Test
    void unrelatedDeveloperCannotReturnRequirementToDevelopment() {
        User unrelated = user("unrelated@ires.test", RoleName.DEVELOPER);
        users = new java.util.HashMap<>(users);
        users.put(unrelated.getEmail(), unrelated);
        requirement.setStatus(RequirementStatus.READY_FOR_TESTING);
        requirement.setAssignedTo(developer);

        assertThatThrownBy(() -> workflow.returnToDevelopment(requirement.getId(), principal(unrelated)))
                .isInstanceOf(ForbiddenException.class);
        verify(requirementRepository, never()).save(requirement);
    }

    @Test
    void nonDeveloperRolesCannotReturnRequirementToDevelopment() {
        requirement.setStatus(RequirementStatus.READY_FOR_TESTING);
        requirement.setAssignedTo(developer);
        for (User actor : List.of(client, analyst, tester, admin)) {
            assertThatThrownBy(() -> workflow.returnToDevelopment(requirement.getId(), principal(actor)))
                    .isInstanceOf(ForbiddenException.class);
        }
        verify(requirementRepository, never()).save(requirement);
    }

    @Test
    void assignedTesterBlocksReturnToDevelopment() {
        requirement.setStatus(RequirementStatus.READY_FOR_TESTING);
        requirement.setAssignedTo(developer);
        TestCase assignedTestCase = mock(TestCase.class);
        when(assignedTestCase.getAssignedTo()).thenReturn(tester);
        when(testCaseRepository.findByRequirementId(requirement.getId())).thenReturn(List.of(assignedTestCase));

        assertThatThrownBy(() -> workflow.returnToDevelopment(requirement.getId(), principal(developer)))
                .isInstanceOf(ConflictException.class).hasMessageContaining("assigned tester");
        verify(requirementRepository, never()).save(requirement);
    }

    @Test
    void testExecutionBlocksReturnToDevelopment() {
        requirement.setStatus(RequirementStatus.READY_FOR_TESTING);
        requirement.setAssignedTo(developer);
        when(testCaseRepository.findByRequirementId(requirement.getId())).thenReturn(List.of());
        when(executionRepository.existsByTestCaseRequirementId(requirement.getId())).thenReturn(true);

        assertThatThrownBy(() -> workflow.returnToDevelopment(requirement.getId(), principal(developer)))
                .isInstanceOf(ConflictException.class).hasMessageContaining("test executions");
        verify(requirementRepository, never()).save(requirement);
    }

    @Test
    void invalidWorkflowStatesCannotReturnToDevelopment() {
        requirement.setAssignedTo(developer);
        for (RequirementStatus status : List.of(RequirementStatus.ASSIGNED_TO_TESTER,
                RequirementStatus.IN_TESTING, RequirementStatus.TEST_PASSED,
                RequirementStatus.WAITING_FOR_ADMIN_APPROVAL, RequirementStatus.COMPLETED)) {
            requirement.setStatus(status);
            assertThatThrownBy(() -> workflow.returnToDevelopment(requirement.getId(), principal(developer)))
                    .isInstanceOf(ConflictException.class);
        }
        verify(requirementRepository, never()).save(requirement);
    }

    @Test
    void onlyAdminCanFinalizeARequirement() {
        requirement.setStatus(RequirementStatus.WAITING_FOR_ADMIN_APPROVAL);
        assertThatThrownBy(() -> workflow.transition(requirement.getId(), RequirementStatus.COMPLETED, principal(tester)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void clientCannotSubmitAnotherClientsDraft() {
        User otherClient = user("other@ires.test", RoleName.CLIENT);
        users = new java.util.HashMap<>(users);
        users.put(otherClient.getEmail(), otherClient);

        assertThatThrownBy(() -> workflow.transition(requirement.getId(), RequirementStatus.SUBMITTED, principal(otherClient)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void clientCannotCompleteAnalysisOrAssignWorkers() {
        requirement.setStatus(RequirementStatus.IN_ANALYSIS);
        assertThatThrownBy(() -> workflow.transition(requirement.getId(), RequirementStatus.ANALYSIS_COMPLETED, principal(client)))
                .isInstanceOf(ForbiddenException.class);
        requirement.setStatus(RequirementStatus.WAITING_FOR_ADMIN_ASSIGNMENT);
        assertThatThrownBy(() -> workflow.assignDeveloper(requirement.getId(), developer.getId(), principal(client)))
                .isInstanceOf(ForbiddenException.class);
        requirement.setStatus(RequirementStatus.READY_FOR_TESTING);
        assertThatThrownBy(() -> workflow.assignTester(requirement.getId(), tester.getId(), principal(client)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void onlyAdminCanAssignDeveloper() {
        requirement.setStatus(RequirementStatus.WAITING_FOR_ADMIN_ASSIGNMENT);
        for (User actor : List.of(analyst, developer, tester)) {
            assertThatThrownBy(() -> workflow.assignDeveloper(requirement.getId(), developer.getId(), principal(actor)))
                    .isInstanceOf(ForbiddenException.class);
        }
        verify(userRepository, never()).findById(developer.getId());
    }

    @Test
    void adminAssignsActiveProjectDeveloperTransactionally() {
        requirement.setStatus(RequirementStatus.WAITING_FOR_ADMIN_ASSIGNMENT);
        when(userRepository.findById(developer.getId())).thenReturn(Optional.of(developer));
        when(memberRepository.existsByProjectIdAndUserId(requirement.getProject().getId(), developer.getId())).thenReturn(true);

        var response = workflow.assignDeveloper(requirement.getId(), developer.getId(), principal(admin));

        assertThat(requirement.getAssignedTo()).isEqualTo(developer);
        assertThat(requirement.getStatus()).isEqualTo(RequirementStatus.ASSIGNED_TO_DEVELOPER);
        assertThat(response.assignedTo().id()).isEqualTo(developer.getId());
        verify(requirementRepository).save(requirement);
    }

    @Test
    void adminCannotAssignNonDeveloperRoles() {
        requirement.setStatus(RequirementStatus.WAITING_FOR_ADMIN_ASSIGNMENT);
        for (User invalid : List.of(client, analyst, tester)) {
            when(userRepository.findById(invalid.getId())).thenReturn(Optional.of(invalid));
            assertThatThrownBy(() -> workflow.assignDeveloper(requirement.getId(), invalid.getId(), principal(admin)))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Test
    void adminCannotAssignInactiveOrNonMemberDeveloper() {
        requirement.setStatus(RequirementStatus.WAITING_FOR_ADMIN_ASSIGNMENT);
        developer.setActive(false);
        when(userRepository.findById(developer.getId())).thenReturn(Optional.of(developer));
        assertThatThrownBy(() -> workflow.assignDeveloper(requirement.getId(), developer.getId(), principal(admin)))
                .isInstanceOf(BadRequestException.class);
        developer.setActive(true);
        when(memberRepository.existsByProjectIdAndUserId(requirement.getProject().getId(), developer.getId())).thenReturn(false);
        assertThatThrownBy(() -> workflow.assignDeveloper(requirement.getId(), developer.getId(), principal(admin)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void adminCannotAssignFromInvalidWorkflowState() {
        requirement.setStatus(RequirementStatus.ANALYSIS_COMPLETED);
        assertThatThrownBy(() -> workflow.assignDeveloper(requirement.getId(), developer.getId(), principal(admin)))
                .isInstanceOf(ConflictException.class);
        verify(userRepository, never()).findById(developer.getId());
    }

    @Test
    void clientCannotCompleteRequirement() {
        requirement.setStatus(RequirementStatus.WAITING_FOR_ADMIN_APPROVAL);
        assertThatThrownBy(() -> workflow.transition(requirement.getId(), RequirementStatus.COMPLETED, principal(client)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void clientCannotRespondToAnotherClientsClarification() {
        User otherClient = user("other@ires.test", RoleName.CLIENT);
        users = new java.util.HashMap<>(users);
        users.put(otherClient.getEmail(), otherClient);
        requirement.setStatus(RequirementStatus.NEEDS_CLARIFICATION);

        assertThatThrownBy(() -> workflow.respond(requirement.getId(),
                new com.ires.requirement.clarification.dto.ClarificationRequest("Response"), principal(otherClient)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void owningClientCanRespondWithoutAdvancingRequirement() {
        requirement.setStatus(RequirementStatus.NEEDS_CLARIFICATION);
        var clarification = new com.ires.requirement.clarification.entity.RequirementClarification(
                requirement, analyst, "Which users are affected?");
        clarification.setId(UUID.randomUUID());
        when(clarificationRepository.findByRequirementIdAndStatus(requirement.getId(),
                com.ires.requirement.clarification.entity.ClarificationStatus.OPEN)).thenReturn(Optional.of(clarification));
        when(clarificationRepository.save(clarification)).thenReturn(clarification);

        var result = workflow.respond(requirement.getId(),
                new com.ires.requirement.clarification.dto.ClarificationRequest("All account holders."), principal(client));

        assertThat(result.response()).isEqualTo("All account holders.");
        assertThat(requirement.getStatus()).isEqualTo(RequirementStatus.NEEDS_CLARIFICATION);
    }

    @Test
    void analystResumesWithMultipleHistoricalResponsesWhenNoneRemainOpen() {
        requirement.setStatus(RequirementStatus.NEEDS_CLARIFICATION);
        var clarification = new com.ires.requirement.clarification.entity.RequirementClarification(
                requirement, analyst, "Which users are affected?");
        clarification.setStatus(com.ires.requirement.clarification.entity.ClarificationStatus.RESPONDED);
        when(clarificationRepository.findByRequirementIdAndStatus(requirement.getId(),
                com.ires.requirement.clarification.entity.ClarificationStatus.OPEN)).thenReturn(Optional.empty());
        lenient().when(clarificationRepository.existsByRequirementIdAndStatus(requirement.getId(),
                com.ires.requirement.clarification.entity.ClarificationStatus.RESPONDED)).thenReturn(true);

        var response = workflow.resumeAnalysis(requirement.getId(), principal(analyst));

        assertThat(response.status()).isEqualTo(RequirementStatus.IN_ANALYSIS);
        verify(requirementRepository).save(requirement);
    }

    @Test
    void resumeRejectsUnansweredClarification() {
        requirement.setStatus(RequirementStatus.NEEDS_CLARIFICATION);
        var clarification = new com.ires.requirement.clarification.entity.RequirementClarification(
                requirement, analyst, "Which users are affected?");
        when(clarificationRepository.findByRequirementIdAndStatus(requirement.getId(),
                com.ires.requirement.clarification.entity.ClarificationStatus.OPEN)).thenReturn(Optional.of(clarification));
        lenient().when(clarificationRepository.existsByRequirementIdAndStatus(requirement.getId(),
                com.ires.requirement.clarification.entity.ClarificationStatus.RESPONDED)).thenReturn(true);

        assertThatThrownBy(() -> workflow.resumeAnalysis(requirement.getId(), principal(analyst)))
                .isInstanceOf(ConflictException.class).hasMessageContaining("respond");
    }

    @Test
    void resumeRejectsSubmittedRequirement() {
        requirement.setStatus(RequirementStatus.SUBMITTED);
        assertThatThrownBy(() -> workflow.resumeAnalysis(requirement.getId(), principal(analyst)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void resumeRejectsRequirementAlreadyInAnalysis() {
        requirement.setStatus(RequirementStatus.IN_ANALYSIS);
        assertThatThrownBy(() -> workflow.resumeAnalysis(requirement.getId(), principal(analyst)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void onlyBusinessAnalystCanResumeAnalysis() {
        requirement.setStatus(RequirementStatus.NEEDS_CLARIFICATION);
        for (User actor : List.of(client, developer, tester, admin)) {
            assertThatThrownBy(() -> workflow.resumeAnalysis(requirement.getId(), principal(actor)))
                    .isInstanceOf(ForbiddenException.class);
        }
    }

    private User user(String email, RoleName roleName) {
        User user = new User("Test", roleName.name(), email, "hash", new Role(roleName.name()));
        user.setId(UUID.randomUUID());
        return user;
    }

    private UserDetails principal(User user) {
        String role = user.getRoles().iterator().next().getName();
        return new org.springframework.security.core.userdetails.User(user.getEmail(), "hash",
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    private boolean hasRole(UserDetails principal, RoleName role) {
        return principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
    }
}
