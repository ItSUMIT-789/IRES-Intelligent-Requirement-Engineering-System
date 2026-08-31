package com.ires.ai.service;

import com.ires.requirement.entity.Requirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MockAIAnalysisProvider implements AIAnalysisProvider {

    private final boolean available;

    public MockAIAnalysisProvider(@Value("${app.ai.mock-enabled:true}") boolean available) {
        this.available = available;
    }

    @Override
    public AIAnalysisResult analyze(Requirement requirement) {
        if (!available) {
            throw new IllegalStateException("AI analysis provider is unavailable.");
        }
        String title = requirement.getTitle();
        return new AIAnalysisResult(
                "Development analysis generated for: " + title,
                new BigDecimal("18.00"),
                new BigDecimal("82.00"),
                new BigDecimal("82.00"),
                "Clarify measurable outcomes and acceptance conditions."
        );
    }
}
