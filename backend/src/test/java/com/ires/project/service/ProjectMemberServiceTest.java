package com.ires.project.service;

import com.ires.common.exception.ConflictException;
import com.ires.common.exception.NotFoundException;
import com.ires.project.dto.ProjectMemberRequest;
import com.ires.project.entity.Project;
import com.ires.project.entity.ProjectMemberRole;
import com.ires.project.entity.ProjectStatus;
import com.ires.project.repository.ProjectMemberRepository;
import com.ires.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails principal;

    @InjectMocks
    private ProjectMemberService memberService;

    @Test
    void rejectsDuplicateMembership() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Project project = new Project("Checkout", "Revamp", ProjectStatus.ACTIVE, null, null, null);
        when(projectService.findProject(projectId)).thenReturn(project);
        when(projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)).thenReturn(true);

        assertThatThrownBy(() -> memberService.add(
                projectId, new ProjectMemberRequest(userId, ProjectMemberRole.DEVELOPER), principal))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsMissingUser() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Project project = new Project("Checkout", "Revamp", ProjectStatus.ACTIVE, null, null, null);
        when(projectService.findProject(projectId)).thenReturn(project);
        when(projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.add(
                projectId, new ProjectMemberRequest(userId, ProjectMemberRole.DEVELOPER), principal))
                .isInstanceOf(NotFoundException.class);
    }
}
