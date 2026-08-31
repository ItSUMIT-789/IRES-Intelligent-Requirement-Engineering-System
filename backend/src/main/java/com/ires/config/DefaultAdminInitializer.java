package com.ires.config;

import com.ires.user.entity.Role;
import com.ires.user.entity.RoleName;
import com.ires.user.entity.User;
import com.ires.user.repository.RoleRepository;
import com.ires.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DefaultAdminInitializer implements CommandLineRunner {

    static final String ADMIN_USERNAME = "abhay18";
    static final String ADMIN_EMAIL = "abhay18@ires.local";
    private static final String ADMIN_PASSWORD = "abhay45";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByUsernameIgnoreCase(ADMIN_USERNAME)) {
            return;
        }

        Role adminRole = roleRepository.findByName(RoleName.ADMIN.name())
                .orElseThrow(() -> new IllegalStateException("ADMIN role is not configured."));
        User admin = new User("Abhay", "Administrator", ADMIN_EMAIL,
                passwordEncoder.encode(ADMIN_PASSWORD), adminRole);
        admin.setUsername(ADMIN_USERNAME);
        userRepository.save(admin);
    }
}
