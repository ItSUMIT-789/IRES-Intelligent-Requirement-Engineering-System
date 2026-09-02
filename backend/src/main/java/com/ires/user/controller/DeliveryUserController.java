package com.ires.user.controller;

import com.ires.common.response.ApiResponse;
import com.ires.user.dto.AdminUserResponse;
import com.ires.user.service.UserService;
import com.ires.project.service.ProjectService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class DeliveryUserController {
    private final UserService userService;
    private final ProjectService projectService;

    @GetMapping("/eligible-testers/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN','DEVELOPER')")
    public ApiResponse<List<AdminUserResponse>> eligibleTesters(@PathVariable UUID projectId) {
        return ApiResponse.success("Eligible project testers loaded.", userService.findEligibleTesters(projectId));
    }

    @GetMapping("/eligible-business-analysts/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    public ApiResponse<List<AdminUserResponse>> eligibleBusinessAnalysts(
            @PathVariable UUID projectId, @AuthenticationPrincipal UserDetails principal) {
        projectService.assertCanManage(projectService.findProject(projectId), principal);
        return ApiResponse.success("Eligible Business Analysts loaded.",
                userService.findEligibleBusinessAnalysts(projectId));
    }
}
