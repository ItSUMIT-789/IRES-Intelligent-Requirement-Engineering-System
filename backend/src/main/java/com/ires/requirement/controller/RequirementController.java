package com.ires.requirement.controller;

import com.ires.common.response.ApiResponse;
import com.ires.requirement.dto.RequirementCreateRequest;
import com.ires.requirement.dto.RequirementResponse;
import com.ires.requirement.dto.RequirementUpdateRequest;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.requirement.entity.RequirementStatus;
import com.ires.requirement.service.RequirementService;
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
public class RequirementController {

    private final RequirementService requirementService;

    @PostMapping("/api/v1/projects/{projectId}/requirements")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<ApiResponse<RequirementResponse>> create(
            @PathVariable UUID projectId,
            @Valid @RequestBody RequirementCreateRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Requirement created.", requirementService.create(projectId, request, principal)));
    }

    @GetMapping("/api/v1/projects/{projectId}/requirements")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<RequirementResponse>> list(
            @PathVariable UUID projectId,
            @RequestParam(required = false) RequirementStatus status,
            @RequestParam(required = false) RequirementPriority priority,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("Requirements loaded.",
                requirementService.list(projectId, status, priority, search, pageable, principal));
    }

    @GetMapping("/api/v1/requirements/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<RequirementResponse> get(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("Requirement loaded.", requirementService.get(id, principal));
    }

    @GetMapping("/api/v1/requirements")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<RequirementResponse>> listAccessible(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) RequirementStatus status,
            @RequestParam(required = false) RequirementPriority priority,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("Requirements loaded.", requirementService.listAccessible(
                projectId, status, priority, search, pageable, principal));
    }

    @GetMapping("/api/v1/requirements/analyst-queue")
    @PreAuthorize("hasRole('BUSINESS_ANALYST')")
    public ApiResponse<Page<RequirementResponse>> analystQueue(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) RequirementStatus status,
            @RequestParam(required = false) RequirementPriority priority,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("Analyst queue loaded.", requirementService.listAnalystQueue(
                projectId, status, priority, search, pageable, principal));
    }

    @PutMapping("/api/v1/requirements/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'BUSINESS_ANALYST')")
    public ApiResponse<RequirementResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody RequirementUpdateRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("Requirement updated.", requirementService.update(id, request, principal));
    }

    @DeleteMapping("/api/v1/requirements/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'BUSINESS_ANALYST')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        requirementService.delete(id, principal);
        return ResponseEntity.noContent().build();
    }
}
