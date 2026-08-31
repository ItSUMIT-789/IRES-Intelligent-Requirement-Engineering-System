package com.ires.project.dto;

import com.ires.user.entity.User;

import java.util.UUID;

public record UserSummary(UUID id, String name, String email) {

    public static UserSummary from(User user) {
        return new UserSummary(user.getId(), user.getFirstName() + " " + user.getLastName(), user.getEmail());
    }
}
