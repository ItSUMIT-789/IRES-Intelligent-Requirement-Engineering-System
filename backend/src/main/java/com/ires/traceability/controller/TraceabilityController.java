package com.ires.traceability.controller;

import com.ires.common.response.ApiResponse;
import com.ires.traceability.dto.TraceabilityLinkRequest;
import com.ires.traceability.dto.TraceabilityLinkResponse;
import com.ires.traceability.service.TraceabilityService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TraceabilityController {

    private final TraceabilityService traceabilityService;

    @GetMapping("/api/v1/requirements/{requirementId}/traceability")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<TraceabilityLinkResponse>> list(
            @PathVariable UUID requirementId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("Traceability links loaded.",
                traceabilityService.list(requirementId, pageable, principal));
    }

    @PostMapping("/api/v1/requirements/{requirementId}/traceability/links")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST')")
    public ResponseEntity<ApiResponse<TraceabilityLinkResponse>> create(
            @PathVariable UUID requirementId,
            @Valid @RequestBody TraceabilityLinkRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Traceability link created.", traceabilityService.create(requirementId, request, principal)));
    }

    @DeleteMapping("/api/v1/traceability-links/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        traceabilityService.delete(id, principal);
        return ResponseEntity.noContent().build();
    }
}
