package com.ires.story.controller;

import com.ires.common.response.ApiResponse;
import com.ires.story.dto.UserStoryCreateRequest;
import com.ires.story.dto.UserStoryResponse;
import com.ires.story.dto.UserStoryUpdateRequest;
import com.ires.story.service.UserStoryService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserStoryController {

    private final UserStoryService userStoryService;

    @PostMapping("/api/v1/requirements/{requirementId}/user-stories")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST')")
    public ResponseEntity<ApiResponse<UserStoryResponse>> create(
            @PathVariable UUID requirementId,
            @Valid @RequestBody UserStoryCreateRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "User story created.", userStoryService.create(requirementId, request, principal)));
    }

    @GetMapping("/api/v1/requirements/{requirementId}/user-stories")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<UserStoryResponse>> list(
            @PathVariable UUID requirementId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("User stories loaded.", userStoryService.list(requirementId, pageable, principal));
    }

    @GetMapping("/api/v1/user-stories/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserStoryResponse> get(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("User story loaded.", userStoryService.get(id, principal));
    }

    @PutMapping("/api/v1/user-stories/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST')")
    public ApiResponse<UserStoryResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UserStoryUpdateRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("User story updated.", userStoryService.update(id, request, principal));
    }

    @DeleteMapping("/api/v1/user-stories/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        userStoryService.delete(id, principal);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/requirements/{requirementId}/user-stories/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST')")
    public ResponseEntity<ApiResponse<UserStoryResponse>> generate(
            @PathVariable UUID requirementId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "User story generated.", userStoryService.generate(requirementId, principal)));
    }
}
