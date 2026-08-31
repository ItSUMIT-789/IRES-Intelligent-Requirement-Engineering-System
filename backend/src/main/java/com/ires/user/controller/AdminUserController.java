package com.ires.user.controller;

import com.ires.common.response.ApiResponse;
import com.ires.user.dto.AdminUserResponse;
import com.ires.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<AdminUserResponse>> listUsers() {
        return ApiResponse.success("Users loaded successfully.", userService.findAllForAdmin());
    }
}
