package com.ires.ai.service;

import com.ires.requirement.entity.Requirement;

import java.math.BigDecimal;

public interface AIAnalysisProvider {

    AIAnalysisResult analyze(Requirement requirement);

    record AIAnalysisResult(
            String summary,
            BigDecimal ambiguityScore,
            BigDecimal completenessScore,
            BigDecimal qualityScore,
            String suggestions
    ) {
    }
}
