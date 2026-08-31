package com.ires.user.service;

import com.ires.common.exception.ConflictException;
import com.ires.user.dto.RegisterRequest;
import com.ires.user.entity.Role;
import com.ires.user.entity.RoleName;
import com.ires.user.entity.User;
import com.ires.user.repository.RoleRepository;
import com.ires.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceRegistrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registersUserWithHashedPasswordAndClientRole() {
        Role clientRole = new Role("CLIENT");
        RegisterRequest request = new RegisterRequest(
                "Ada", "Lovelace", " ADA@EXAMPLE.COM ", "PlainPassword1", RoleName.CLIENT);
        when(userRepository.existsByEmail("ada@example.com")).thenReturn(false);
        when(roleRepository.findByName("CLIENT")).thenReturn(Optional.of(clientRole));
        when(passwordEncoder.encode("PlainPassword1")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userService.register(request);

        assertThat(response.email()).isEqualTo("ada@example.com");
        assertThat(response.roles()).containsExactly("CLIENT");
        verify(passwordEncoder).encode("PlainPassword1");
    }

    @Test
    void rejectsDuplicateEmailBeforeHashingPassword() {
        RegisterRequest request = new RegisterRequest(
                "Ada", "Lovelace", "ada@example.com", "PlainPassword1", RoleName.CLIENT);
        when(userRepository.existsByEmail("ada@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void registersEachAllowedPublicRole() {
        for (RoleName roleName : new RoleName[]{RoleName.BUSINESS_ANALYST, RoleName.CLIENT,
                RoleName.DEVELOPER, RoleName.TESTER}) {
            String email = roleName.name().toLowerCase() + "@example.com";
            Role role = new Role(roleName.name());
            when(userRepository.existsByEmail(email)).thenReturn(false);
            when(roleRepository.findByName(roleName.name())).thenReturn(Optional.of(role));
            when(passwordEncoder.encode("PlainPassword1")).thenReturn("bcrypt-hash");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            var response = userService.register(new RegisterRequest(
                    "Test", "User", email, "PlainPassword1", roleName));

            assertThat(response.roles()).containsExactly(roleName.name());
        }
    }

    @Test
    void rejectsAdminPublicRegistration() {
        RegisterRequest request = new RegisterRequest(
                "Admin", "User", "admin@example.com", "PlainPassword1", RoleName.ADMIN);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(com.ires.common.exception.ForbiddenException.class)
                .hasMessageContaining("public registration");
    }
}
