package com.ires.dashboard.service;

import com.ires.ai.entity.AnalysisStatus;
import com.ires.ai.repository.RequirementAIAnalysisRepository;
import com.ires.bug.entity.BugStatus;
import com.ires.bug.repository.BugRepository;
import com.ires.dashboard.dto.DashboardMetric;
import com.ires.dashboard.dto.DashboardResponse;
import com.ires.project.repository.ProjectRepository;
import com.ires.project.service.ProjectService;
import com.ires.requirement.entity.RequirementStatus;
import com.ires.requirement.repository.RequirementRepository;
import com.ires.task.entity.TaskStatus;
import com.ires.task.repository.DeveloperTaskRepository;
import com.ires.testing.entity.ExecutionStatus;
import com.ires.testing.entity.TestCaseStatus;
import com.ires.testing.repository.TestCaseExecutionRepository;
import com.ires.testing.repository.TestCaseRepository;
import com.ires.user.entity.RoleName;
import com.ires.user.entity.User;
import com.ires.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final List<TaskStatus> ACTIVE_TASKS = List.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS,
            TaskStatus.CODE_REVIEW, TaskStatus.BLOCKED);
    private static final List<TestCaseStatus> PENDING_TESTS = List.of(TestCaseStatus.DRAFT, TestCaseStatus.READY,
            TestCaseStatus.IN_PROGRESS);
    private static final List<BugStatus> OPEN_BUGS = List.of(BugStatus.OPEN, BugStatus.ASSIGNED,
            BugStatus.IN_PROGRESS, BugStatus.REOPENED);

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final RequirementRepository requirementRepository;
    private final RequirementAIAnalysisRepository analysisRepository;
    private final DeveloperTaskRepository taskRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestCaseExecutionRepository executionRepository;
    private final BugRepository bugRepository;
    private final ProjectService projectService;

    public DashboardResponse get(UserDetails principal) {
        User user = projectService.currentUser(principal);
        RoleName role = primaryRole(user);
        List<DashboardMetric> metrics = switch (role) {
            case ADMIN -> admin();
            case CLIENT -> client(user);
            case BUSINESS_ANALYST -> analyst(user);
            case DEVELOPER -> developer(user);
            case TESTER -> tester(user);
        };
        return new DashboardResponse(role.name(), metrics);
    }

    private List<DashboardMetric> admin() {
        long awaitingAssignment = requirementRepository.countByStatus(RequirementStatus.WAITING_FOR_ADMIN_ASSIGNMENT);
        long assignedOrInDevelopment = requirementRepository.countByStatus(RequirementStatus.ASSIGNED_TO_DEVELOPER)
                + requirementRepository.countByStatus(RequirementStatus.IN_DEVELOPMENT);
        long pendingOrFailedTests = testCaseRepository.countByStatusIn(PENDING_TESTS)
                + executionRepository.countByExecutionStatus(ExecutionStatus.FAIL);
        return List.of(
                metric("totalUsers", "Total Users", userRepository.count(), "No users found."),
                metric("totalProjects", "Total Projects", projectRepository.count(), "No projects yet."),
                metric("totalRequirements", "Total Requirements", requirementRepository.count(), "No requirements yet."),
                metric("awaitingDeveloperAssignment", "Awaiting Developer Assignment", awaitingAssignment, "No requirements awaiting assignment."),
                metric("assignedOrInDevelopment", "Assigned / In Development", assignedOrInDevelopment, "No assigned development requirements."),
                metric("awaitingFinalApproval", "Awaiting Final Approval", requirementRepository.countByStatus(RequirementStatus.WAITING_FOR_ADMIN_APPROVAL), "No requirements awaiting final approval."),
                metric("completedRequirements", "Completed Requirements", requirementRepository.countByStatus(RequirementStatus.COMPLETED), "No completed requirements."),
                metric("activeDeveloperTasks", "Active Developer Tasks", taskRepository.countByStatusIn(ACTIVE_TASKS), "No active developer tasks."),
                metric("openBugs", "Open Bugs", bugRepository.countByStatusIn(OPEN_BUGS), "No open bugs."),
                metric("pendingOrFailedTests", "Pending / Failed Tests", pendingOrFailedTests, "No pending or failed tests.")
        );
    }

    private List<DashboardMetric> client(User user) {
        List<RequirementStatus> submitted = List.of(RequirementStatus.SUBMITTED, RequirementStatus.IN_ANALYSIS,
                RequirementStatus.NEEDS_CLARIFICATION, RequirementStatus.ANALYSIS_COMPLETED,
                RequirementStatus.WAITING_FOR_ADMIN_ASSIGNMENT, RequirementStatus.ASSIGNED_TO_DEVELOPER,
                RequirementStatus.IN_DEVELOPMENT, RequirementStatus.READY_FOR_TESTING,
                RequirementStatus.ASSIGNED_TO_TESTER, RequirementStatus.IN_TESTING, RequirementStatus.TEST_FAILED,
                RequirementStatus.TEST_PASSED, RequirementStatus.WAITING_FOR_ADMIN_APPROVAL, RequirementStatus.COMPLETED);
        List<RequirementStatus> pending = List.of(RequirementStatus.SUBMITTED, RequirementStatus.IN_ANALYSIS,
                RequirementStatus.NEEDS_CLARIFICATION, RequirementStatus.ANALYSIS_COMPLETED,
                RequirementStatus.WAITING_FOR_ADMIN_ASSIGNMENT);
        List<RequirementStatus> inProgress = List.of(RequirementStatus.ASSIGNED_TO_DEVELOPER,
                RequirementStatus.IN_DEVELOPMENT, RequirementStatus.READY_FOR_TESTING,
                RequirementStatus.ASSIGNED_TO_TESTER, RequirementStatus.IN_TESTING, RequirementStatus.TEST_FAILED,
                RequirementStatus.TEST_PASSED, RequirementStatus.WAITING_FOR_ADMIN_APPROVAL);
        return List.of(
                metric("myProjects", "My Projects", projectRepository.countAccessibleTo(user.getId()), "No projects yet."),
                metric("submittedRequirements", "Submitted Requirements", requirementRepository.countAccessibleToByStatusIn(user.getId(), submitted), "No submitted requirements."),
                metric("pendingReview", "Pending Review", requirementRepository.countAccessibleToByStatusIn(user.getId(), pending), "Nothing pending review."),
                metric("approvedInProgress", "Approved / In Progress", requirementRepository.countAccessibleToByStatusIn(user.getId(), inProgress), "Nothing in progress."),
                metric("completedRequirements", "Completed Requirements", accessible(user, RequirementStatus.COMPLETED), "No completed requirements.")
        );
    }

    private List<DashboardMetric> analyst(User user) {
        return List.of(
                metric("awaitingAnalysis", "Requirements Awaiting Analysis", accessible(user, RequirementStatus.SUBMITTED), "No requirements awaiting analysis."),
                metric("inReview", "Requirements In Review", accessible(user, RequirementStatus.IN_ANALYSIS), "No requirements in review."),
                metric("needsClarification", "Requirements Needing Clarification", accessible(user, RequirementStatus.NEEDS_CLARIFICATION), "No requirements need clarification."),
                metric("sentToAdmin", "Analysis Completed / Sent to Admin",
                        accessible(user, RequirementStatus.ANALYSIS_COMPLETED)
                                + accessible(user, RequirementStatus.WAITING_FOR_ADMIN_ASSIGNMENT),
                        "No completed analyses."),
                metric("aiAnalysesPendingReview", "AI Analyses Pending Review",
                        analysisRepository.countAccessibleToByStatuses(user.getId(), List.of(AnalysisStatus.PENDING, AnalysisStatus.PROCESSING)),
                        "No AI analyses pending review.")
        );
    }

    private List<DashboardMetric> developer(User user) {
        return List.of(
                metric("assignedRequirements", "Assigned Requirements", requirementRepository.countByAssignedToIdAndStatusNot(user.getId(), RequirementStatus.COMPLETED), "No assigned requirements."),
                metric("activeTasks", "Active Developer Tasks", taskRepository.countByAssignedToIdAndStatusIn(user.getId(), ACTIVE_TASKS), "No active developer tasks."),
                metric("completedTasks", "Completed Tasks", taskRepository.countByAssignedToIdAndStatus(user.getId(), TaskStatus.COMPLETED), "No completed tasks."),
                metric("pendingTestCases", "Test Cases Awaiting Testing", testCaseRepository.countAccessibleToByStatusIn(user.getId(), PENDING_TESTS), "No test cases awaiting testing."),
                metric("openBugs", "Open Bugs", bugRepository.countByAssignedToIdAndStatusIn(user.getId(), OPEN_BUGS), "No assigned open bugs.")
        );
    }

    private List<DashboardMetric> tester(User user) {
        long latestPassed = executionRepository.countLatestAssignedByStatus(user.getId(), ExecutionStatus.PASS);
        long latestFailed = executionRepository.countLatestAssignedByStatus(user.getId(), ExecutionStatus.FAIL);
        long pending = executionRepository.countAssignedWithoutExecution(user.getId())
                + executionRepository.countLatestAssignedByStatus(user.getId(), ExecutionStatus.NOT_RUN)
                + executionRepository.countLatestAssignedByStatus(user.getId(), ExecutionStatus.BLOCKED);
        return List.of(
                metric("assignedTestCases", "Assigned Test Cases", testCaseRepository.countByAssignedToId(user.getId()), "No assigned test cases yet."),
                metric("pendingTests", "Pending Tests", pending, "No pending tests."),
                metric("passedTests", "Passed Tests", latestPassed, "No currently passing tests."),
                metric("failedTests", "Failed Tests", latestFailed, "No currently failing tests."),
                metric("openBugsReported", "Open Bugs Reported", bugRepository.countByReportedByIdAndStatusIn(user.getId(), OPEN_BUGS), "No open bugs reported.")
        );
    }

    private long accessible(User user, RequirementStatus status) {
        return requirementRepository.countAccessibleToByStatus(user.getId(), status);
    }

    private DashboardMetric metric(String key, String label, long value, String emptyMessage) {
        return DashboardMetric.count(key, label, value, emptyMessage);
    }

    private RoleName primaryRole(User user) {
        if (user.getRoles().size() != 1) throw new IllegalStateException("A user must have exactly one application role.");
        return user.getRoles().stream().findFirst().map(role -> RoleName.valueOf(role.getName())).orElseThrow();
    }
}
