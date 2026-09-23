package com.ires.ai.config;

import com.ires.ai.service.AIAnalysisProvider;
import com.ires.ai.service.MockAIAnalysisProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AIAnalysisProviderConfiguration {

    @Bean
    public MockAIAnalysisProvider mockAIAnalysisProvider(
            @Value("${app.ai.mock-enabled:true}") boolean available
    ) {
        return new MockAIAnalysisProvider(available);
    }

    @Bean
    @Primary
    public AIAnalysisProvider aiAnalysisProvider(
            AIAnalysisProperties properties,
            MockAIAnalysisProvider mockAIAnalysisProvider
    ) {
        String provider = properties.getProvider();

        if ("mock".equalsIgnoreCase(provider)) {
            return mockAIAnalysisProvider;
        }

        throw new IllegalArgumentException(
                "Unsupported AI analysis provider: " + provider
        );
    }
}