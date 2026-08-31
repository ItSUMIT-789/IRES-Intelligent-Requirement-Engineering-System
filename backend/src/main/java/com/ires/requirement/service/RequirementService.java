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
import com.ires.user.repository.UserRepository;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequirementService {

    private final RequirementRepository requirementRepository;
    private final ProjectService projectService;
    private final UserRepository userRepository;

    @Transactional
    public RequirementResponse create(UUID projectId, RequirementCreateRequest request, UserDetails principal) {
        Project project = projectService.findProject(projectId);
        projectService.assertCanView(project, principal);
        User creator = projectService.currentUser(principal);
        assertCanSetStatus(request.status(), principal);
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

    public RequirementResponse get(UUID id, UserDetails principal) {
        Requirement requirement = findRequirement(id);
        projectService.assertCanView(requirement.getProject(), principal);
        return RequirementResponse.from(requirement);
    }

    public Requirement findAccessibleRequirement(UUID id, UserDetails principal) {
        Requirement requirement = findRequirement(id);
        projectService.assertCanView(requirement.getProject(), principal);
        return requirement;
    }

    @Transactional
    public RequirementResponse update(UUID id, RequirementUpdateRequest request, UserDetails principal) {
        Requirement requirement = findRequirement(id);
        assertCanEdit(requirement, principal);
        assertCanSetStatus(request.status(), principal);
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

    private void assertCanSetStatus(RequirementStatus status, UserDetails principal) {
        if (status != RequirementStatus.APPROVED_FOR_DEVELOPMENT) return;
        boolean analyst = principal.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_BUSINESS_ANALYST".equals(authority.getAuthority()));
        if (!projectService.isAdmin(principal) && !analyst) {
            throw new ForbiddenException("Only business analysts or admins can approve requirements.");
        }
    }

    private User findOptionalUser(UUID userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Assigned user not found."));
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
