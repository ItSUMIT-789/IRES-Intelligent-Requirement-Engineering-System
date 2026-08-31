package com.ires.user.service;

import com.ires.user.entity.User;
import com.ires.user.entity.Role;
import com.ires.user.repository.RoleRepository;
import com.ires.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void findByEmailNormalizesTheEmailBeforeLookup() {
        User user = org.mockito.Mockito.mock(User.class);
        Role role = org.mockito.Mockito.mock(Role.class);
        when(user.getRoles()).thenReturn(java.util.Set.of(role));
        when(role.getName()).thenReturn("CLIENT");
        when(userRepository.findByEmail("person@example.com")).thenReturn(Optional.of(user));

        assertThat(userService.findByEmail("  PERSON@EXAMPLE.COM ")).isPresent();
        verify(userRepository).findByEmail("person@example.com");
    }

    @Test
    void existsByEmailReturnsRepositoryResult() {
        when(userRepository.existsByEmail("person@example.com")).thenReturn(true);

        assertThat(userService.existsByEmail("person@example.com")).isTrue();
        verify(userRepository).existsByEmail("person@example.com");
    }
}
