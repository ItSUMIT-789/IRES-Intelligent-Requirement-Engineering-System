package com.ires.ai.controller;

import com.ires.ai.dto.AIAnalysisResponse;
import com.ires.ai.dto.analysis.AmbiguityResponse;
import com.ires.ai.dto.analysis.CandidateRequirementsRequest;
import com.ires.ai.dto.analysis.ClassificationResponse;
import com.ires.ai.dto.analysis.CompletenessResponse;
import com.ires.ai.dto.analysis.ConflictDetectionResponse;
import com.ires.ai.dto.analysis.DuplicateDetectionResponse;
import com.ires.ai.dto.analysis.QualityAnalysisResponse;
import com.ires.ai.service.RequirementAIAnalysisService;
import com.ires.common.exception.BadRequestException;
import com.ires.common.response.ApiResponse;
import com.ires.requirement.service.RequirementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RequirementAIAnalysisController {

    private final RequirementAIAnalysisService analysisService;
    private final RequirementService requirementService;

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

    @PostMapping("/api/v1/requirements/{requirementId}/ai/classify")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST')")
    public ClassificationResponse classify(
            @PathVariable UUID requirementId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        assertAccessible(requirementId, principal);
        return analysisService.classify(requirementId);
    }

    @PostMapping("/api/v1/requirements/{requirementId}/ai/ambiguity")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST')")
    public AmbiguityResponse detectAmbiguity(
            @PathVariable UUID requirementId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        assertAccessible(requirementId, principal);
        return analysisService.detectAmbiguity(requirementId);
    }

    @PostMapping("/api/v1/requirements/{requirementId}/ai/completeness")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST')")
    public CompletenessResponse analyzeCompleteness(
            @PathVariable UUID requirementId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        assertAccessible(requirementId, principal);
        return analysisService.analyzeCompleteness(requirementId);
    }

    @PostMapping("/api/v1/requirements/{requirementId}/ai/quality")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST')")
    public QualityAnalysisResponse analyzeQuality(
            @PathVariable UUID requirementId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        assertAccessible(requirementId, principal);
        return analysisService.analyzeQuality(requirementId);
    }

    @PostMapping("/api/v1/requirements/{requirementId}/ai/duplicates")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST')")
    public DuplicateDetectionResponse detectDuplicates(
            @PathVariable UUID requirementId,
            @RequestBody CandidateRequirementsRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        assertAccessible(requirementId, principal);
        List<UUID> candidateRequirementIds = candidateRequirementIds(request);
        assertCandidatesAccessible(candidateRequirementIds, principal);
        return analysisService.detectDuplicates(requirementId, candidateRequirementIds);
    }

    @PostMapping("/api/v1/requirements/{requirementId}/ai/conflicts")
    @PreAuthorize("hasAnyRole('ADMIN', 'BUSINESS_ANALYST')")
    public ConflictDetectionResponse detectConflicts(
            @PathVariable UUID requirementId,
            @RequestBody CandidateRequirementsRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        assertAccessible(requirementId, principal);
        List<UUID> candidateRequirementIds = candidateRequirementIds(request);
        assertCandidatesAccessible(candidateRequirementIds, principal);
        return analysisService.detectConflicts(requirementId, candidateRequirementIds);
    }

    private List<UUID> candidateRequirementIds(CandidateRequirementsRequest request) {
        if (request == null) {
            throw new BadRequestException("Candidate requirement IDs request is required.");
        }
        return request.candidateRequirementIds();
    }

    private void assertAccessible(UUID requirementId, UserDetails principal) {
        requirementService.findAccessibleRequirement(requirementId, principal);
    }

    private void assertCandidatesAccessible(List<UUID> candidateRequirementIds, UserDetails principal) {
        if (candidateRequirementIds == null) {
            return;
        }
        candidateRequirementIds.forEach(candidateRequirementId ->
                requirementService.findAccessibleRequirement(candidateRequirementId, principal));
    }
}
