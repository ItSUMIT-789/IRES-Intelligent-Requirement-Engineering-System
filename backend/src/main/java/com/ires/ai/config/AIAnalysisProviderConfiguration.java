package com.ires.ai.config;

import com.ires.ai.provider.jev.JevAIAnalysisProvider;
import com.ires.ai.provider.jev.JevApiClient;
import com.ires.ai.service.AIAnalysisProvider;
import com.ires.ai.service.MockAIAnalysisProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class AIAnalysisProviderConfiguration {

    @Bean
    public MockAIAnalysisProvider mockAIAnalysisProvider(
            @Value("${app.ai.mock-enabled:true}") boolean available
    ) {
        return new MockAIAnalysisProvider(available);
    }

    @Bean
    public JevApiClient jevApiClient(
            RestClient.Builder restClientBuilder,
            AIAnalysisProperties properties
    ) {
        return new JevApiClient(
                restClientBuilder,
                properties
        );
    }

    @Bean
    public JevAIAnalysisProvider jevAIAnalysisProvider(
            JevApiClient client,
            AIAnalysisProperties properties
    ) {
        return new JevAIAnalysisProvider(
                client,
                properties.getModel()
        );
    }

    @Bean
    @Primary
    public AIAnalysisProvider aiAnalysisProvider(
            AIAnalysisProperties properties,
            MockAIAnalysisProvider mockAIAnalysisProvider,
            JevAIAnalysisProvider jevAIAnalysisProvider
    ) {
        String provider = properties.getProvider();

        if ("mock".equalsIgnoreCase(provider)) {
            return mockAIAnalysisProvider;
        }

        if ("jev".equalsIgnoreCase(provider)) {
            return jevAIAnalysisProvider;
        }

        throw new IllegalArgumentException(
                "Unsupported AI analysis provider: " + provider
        );
    }
}