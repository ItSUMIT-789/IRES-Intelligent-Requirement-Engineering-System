package com.ires.bug.controller;

import com.ires.bug.dto.BugCreateRequest;
import com.ires.bug.dto.BugResponse;
import com.ires.bug.dto.BugUpdateRequest;
import com.ires.bug.entity.BugSeverity;
import com.ires.bug.entity.BugStatus;
import com.ires.bug.service.BugService;
import com.ires.common.response.ApiResponse;
import com.ires.requirement.entity.RequirementPriority;
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
public class BugController {

    private final BugService bugService;

    @PostMapping("/api/v1/projects/{projectId}/bugs")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST', 'DEVELOPER', 'TESTER')")
    public ResponseEntity<ApiResponse<BugResponse>> create(
            @PathVariable UUID projectId,
            @Valid @RequestBody BugCreateRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Bug created.", bugService.create(projectId, request, principal)));
    }

    @GetMapping("/api/v1/projects/{projectId}/bugs")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST', 'DEVELOPER', 'TESTER')")
    public ApiResponse<Page<BugResponse>> list(
            @PathVariable UUID projectId,
            @RequestParam(required = false) BugStatus status,
            @RequestParam(required = false) BugSeverity severity,
            @RequestParam(required = false) RequirementPriority priority,
            @RequestParam(required = false) UUID assigneeId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("Bugs loaded.", bugService.list(projectId, status, severity, priority, assigneeId, pageable, principal));
    }

    @GetMapping("/api/v1/bugs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST', 'DEVELOPER', 'TESTER')")
    public ApiResponse<BugResponse> get(@PathVariable UUID id, @AuthenticationPrincipal UserDetails principal) {
        return ApiResponse.success("Bug loaded.", bugService.get(id, principal));
    }

    @PutMapping("/api/v1/bugs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST', 'DEVELOPER', 'TESTER')")
    public ApiResponse<BugResponse> update(@PathVariable UUID id, @Valid @RequestBody BugUpdateRequest request,
                                            @AuthenticationPrincipal UserDetails principal) {
        return ApiResponse.success("Bug updated.", bugService.update(id, request, principal));
    }

    @DeleteMapping("/api/v1/bugs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST', 'TESTER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal UserDetails principal) {
        bugService.delete(id, principal);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/bugs/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST', 'DEVELOPER', 'TESTER')")
    public ApiResponse<BugResponse> resolve(@PathVariable UUID id, @AuthenticationPrincipal UserDetails principal) {
        return ApiResponse.success("Bug resolved.", bugService.resolve(id, principal));
    }

    @PostMapping("/api/v1/bugs/{id}/reopen")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST', 'DEVELOPER', 'TESTER')")
    public ApiResponse<BugResponse> reopen(@PathVariable UUID id, @AuthenticationPrincipal UserDetails principal) {
        return ApiResponse.success("Bug reopened.", bugService.reopen(id, principal));
    }

    @PostMapping("/api/v1/test-case-executions/{executionId}/bugs")
    @PreAuthorize("hasAnyRole('ADMIN', 'TESTER')")
    public ResponseEntity<ApiResponse<BugResponse>> createFromExecution(
            @PathVariable UUID executionId,
            @Valid @RequestBody BugCreateRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Bug created from failed execution.", bugService.createFromFailedExecution(executionId, request, principal)));
    }
}
