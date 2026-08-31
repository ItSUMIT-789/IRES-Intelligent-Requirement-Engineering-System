package com.ires.config;

import com.ires.user.entity.Role;
import com.ires.user.entity.User;
import com.ires.user.repository.RoleRepository;
import com.ires.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultAdminInitializerTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks DefaultAdminInitializer initializer;

    @Test
    void createsOneAdminWithEncodedPassword() {
        Role adminRole = new Role("ADMIN");
        when(userRepository.existsByUsernameIgnoreCase("abhay18")).thenReturn(false);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(passwordEncoder.encode("abhay45")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        initializer.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User admin = captor.getValue();
        assertThat(admin.getUsername()).isEqualTo("abhay18");
        assertThat(admin.getPassword()).startsWith("$2a$");
        assertThat(admin.getPassword()).isNotEqualTo("abhay45");
        assertThat(admin.getRoles()).extracting(Role::getName).containsExactly("ADMIN");
    }

    @Test
    void doesNotCreateDuplicateAdmin() {
        when(userRepository.existsByUsernameIgnoreCase("abhay18")).thenReturn(true);

        initializer.run();

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}
