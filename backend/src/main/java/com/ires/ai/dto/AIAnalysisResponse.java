package com.ires.ai.dto;

import com.ires.ai.entity.AnalysisStatus;
import com.ires.ai.entity.RequirementAIAnalysis;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AIAnalysisResponse(
        UUID id,
        UUID requirementId,
        AnalysisStatus analysisStatus,
        String summary,
        BigDecimal ambiguityScore,
        BigDecimal completenessScore,
        BigDecimal qualityScore,
        String suggestions,
        Instant analyzedAt,
        Instant createdAt,
        Instant updatedAt
) {

    public static AIAnalysisResponse from(RequirementAIAnalysis analysis) {
        return new AIAnalysisResponse(
                analysis.getId(),
                analysis.getRequirement().getId(),
                analysis.getAnalysisStatus(),
                analysis.getSummary(),
                analysis.getAmbiguityScore(),
                analysis.getCompletenessScore(),
                analysis.getQualityScore(),
                analysis.getSuggestions(),
                analysis.getAnalyzedAt(),
                analysis.getCreatedAt(),
                analysis.getUpdatedAt()
        );
    }
}
