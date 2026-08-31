package com.ires.user.dto;

import com.ires.user.entity.User;
import com.ires.user.entity.Role;
import com.ires.user.entity.RoleName;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String username,
        String role,
        Set<String> roles,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {

    public static UserResponse from(User user) {
        String primaryRole = primaryRole(user);
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUsername(),
                primaryRole,
                user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toUnmodifiableSet()),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private static String primaryRole(User user) {
        if (user.getRoles().size() != 1) {
            throw new IllegalStateException("A user must have exactly one application role.");
        }
        return user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .map(RoleName::valueOf)
                .map(RoleName::name)
                .orElseThrow();
    }
}
