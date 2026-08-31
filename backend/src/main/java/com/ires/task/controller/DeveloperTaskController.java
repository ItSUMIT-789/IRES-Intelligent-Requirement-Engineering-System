package com.ires.task.controller;

import com.ires.common.response.ApiResponse;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.task.dto.DeveloperTaskCreateRequest;
import com.ires.task.dto.DeveloperTaskResponse;
import com.ires.task.dto.DeveloperTaskUpdateRequest;
import com.ires.task.entity.TaskStatus;
import com.ires.task.service.DeveloperTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DeveloperTaskController {

    private final DeveloperTaskService taskService;

    @PostMapping("/api/v1/projects/{projectId}/tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<DeveloperTaskResponse>> create(
            @PathVariable UUID projectId,
            @Valid @RequestBody DeveloperTaskCreateRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Developer task created.", taskService.create(projectId, request, principal)));
    }

    @GetMapping("/api/v1/projects/{projectId}/tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST', 'DEVELOPER')")
    public ApiResponse<Page<DeveloperTaskResponse>> list(
            @PathVariable UUID projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) RequirementPriority priority,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("Developer tasks loaded.",
                taskService.list(projectId, status, assigneeId, priority, pageable, principal));
    }

    @GetMapping("/api/v1/tasks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST', 'DEVELOPER')")
    public ApiResponse<DeveloperTaskResponse> get(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("Developer task loaded.", taskService.get(id, principal));
    }

    @PutMapping("/api/v1/tasks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST', 'DEVELOPER')")
    public ApiResponse<DeveloperTaskResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody DeveloperTaskUpdateRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("Developer task updated.", taskService.update(id, request, principal));
    }

    @DeleteMapping("/api/v1/tasks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        taskService.delete(id, principal);
        return ResponseEntity.noContent().build();
    }
}
