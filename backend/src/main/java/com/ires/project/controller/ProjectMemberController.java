package com.ires.project.controller;

import com.ires.common.response.ApiResponse;
import com.ires.project.dto.ProjectMemberRequest;
import com.ires.project.dto.ProjectMemberResponse;
import com.ires.project.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> add(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectMemberRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project member added.", projectMemberService.add(projectId, request, principal)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ProjectMemberResponse>> list(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("Project members loaded.", projectMemberService.list(projectId, principal));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<Void> remove(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        projectMemberService.remove(projectId, userId, principal);
        return ResponseEntity.noContent().build();
    }
}
