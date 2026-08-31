package com.ires.ai.controller;

import com.ires.ai.dto.AIAnalysisResponse;
import com.ires.ai.service.RequirementAIAnalysisService;
import com.ires.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RequirementAIAnalysisController {

    private final RequirementAIAnalysisService analysisService;

    @PostMapping("/api/v1/requirements/{requirementId}/ai-analysis")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST')")
    public ResponseEntity<ApiResponse<AIAnalysisResponse>> analyze(
            @PathVariable UUID requirementId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("AI analysis completed.", analysisService.analyze(requirementId, principal)));
    }

    @GetMapping("/api/v1/requirements/{requirementId}/ai-analysis")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST')")
    public ApiResponse<AIAnalysisResponse> get(
            @PathVariable UUID requirementId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ApiResponse.success("AI analysis loaded.", analysisService.get(requirementId, principal));
    }
}
