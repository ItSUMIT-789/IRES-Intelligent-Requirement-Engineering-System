package com.ires.project.service;

import com.ires.common.exception.ConflictException;
import com.ires.common.exception.NotFoundException;
import com.ires.project.dto.ProjectMemberRequest;
import com.ires.project.dto.ProjectMemberResponse;
import com.ires.project.entity.Project;
import com.ires.project.entity.ProjectMember;
import com.ires.project.repository.ProjectMemberRepository;
import com.ires.user.entity.User;
import com.ires.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectService projectService;
    private final UserRepository userRepository;

    @Transactional
    public ProjectMemberResponse add(UUID projectId, ProjectMemberRequest request, UserDetails principal) {
        Project project = projectService.findProject(projectId);
        projectService.assertCanManage(project, principal);
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("User not found."));
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, request.userId())) {
            throw new ConflictException("This user is already a member of the project.");
        }
        ProjectMember member = new ProjectMember(project, user, request.projectRole());
        return ProjectMemberResponse.from(projectMemberRepository.save(member));
    }

    public List<ProjectMemberResponse> list(UUID projectId, UserDetails principal) {
        Project project = projectService.findProject(projectId);
        projectService.assertCanView(project, principal);
        return projectMemberRepository.findByProjectId(projectId).stream()
                .map(ProjectMemberResponse::from)
                .toList();
    }

    @Transactional
    public void remove(UUID projectId, UUID userId, UserDetails principal) {
        Project project = projectService.findProject(projectId);
        projectService.assertCanManage(project, principal);
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new NotFoundException("Project membership not found.");
        }
        projectMemberRepository.deleteByProjectIdAndUserId(projectId, userId);
    }
}
