package com.ires.ai.provider.nvidia;

import com.ires.ai.dto.analysis.ClassificationRequest;
import com.ires.ai.dto.analysis.ClassificationResponse;
import com.ires.ai.provider.nvidia.dto.NvidiaChatRequest;
import com.ires.ai.provider.nvidia.dto.NvidiaChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NvidiaAIAnalysisProviderTest {

    private NvidiaApiClient client;
    private NvidiaAIAnalysisProvider provider;

    @BeforeEach
    void setUp() {
        client = mock(NvidiaApiClient.class);
        provider = new NvidiaAIAnalysisProvider(client, "test-model");
    }

    @Test
    void classifyReturnsValidClassification() {
        NvidiaChatResponse response = chatResponse(
                "{\"classification\": \"FUNCTIONAL\", \"confidence\": 0.94, \"reason\": \"Describes system behavior.\"}"
        );

        when(client.chatCompletion(any())).thenReturn(response);

        ClassificationRequest request = new ClassificationRequest(
                UUID.randomUUID(),
                "The system shall allow users to log in."
        );

        ClassificationResponse result = provider.classify(request);

        assertEquals("FUNCTIONAL", result.classification());
        assertEquals(new BigDecimal("0.94"), result.confidence());
        assertEquals("Describes system behavior.", result.reason());

        verify(client).chatCompletion(any(NvidiaChatRequest.class));
    }

    @Test
    void classifyHandlesAllValidClassifications() {
        for (String category : List.of(
                "FUNCTIONAL", "NON_FUNCTIONAL", "BUSINESS", "TECHNICAL", "UNCLEAR"
        )) {
            NvidiaChatResponse response = chatResponse(
                    "{\"classification\": \"" + category + "\", \"confidence\": 0.85, \"reason\": \"Test reason.\"}"
            );

            when(client.chatCompletion(any())).thenReturn(response);

            ClassificationResponse result = provider.classify(
                    new ClassificationRequest(UUID.randomUUID(), "Some requirement.")
            );

            assertEquals(category, result.classification());
        }
    }

    @Test
    void classifyUsesConfiguredModel() {
        NvidiaChatResponse response = chatResponse(
                "{\"classification\": \"FUNCTIONAL\", \"confidence\": 0.90, \"reason\": \"Test.\"}"
        );

        when(client.chatCompletion(any())).thenReturn(response);

        provider.classify(new ClassificationRequest(
                UUID.randomUUID(), "Some requirement."
        ));

        verify(client).chatCompletion(argThat(request ->
                "test-model".equals(request.model())
        ));
    }

    @Test
    void classifyFallsBackToDefaultModelWhenBlank() {
        NvidiaAIAnalysisProvider providerNoModel =
                new NvidiaAIAnalysisProvider(client, "");

        NvidiaChatResponse response = chatResponse(
                "{\"classification\": \"FUNCTIONAL\", \"confidence\": 0.90, \"reason\": \"Test.\"}"
        );

        when(client.chatCompletion(any())).thenReturn(response);

        providerNoModel.classify(new ClassificationRequest(
                UUID.randomUUID(), "Some requirement."
        ));

        verify(client).chatCompletion(argThat(request ->
                "meta/llama-3.1-70b-instruct".equals(request.model())
        ));
    }

    @Test
    void classifyRejectsNullResponse() {
        when(client.chatCompletion(any())).thenReturn(null);

        ClassificationRequest request = new ClassificationRequest(
                UUID.randomUUID(), "Some requirement."
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.classify(request)
        );

        assertEquals(
                "NVIDIA returned an empty response.",
                exception.getMessage()
        );
    }

    @Test
    void classifyRejectsEmptyChoices() {
        NvidiaChatResponse response = new NvidiaChatResponse(List.of());

        when(client.chatCompletion(any())).thenReturn(response);

        ClassificationRequest request = new ClassificationRequest(
                UUID.randomUUID(), "Some requirement."
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.classify(request)
        );

        assertEquals(
                "NVIDIA response did not contain any choices.",
                exception.getMessage()
        );
    }

    @Test
    void classifyRejectsNullChoices() {
        NvidiaChatResponse response = new NvidiaChatResponse(null);

        when(client.chatCompletion(any())).thenReturn(response);

        ClassificationRequest request = new ClassificationRequest(
                UUID.randomUUID(), "Some requirement."
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.classify(request)
        );

        assertEquals(
                "NVIDIA response did not contain any choices.",
                exception.getMessage()
        );
    }

    @Test
    void classifyRejectsBlankMessageContent() {
        NvidiaChatResponse response = new NvidiaChatResponse(
                List.of(new NvidiaChatResponse.Choice(
                        new NvidiaChatResponse.Message("assistant", "   ")
                ))
        );

        when(client.chatCompletion(any())).thenReturn(response);

        ClassificationRequest request = new ClassificationRequest(
                UUID.randomUUID(), "Some requirement."
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.classify(request)
        );

        assertEquals(
                "NVIDIA response did not contain message content.",
                exception.getMessage()
        );
    }

    @Test
    void classifyRejectsInvalidJson() {
        NvidiaChatResponse response = chatResponse("not valid json at all");

        when(client.chatCompletion(any())).thenReturn(response);

        ClassificationRequest request = new ClassificationRequest(
                UUID.randomUUID(), "Some requirement."
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.classify(request)
        );

        assertTrue(exception.getMessage().contains("not valid JSON"));
    }

    @Test
    void classifyRejectsInvalidClassification() {
        NvidiaChatResponse response = chatResponse(
                "{\"classification\": \"INVALID_TYPE\", \"confidence\": 0.85, \"reason\": \"Test.\"}"
        );

        when(client.chatCompletion(any())).thenReturn(response);

        ClassificationRequest request = new ClassificationRequest(
                UUID.randomUUID(), "Some requirement."
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.classify(request)
        );

        assertTrue(exception.getMessage().contains("invalid classification"));
    }

    @Test
    void classifyRejectsMissingClassification() {
        NvidiaChatResponse response = chatResponse(
                "{\"confidence\": 0.85, \"reason\": \"Test.\"}"
        );

        when(client.chatCompletion(any())).thenReturn(response);

        ClassificationRequest request = new ClassificationRequest(
                UUID.randomUUID(), "Some requirement."
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.classify(request)
        );

        assertTrue(exception.getMessage().contains("classification"));
    }

    @Test
    void classifyRejectsMissingConfidence() {
        NvidiaChatResponse response = chatResponse(
                "{\"classification\": \"FUNCTIONAL\", \"reason\": \"Test.\"}"
        );

        when(client.chatCompletion(any())).thenReturn(response);

        ClassificationRequest request = new ClassificationRequest(
                UUID.randomUUID(), "Some requirement."
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.classify(request)
        );

        assertTrue(exception.getMessage().contains("confidence"));
    }

    @Test
    void classifyRejectsMissingReason() {
        NvidiaChatResponse response = chatResponse(
                "{\"classification\": \"FUNCTIONAL\", \"confidence\": 0.90, \"reason\": \"\"}"
        );

        when(client.chatCompletion(any())).thenReturn(response);

        ClassificationRequest request = new ClassificationRequest(
                UUID.randomUUID(), "Some requirement."
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.classify(request)
        );

        assertTrue(exception.getMessage().contains("reason"));
    }

    @Test
    void classifyRejectsConfidenceOutOfRange() {
        NvidiaChatResponse response = chatResponse(
                "{\"classification\": \"FUNCTIONAL\", \"confidence\": 1.5, \"reason\": \"Test.\"}"
        );

        when(client.chatCompletion(any())).thenReturn(response);

        ClassificationRequest request = new ClassificationRequest(
                UUID.randomUUID(), "Some requirement."
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.classify(request)
        );

        assertTrue(exception.getMessage().contains("out of range"));
    }

    @Test
    void classifyStripsMarkdownCodeFences() {
        NvidiaChatResponse response = chatResponse(
                "```json\n{\"classification\": \"BUSINESS\", \"confidence\": 0.88, \"reason\": \"Business rule.\"}\n```"
        );

        when(client.chatCompletion(any())).thenReturn(response);

        ClassificationResponse result = provider.classify(
                new ClassificationRequest(UUID.randomUUID(), "Some requirement.")
        );

        assertEquals("BUSINESS", result.classification());
        assertEquals(new BigDecimal("0.88"), result.confidence());
        assertEquals("Business rule.", result.reason());
    }

    @Test
    void classifyHandlesLowercaseClassificationByUppercasing() {
        NvidiaChatResponse response = chatResponse(
                "{\"classification\": \"functional\", \"confidence\": 0.90, \"reason\": \"Test reason.\"}"
        );

        when(client.chatCompletion(any())).thenReturn(response);

        ClassificationResponse result = provider.classify(
                new ClassificationRequest(UUID.randomUUID(), "Some requirement.")
        );

        assertEquals("FUNCTIONAL", result.classification());
    }

    private NvidiaChatResponse chatResponse(String content) {
        return new NvidiaChatResponse(
                List.of(new NvidiaChatResponse.Choice(
                        new NvidiaChatResponse.Message("assistant", content)
                ))
        );
    }
}
