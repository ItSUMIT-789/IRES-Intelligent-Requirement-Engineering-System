package com.ires.project.service;

import com.ires.common.exception.BadRequestException;
import com.ires.common.exception.ForbiddenException;
import com.ires.project.dto.ProjectCreateRequest;
import com.ires.project.dto.ProjectUpdateRequest;
import com.ires.project.entity.Project;
import com.ires.project.entity.ProjectStatus;
import com.ires.project.repository.ProjectMemberRepository;
import com.ires.project.repository.ProjectRepository;
import com.ires.user.entity.User;
import com.ires.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserDetails principal;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void clientCreatesProjectOwnedByAuthenticatedUser() {
        User client = new User("Ada", "Lovelace", "ada@example.com", "hash", null);
        when(principal.getUsername()).thenReturn("ada@example.com");
        when(userRepository.findByEmail("ada@example.com")).thenReturn(Optional.of(client));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectCreateRequest request = new ProjectCreateRequest(
                "Checkout", "Revamp", ProjectStatus.ACTIVE, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1));

        ProjectService service = projectService;
        var response = service.create(request, principal);

        assertThat(response.name()).isEqualTo("Checkout");
        assertThat(response.client().email()).isEqualTo("ada@example.com");
    }

    @Test
    void rejectsInvalidDateRange() {
        ProjectCreateRequest request = new ProjectCreateRequest(
                "Checkout", "Revamp", null, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1));

        assertThatThrownBy(() -> projectService.create(request, principal))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void preventsClientFromUpdatingAnotherClientsProject() {
        User authenticatedClient = new User("Ada", "Lovelace", "ada@example.com", "hash", null);
        User projectOwner = new User("Grace", "Hopper", "grace@example.com", "hash", null);
        Project project = new Project("Checkout", "Revamp", ProjectStatus.ACTIVE, null, null, projectOwner);
        when(principal.getUsername()).thenReturn("ada@example.com");
        when(userRepository.findByEmail("ada@example.com")).thenReturn(Optional.of(authenticatedClient));
        when(projectRepository.findById(any(UUID.class))).thenReturn(Optional.of(project));

        ProjectUpdateRequest request = new ProjectUpdateRequest("Changed", "", ProjectStatus.ACTIVE, null, null);

        assertThatThrownBy(() -> projectService.update(UUID.randomUUID(), request, principal))
                .isInstanceOf(ForbiddenException.class);
    }
}
