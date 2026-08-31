package com.ires.task.service;

import com.ires.common.exception.BadRequestException;
import com.ires.common.exception.ForbiddenException;
import com.ires.common.exception.NotFoundException;
import com.ires.project.entity.Project;
import com.ires.project.service.ProjectService;
import com.ires.requirement.entity.Requirement;
import com.ires.requirement.service.RequirementService;
import com.ires.story.entity.UserStory;
import com.ires.story.repository.UserStoryRepository;
import com.ires.task.dto.DeveloperTaskCreateRequest;
import com.ires.task.dto.DeveloperTaskResponse;
import com.ires.task.dto.DeveloperTaskUpdateRequest;
import com.ires.task.entity.DeveloperTask;
import com.ires.task.entity.TaskStatus;
import com.ires.task.repository.DeveloperTaskRepository;
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
public class DeveloperTaskService {

    private final DeveloperTaskRepository taskRepository;
    private final ProjectService projectService;
    private final RequirementService requirementService;
    private final UserStoryRepository userStoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public DeveloperTaskResponse create(UUID projectId, DeveloperTaskCreateRequest request, UserDetails principal) {
        Project project = projectService.findProject(projectId);
        assertCanCreate(project, principal);
        Requirement requirement = findRequirementForProject(request.requirementId(), projectId, principal);
        if (hasRole(principal, "ROLE_DEVELOPER") && (requirement == null || requirement.getAssignedTo() == null
                || !requirement.getAssignedTo().getId().equals(projectService.currentUser(principal).getId()))) {
            throw new ForbiddenException("Developers can only create tasks for requirements assigned to them.");
        }
        UserStory userStory = findStoryForProject(request.userStoryId(), projectId);
        User assignedTo = findOptionalUser(request.assignedTo());
        DeveloperTask task = new DeveloperTask(
                project,
                requirement,
                userStory,
                request.title().trim(),
                request.description(),
                assignedTo,
                request.priority() == null ? com.ires.requirement.entity.RequirementPriority.MEDIUM : request.priority(),
                request.status() == null ? TaskStatus.TODO : request.status(),
                request.dueDate(),
                projectService.currentUser(principal)
        );
        return DeveloperTaskResponse.from(taskRepository.save(task));
    }

    public Page<DeveloperTaskResponse> list(
            UUID projectId,
            TaskStatus status,
            UUID assigneeId,
            com.ires.requirement.entity.RequirementPriority priority,
            Pageable pageable,
            UserDetails principal
    ) {
        Project project = projectService.findProject(projectId);
        projectService.assertCanView(project, principal);
        if (hasRole(principal, "ROLE_DEVELOPER")) {
            UUID currentUserId = projectService.currentUser(principal).getId();
            if (assigneeId != null && !assigneeId.equals(currentUserId)) {
                throw new ForbiddenException("Developers can only view tasks assigned to them.");
            }
            assigneeId = currentUserId;
        }
        Specification<DeveloperTask> specification = byProjectAndFilters(projectId, status, assigneeId, priority);
        return taskRepository.findAll(specification, pageable).map(DeveloperTaskResponse::from);
    }

    public DeveloperTaskResponse get(UUID id, UserDetails principal) {
        DeveloperTask task = findTask(id);
        projectService.assertCanView(task.getProject(), principal);
        assertAssignedDeveloper(task, principal);
        return DeveloperTaskResponse.from(task);
    }

    @Transactional
    public DeveloperTaskResponse update(UUID id, DeveloperTaskUpdateRequest request, UserDetails principal) {
        DeveloperTask task = findTask(id);
        assertCanUpdate(task, principal);
        Requirement requirement = findRequirementForProject(request.requirementId(), task.getProject().getId(), principal);
        UserStory userStory = findStoryForProject(request.userStoryId(), task.getProject().getId());
        task.setRequirement(requirement);
        task.setUserStory(userStory);
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setAssignedTo(findOptionalUser(request.assignedTo()));
        task.setPriority(request.priority() == null ? task.getPriority() : request.priority());
        task.setStatus(request.status() == null ? task.getStatus() : request.status());
        task.setDueDate(request.dueDate());
        return DeveloperTaskResponse.from(task);
    }

    @Transactional
    public void delete(UUID id, UserDetails principal) {
        DeveloperTask task = findTask(id);
        assertCanManage(task.getProject(), principal);
        taskRepository.delete(task);
    }

    private DeveloperTask findTask(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Developer task not found."));
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
        boolean developer = user.getRoles().stream().anyMatch(role -> RoleName.DEVELOPER.name().equals(role.getName()));
        if (!developer) {
            throw new BadRequestException("Developer tasks can only be assigned to DEVELOPER users.");
        }
        return user;
    }

    private void assertCanCreate(Project project, UserDetails principal) {
        if (isAdmin(principal) || hasRole(principal, "ROLE_BUSINESS_ANALYST")) {
            if (!isAdmin(principal)) projectService.assertCanView(project, principal);
            return;
        }
        if (hasRole(principal, "ROLE_DEVELOPER")) {
            projectService.assertCanView(project, principal);
            return;
        }
        User currentUser = projectService.currentUser(principal);
        if (!project.getClient().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Only project owners, business analysts, or admins can create tasks.");
        }
    }

    private void assertCanUpdate(DeveloperTask task, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }
        User currentUser = projectService.currentUser(principal);
        boolean manager = task.getProject().getClient().getId().equals(currentUser.getId())
                || hasRole(principal, "ROLE_BUSINESS_ANALYST");
        boolean assignee = task.getAssignedTo() != null && task.getAssignedTo().getId().equals(currentUser.getId());
        if (!manager && !assignee) {
            throw new ForbiddenException("Only task managers or the assigned developer can update this task.");
        }
        projectService.assertCanView(task.getProject(), principal);
    }

    private void assertCanManage(Project project, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }
        User currentUser = projectService.currentUser(principal);
        if (!project.getClient().getId().equals(currentUser.getId()) && !hasRole(principal, "ROLE_BUSINESS_ANALYST")) {
            throw new ForbiddenException("Only project managers or admins can delete this task.");
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

    private void assertAssignedDeveloper(DeveloperTask task, UserDetails principal) {
        if (!hasRole(principal, "ROLE_DEVELOPER")) return;
        User currentUser = projectService.currentUser(principal);
        if (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Developers can only access tasks assigned to them.");
        }
    }

    private Specification<DeveloperTask> byProjectAndFilters(
            UUID projectId,
            TaskStatus status,
            UUID assigneeId,
            com.ires.requirement.entity.RequirementPriority priority
    ) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("project").get("id"), projectId));
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (assigneeId != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignedTo").get("id"), assigneeId));
            }
            if (priority != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            }
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
