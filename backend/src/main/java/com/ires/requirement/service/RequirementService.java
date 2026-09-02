package com.ires.requirement.service;

import com.ires.common.exception.ForbiddenException;
import com.ires.common.exception.NotFoundException;
import com.ires.project.entity.Project;
import com.ires.project.service.ProjectService;
import com.ires.requirement.dto.RequirementCreateRequest;
import com.ires.requirement.dto.RequirementResponse;
import com.ires.requirement.dto.RequirementUpdateRequest;
import com.ires.requirement.entity.Requirement;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.requirement.entity.RequirementStatus;
import com.ires.requirement.entity.RequirementType;
import com.ires.requirement.repository.RequirementRepository;
import com.ires.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequirementService {

    private static final Set<RequirementStatus> ANALYST_QUEUE_STATUSES = Set.of(
            RequirementStatus.SUBMITTED, RequirementStatus.IN_ANALYSIS,
            RequirementStatus.NEEDS_CLARIFICATION, RequirementStatus.ANALYSIS_COMPLETED);

    private final RequirementRepository requirementRepository;
    private final ProjectService projectService;

    @Transactional
    public RequirementResponse create(UUID projectId, RequirementCreateRequest request, UserDetails principal) {
        Project project = projectService.findProject(projectId);
        projectService.assertCanManage(project, principal);
        User creator = projectService.currentUser(principal);
        User assignee = null;
        Requirement requirement = new Requirement(
                project,
                request.title().trim(),
                request.description(),
                defaultType(request.requirementType()),
                defaultPriority(request.priority()),
                RequirementStatus.DRAFT,
                request.source(),
                creator,
                assignee
        );
        return RequirementResponse.from(requirementRepository.save(requirement));
    }

    public Page<RequirementResponse> list(
            UUID projectId,
            RequirementStatus status,
            RequirementPriority priority,
            String search,
            Pageable pageable,
            UserDetails principal
    ) {
        Project project = projectService.findProject(projectId);
        projectService.assertCanView(project, principal);
        Specification<Requirement> specification = byProjectAndFilters(projectId, status, priority, search);
        return requirementRepository.findAll(specification, pageable).map(RequirementResponse::from);
    }

    public Page<RequirementResponse> listAccessible(
            UUID projectId, RequirementStatus status, RequirementPriority priority, String search,
            Pageable pageable, UserDetails principal
    ) {
        User user = projectService.currentUser(principal);
        Specification<Requirement> specification = accessibleRequirements(
                user, projectService.isAdmin(principal), hasRole(principal, "ROLE_DEVELOPER"), projectId, status, priority, search);
        return requirementRepository.findAll(specification, pageable).map(RequirementResponse::from);
    }

    public Page<RequirementResponse> listAnalystQueue(
            UUID projectId, RequirementStatus status, RequirementPriority priority, String search,
            Pageable pageable, UserDetails principal
    ) {
        if (!hasRole(principal, "ROLE_BUSINESS_ANALYST")) {
            throw new ForbiddenException("Only Business Analysts can access the shared analyst queue.");
        }
        if (status != null && !ANALYST_QUEUE_STATUSES.contains(status)) {
            return Page.empty(pageable);
        }
        Specification<Requirement> specification = byAnalystQueueFilters(projectId, status, priority, search);
        return requirementRepository.findAll(specification, pageable).map(RequirementResponse::from);
    }

    public Requirement findForAnalystClaim(UUID id, UserDetails principal) {
        if (!hasRole(principal, "ROLE_BUSINESS_ANALYST")) {
            throw new ForbiddenException("Only Business Analysts can claim submitted requirements.");
        }
        Requirement requirement = findRequirement(id);
        if (requirement.getStatus() != RequirementStatus.SUBMITTED) {
            throw new ForbiddenException("Only submitted requirements can be claimed from the analyst queue.");
        }
        return requirement;
    }

    public RequirementResponse get(UUID id, UserDetails principal) {
        Requirement requirement = findRequirement(id);
        projectService.assertCanView(requirement.getProject(), principal);
        assertDeveloperAssignment(requirement, principal);
        return RequirementResponse.from(requirement);
    }

    public Requirement findAccessibleRequirement(UUID id, UserDetails principal) {
        Requirement requirement = findRequirement(id);
        projectService.assertCanView(requirement.getProject(), principal);
        assertDeveloperAssignment(requirement, principal);
        return requirement;
    }

    @Transactional
    public RequirementResponse update(UUID id, RequirementUpdateRequest request, UserDetails principal) {
        Requirement requirement = findRequirement(id);
        assertCanEdit(requirement, principal);
        requirement.setTitle(request.title().trim());
        requirement.setDescription(request.description());
        requirement.setRequirementType(defaultType(request.requirementType()));
        requirement.setPriority(defaultPriority(request.priority()));
        if (request.status() != null && request.status() != requirement.getStatus()) {
            throw new ForbiddenException("Requirement status must be changed through a workflow action.");
        }
        requirement.setSource(request.source());
        if (request.assignedTo() != null && (requirement.getAssignedTo() == null || !request.assignedTo().equals(requirement.getAssignedTo().getId()))) {
            throw new ForbiddenException("Developer assignment must use the workflow assignment action.");
        }
        return RequirementResponse.from(requirement);
    }

    @Transactional
    public void delete(UUID id, UserDetails principal) {
        Requirement requirement = findRequirement(id);
        assertCanEdit(requirement, principal);
        requirementRepository.delete(requirement);
    }

    private Requirement findRequirement(UUID id) {
        return requirementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Requirement not found."));
    }

    private void assertCanEdit(Requirement requirement, UserDetails principal) {
        if (projectService.isAdmin(principal)) {
            return;
        }
        if (principal.getAuthorities().stream().anyMatch(a -> "ROLE_BUSINESS_ANALYST".equals(a.getAuthority()))) {
            projectService.assertCanView(requirement.getProject(), principal);
            return;
        }
        User user = projectService.currentUser(principal);
        if (principal.getAuthorities().stream().anyMatch(a -> "ROLE_CLIENT".equals(a.getAuthority()))
                && requirement.getStatus() != RequirementStatus.DRAFT) {
            throw new ForbiddenException("Clients can only edit draft requirements.");
        }
        boolean projectOwner = requirement.getProject().getClient().getId().equals(user.getId());
        boolean creator = requirement.getCreatedBy().getId().equals(user.getId());
        boolean assignee = requirement.getAssignedTo() != null
                && requirement.getAssignedTo().getId().equals(user.getId());
        if (!projectOwner && !creator && !assignee) {
            throw new ForbiddenException("You do not have permission to modify this requirement.");
        }
    }

    private Specification<Requirement> byProjectAndFilters(
            UUID projectId,
            RequirementStatus status,
            RequirementPriority priority,
            String search
    ) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("project").get("id"), projectId));
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            }
            if (search != null && !search.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")), "%" + search.trim().toLowerCase() + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private Specification<Requirement> accessibleRequirements(
            User user, boolean admin, boolean developer, UUID projectId, RequirementStatus status,
            RequirementPriority priority, String search
    ) {
        return (root, query, criteriaBuilder) -> {
            if (query != null) query.distinct(true);
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (!admin) {
                var members = root.join("project").join("members", jakarta.persistence.criteria.JoinType.LEFT);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("project").get("client"), user),
                        criteriaBuilder.equal(members.get("user"), user)
                ));
            }
            if (!admin && developer) {
                predicates.add(criteriaBuilder.equal(root.get("assignedTo"), user));
            }
            if (projectId != null) predicates.add(criteriaBuilder.equal(root.get("project").get("id"), projectId));
            if (status != null) predicates.add(criteriaBuilder.equal(root.get("status"), status));
            if (priority != null) predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            if (search != null && !search.isBlank()) predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), "%" + search.trim().toLowerCase() + "%"));
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private Specification<Requirement> byAnalystQueueFilters(
            UUID projectId, RequirementStatus status, RequirementPriority priority, String search
    ) {
        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (status == null) predicates.add(root.get("status").in(ANALYST_QUEUE_STATUSES));
            else predicates.add(criteriaBuilder.equal(root.get("status"), status));
            if (projectId != null) predicates.add(criteriaBuilder.equal(root.get("project").get("id"), projectId));
            if (priority != null) predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            if (search != null && !search.isBlank()) predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), "%" + search.trim().toLowerCase() + "%"));
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private void assertDeveloperAssignment(Requirement requirement, UserDetails principal) {
        if (!hasRole(principal, "ROLE_DEVELOPER")) return;
        User user = projectService.currentUser(principal);
        if (requirement.getAssignedTo() == null || !requirement.getAssignedTo().getId().equals(user.getId())) {
            throw new ForbiddenException("Developers can only access requirements assigned to them.");
        }
    }

    private boolean hasRole(UserDetails principal, String role) {
        return principal.getAuthorities().stream().anyMatch(authority -> role.equals(authority.getAuthority()));
    }

    private RequirementType defaultType(RequirementType type) {
        return type == null ? RequirementType.FUNCTIONAL : type;
    }

    private RequirementPriority defaultPriority(RequirementPriority priority) {
        return priority == null ? RequirementPriority.MEDIUM : priority;
    }

    private RequirementStatus defaultStatus(RequirementStatus status) {
        return status == null ? RequirementStatus.DRAFT : status;
    }
}
