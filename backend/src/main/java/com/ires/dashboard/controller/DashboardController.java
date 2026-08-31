package com.ires.dashboard.controller;

import com.ires.common.response.ApiResponse;
import com.ires.dashboard.dto.DashboardResponse;
import com.ires.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<DashboardResponse> get(@AuthenticationPrincipal UserDetails principal) {
        return ApiResponse.success("Dashboard summary loaded.", dashboardService.get(principal));
    }
}
