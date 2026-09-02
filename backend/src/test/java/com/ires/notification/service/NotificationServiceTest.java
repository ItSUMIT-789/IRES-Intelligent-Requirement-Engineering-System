package com.ires.notification.service;

import com.ires.common.exception.NotFoundException;
import com.ires.notification.entity.Notification;
import com.ires.notification.entity.NotificationType;
import com.ires.notification.repository.NotificationRepository;
import com.ires.project.repository.ProjectMemberRepository;
import com.ires.project.service.ProjectService;
import com.ires.testing.repository.TestCaseRepository;
import com.ires.user.entity.User;
import com.ires.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock NotificationRepository repository;
    @Mock ProjectService projectService;
    @Mock UserRepository userRepository;
    @Mock ProjectMemberRepository memberRepository;
    @Mock TestCaseRepository testCaseRepository;
    @Mock UserDetails principal;
    private NotificationService service;
    private User user;

    @BeforeEach void setUp() {
        service = new NotificationService(repository, projectService, userRepository, memberRepository, testCaseRepository);
        user = new User("Phase", "Seven", "phase7@ires.test", "hash", null);
        user.setId(UUID.randomUUID());
        lenient().when(projectService.currentUser(principal)).thenReturn(user);
    }

    @Test void listsOnlyCurrentUsersNotifications() {
        var pageable = PageRequest.of(0, 20);
        Notification notification = new Notification(user, NotificationType.REQUIREMENT_COMPLETED, "Completed", "Done", null);
        when(repository.findByUserId(user.getId(), pageable)).thenReturn(new PageImpl<>(List.of(notification)));
        assertThat(service.list(null, pageable, principal)).hasSize(1);
        verify(repository).findByUserId(user.getId(), pageable);
    }

    @Test void unreadCountUsesCurrentUser() {
        when(repository.countByUserIdAndReadFalse(user.getId())).thenReturn(3L);
        assertThat(service.unread(principal)).isEqualTo(3);
    }

    @Test void marksOwnedNotificationRead() {
        UUID id = UUID.randomUUID();
        Notification notification = new Notification(user, NotificationType.TEST_PASSED, "Passed", "Ready", null);
        when(repository.findByIdAndUserId(id, user.getId())).thenReturn(Optional.of(notification));
        assertThat(service.markRead(id, principal).read()).isTrue();
    }

    @Test void cannotMarkAnotherUsersNotificationRead() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndUserId(id, user.getId())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.markRead(id, principal)).isInstanceOf(NotFoundException.class);
    }
}
