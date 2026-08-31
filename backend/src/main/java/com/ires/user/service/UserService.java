package com.ires.user.service;

import com.ires.user.dto.UserResponse;
import com.ires.user.dto.AdminUserResponse;
import com.ires.common.exception.ConflictException;
import com.ires.user.dto.RegisterRequest;
import com.ires.user.entity.Role;
import com.ires.user.entity.RoleName;
import com.ires.user.entity.User;
import com.ires.common.exception.ForbiddenException;
import com.ires.user.repository.RoleRepository;
import com.ires.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists.");
        }

        RoleName requestedRole = request.role();
        if (!requestedRole.canBeSelfRegistered()) {
            throw new ForbiddenException("The requested role cannot be assigned through public registration.");
        }
        Role role = roleRepository.findByName(requestedRole.name())
                .orElseThrow(() -> new IllegalStateException("Requested role is not configured."));
        User user = new User(
                request.firstName().trim(),
                request.lastName().trim(),
                email,
                passwordEncoder.encode(request.password()),
                role
        );
        return UserResponse.from(userRepository.save(user));
    }

    public Optional<UserResponse> findByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email)).map(UserResponse::from);
    }

    public Optional<UserResponse> findByLogin(String login) {
        return findUserByLogin(login).map(UserResponse::from);
    }

    public Optional<User> findUserByLogin(String login) {
        String normalized = normalizeEmail(login);
        return userRepository.findByEmail(normalized)
                .or(() -> userRepository.findByUsernameIgnoreCase(normalized));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(normalizeEmail(email));
    }

    public Optional<UserResponse> findById(UUID id) {
        return userRepository.findById(id).map(UserResponse::from);
    }

    public Optional<Role> findRoleByName(String name) {
        return roleRepository.findByName(name);
    }

    public List<AdminUserResponse> findAllForAdmin() {
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
