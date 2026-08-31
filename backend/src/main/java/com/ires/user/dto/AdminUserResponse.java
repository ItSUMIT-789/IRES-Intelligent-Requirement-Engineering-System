package com.ires.user.dto;

import com.ires.user.entity.Role;
import com.ires.user.entity.RoleName;
import com.ires.user.entity.User;

import java.time.Instant;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String firstName,
        String lastName,
        String name,
        String email,
        String username,
        String role,
        String roleLabel,
        boolean active,
        Instant createdAt
) {
    public static AdminUserResponse from(User user) {
        if (user.getRoles().size() != 1) {
            throw new IllegalStateException("A user must have exactly one application role.");
        }
        RoleName role = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .map(RoleName::valueOf)
                .orElseThrow();
        return new AdminUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                (user.getFirstName() + " " + user.getLastName()).trim(),
                user.getEmail(),
                user.getUsername(),
                role.name(),
                roleLabel(role),
                user.isActive(),
                user.getCreatedAt()
        );
    }

    private static String roleLabel(RoleName role) {
        return switch (role) {
            case ADMIN -> "Admin";
            case BUSINESS_ANALYST -> "Business Analyst";
            case CLIENT -> "Client";
            case DEVELOPER -> "Developer";
            case TESTER -> "Tester";
        };
    }
}
