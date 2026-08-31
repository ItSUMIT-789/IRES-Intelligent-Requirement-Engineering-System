package com.ires.ai.service;

import com.ires.ai.dto.AIAnalysisResponse;
import com.ires.ai.entity.AnalysisStatus;
import com.ires.ai.entity.RequirementAIAnalysis;
import com.ires.ai.repository.RequirementAIAnalysisRepository;
import com.ires.common.exception.NotFoundException;
import com.ires.requirement.entity.Requirement;
import com.ires.requirement.service.RequirementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequirementAIAnalysisService {

    private final RequirementAIAnalysisRepository analysisRepository;
    private final RequirementService requirementService;
    private final AIAnalysisProvider analysisProvider;

    @Transactional
    public AIAnalysisResponse analyze(UUID requirementId, UserDetails principal) {
        Requirement requirement = requirementService.findAccessibleRequirement(requirementId, principal);
        RequirementAIAnalysis analysis = analysisRepository.findByRequirementId(requirementId)
                .orElseGet(() -> new RequirementAIAnalysis(requirement));
        analysis.setAnalysisStatus(AnalysisStatus.PROCESSING);
        analysis = analysisRepository.save(analysis);

        try {
            AIAnalysisProvider.AIAnalysisResult result = analysisProvider.analyze(requirement);
            analysis.setAnalysisStatus(AnalysisStatus.COMPLETED);
            analysis.setSummary(result.summary());
            analysis.setAmbiguityScore(result.ambiguityScore());
            analysis.setCompletenessScore(result.completenessScore());
            analysis.setQualityScore(result.qualityScore());
            analysis.setSuggestions(result.suggestions());
            analysis.setAnalyzedAt(Instant.now());
        } catch (RuntimeException exception) {
            analysis.setAnalysisStatus(AnalysisStatus.FAILED);
            analysis.setSummary("AI analysis could not be completed.");
            analysis.setSuggestions("Retry the analysis when the provider is available.");
            analysis.setAnalyzedAt(Instant.now());
        }

        return AIAnalysisResponse.from(analysisRepository.save(analysis));
    }

    public AIAnalysisResponse get(UUID requirementId, UserDetails principal) {
        requirementService.findAccessibleRequirement(requirementId, principal);
        return analysisRepository.findByRequirementId(requirementId)
                .map(AIAnalysisResponse::from)
                .orElseThrow(() -> new NotFoundException("AI analysis not found."));
    }
}
