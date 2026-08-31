package com.ires.requirement.comment.controller;

import com.ires.common.response.ApiResponse;
import com.ires.requirement.comment.dto.CommentCreateRequest;
import com.ires.requirement.comment.dto.CommentResponse;
import com.ires.requirement.comment.service.RequirementCommentService;
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
public class RequirementCommentController {

    private final RequirementCommentService commentService;

    @PostMapping("/api/v1/requirements/{requirementId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            @PathVariable UUID requirementId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment created.", commentService.create(requirementId, request, principal)));
    }

    @GetMapping("/api/v1/requirements/{requirementId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<CommentResponse>> list(
            @PathVariable UUID requirementId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("Comments loaded.", commentService.list(requirementId, pageable, principal));
    }

    @PutMapping("/api/v1/comments/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CommentResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("Comment updated.", commentService.update(id, request, principal));
    }

    @DeleteMapping("/api/v1/comments/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal
    ) {
        commentService.delete(id, principal);
        return ResponseEntity.noContent().build();
    }
}
