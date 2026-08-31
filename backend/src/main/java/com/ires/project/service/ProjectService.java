package com.ires.project.service;

import com.ires.common.exception.BadRequestException;
import com.ires.common.exception.ForbiddenException;
import com.ires.common.exception.NotFoundException;
import com.ires.project.dto.ProjectCreateRequest;
import com.ires.project.dto.ProjectResponse;
import com.ires.project.dto.ProjectUpdateRequest;
import com.ires.project.entity.Project;
import com.ires.project.entity.ProjectStatus;
import com.ires.project.repository.ProjectMemberRepository;
import com.ires.project.repository.ProjectRepository;
import com.ires.user.entity.User;
import com.ires.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.JoinType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectResponse create(ProjectCreateRequest request, UserDetails principal) {
        if (!isAdmin(principal) && !hasRole(principal, "ROLE_CLIENT")) {
            throw new ForbiddenException("Only clients or admins can create projects.");
        }
        validateDates(request.startDate(), request.endDate());
        User client = currentUser(principal);
        Project project = new Project(
                request.name().trim(),
                request.description(),
                request.status() == null ? ProjectStatus.PLANNING : request.status(),
                request.startDate(),
                request.endDate(),
                client
        );
        return ProjectResponse.from(projectRepository.save(project));
    }

    public Page<ProjectResponse> list(String search, ProjectStatus status, Pageable pageable, UserDetails principal) {
        User user = currentUser(principal);
        Specification<Project> specification = accessibleTo(user, isAdmin(principal), search, status);
        return projectRepository.findAll(specification, pageable).map(ProjectResponse::from);
    }

    public ProjectResponse get(UUID id, UserDetails principal) {
        Project project = findProject(id);
        assertCanView(project, principal);
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse update(UUID id, ProjectUpdateRequest request, UserDetails principal) {
        Project project = findProject(id);
        assertCanManage(project, principal);
        project.setName(request.name().trim());
        project.setDescription(request.description());
        project.setStatus(request.status() == null ? project.getStatus() : request.status());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        validateDates(project.getStartDate(), project.getEndDate());
        return ProjectResponse.from(project);
    }

    @Transactional
    public void delete(UUID id, UserDetails principal) {
        Project project = findProject(id);
        assertCanManage(project, principal);
        projectRepository.delete(project);
    }

    public Project findProject(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found."));
    }

    public void assertCanView(Project project, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }
        User user = currentUser(principal);
        boolean owner = project.getClient().getId().equals(user.getId());
        boolean member = projectMemberRepository.existsByProjectIdAndUserId(project.getId(), user.getId());
        if (!owner && !member) {
            throw new ForbiddenException("You do not have access to this project.");
        }
    }

    public void assertCanManage(Project project, UserDetails principal) {
        if (isAdmin(principal)) {
            return;
        }
        User user = currentUser(principal);
        if (!project.getClient().getId().equals(user.getId())) {
            throw new ForbiddenException("Only the project client or an admin can manage this project.");
        }
    }

    public User currentUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .or(() -> userRepository.findByUsernameIgnoreCase(principal.getUsername()))
                .orElseThrow(() -> new NotFoundException("Authenticated user not found."));
    }

    public boolean isAdmin(UserDetails principal) {
        return hasRole(principal, "ROLE_ADMIN");
    }

    private boolean hasRole(UserDetails principal, String role) {
        return principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }

    private Specification<Project> accessibleTo(User user, boolean admin, String search, ProjectStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (query != null) {
                query.distinct(true);
            }
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (!admin) {
                var members = root.join("members", JoinType.LEFT);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("client"), user),
                        criteriaBuilder.equal(members.get("user"), user)
                ));
            }
            if (search != null && !search.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), "%" + search.trim().toLowerCase() + "%"));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private void validateDates(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("endDate cannot be before startDate.");
        }
    }
}
