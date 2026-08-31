package com.ires.auth.dto;

import com.ires.user.dto.UserResponse;

public record AuthResponse(String token, UserResponse user) {
}
