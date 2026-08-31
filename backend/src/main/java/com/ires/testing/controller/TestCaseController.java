package com.ires.testing.controller;

import com.ires.common.response.ApiResponse;
import com.ires.requirement.entity.RequirementPriority;
import com.ires.testing.dto.TestCaseCreateRequest;
import com.ires.testing.dto.TestCaseExecutionRequest;
import com.ires.testing.dto.TestCaseExecutionResponse;
import com.ires.testing.dto.TestCaseResponse;
import com.ires.testing.dto.TestCaseUpdateRequest;
import com.ires.testing.entity.TestCaseStatus;
import com.ires.testing.service.TestCaseService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TestCaseController {

    private final TestCaseService testCaseService;

    @PostMapping("/api/v1/projects/{projectId}/test-cases")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<TestCaseResponse>> create(
            @PathVariable UUID projectId,
            @Valid @RequestBody TestCaseCreateRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Test case created.", testCaseService.create(projectId, request, principal)));
    }

    @GetMapping("/api/v1/projects/{projectId}/test-cases")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST', 'DEVELOPER', 'TESTER')")
    public ApiResponse<Page<TestCaseResponse>> list(
            @PathVariable UUID projectId,
            @RequestParam(required = false) RequirementPriority priority,
            @RequestParam(required = false) TestCaseStatus status,
            @RequestParam(required = false) UUID assigneeId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("Test cases loaded.",
                testCaseService.list(projectId, priority, status, assigneeId, pageable, principal));
    }

    @GetMapping("/api/v1/test-cases/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST', 'DEVELOPER', 'TESTER')")
    public ApiResponse<TestCaseResponse> get(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("Test case loaded.", testCaseService.get(id, principal));
    }

    @PutMapping("/api/v1/test-cases/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST', 'DEVELOPER')")
    public ApiResponse<TestCaseResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody TestCaseUpdateRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("Test case updated.", testCaseService.update(id, request, principal));
    }

    @DeleteMapping("/api/v1/test-cases/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        testCaseService.delete(id, principal);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/test-cases/{id}/execute")
    @PreAuthorize("hasAnyRole('ADMIN', 'TESTER')")
    public ResponseEntity<ApiResponse<TestCaseExecutionResponse>> execute(
            @PathVariable UUID id,
            @Valid @RequestBody TestCaseExecutionRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Test case execution recorded.", testCaseService.execute(id, request, principal)));
    }

    @GetMapping("/api/v1/test-cases/{id}/executions")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST', 'DEVELOPER', 'TESTER')")
    public ApiResponse<List<TestCaseExecutionResponse>> executions(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("Test case executions loaded.", testCaseService.executions(id, principal));
    }
}
