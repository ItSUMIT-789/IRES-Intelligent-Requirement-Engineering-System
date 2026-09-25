package com.ires.ai.provider.nvidia;

import com.ires.ai.dto.analysis.AmbiguityFinding;
import com.ires.ai.dto.analysis.AmbiguityRequest;
import com.ires.ai.dto.analysis.AmbiguityResponse;
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
import static org.mockito.Mockito.verifyNoInteractions;

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
    void classifyRejectsMissingModelConfiguration() {
        NvidiaAIAnalysisProvider providerNoModel =
                new NvidiaAIAnalysisProvider(client, "");

        ClassificationRequest request = new ClassificationRequest(
                UUID.randomUUID(),
                "Some requirement."
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> providerNoModel.classify(request)
        );

        assertEquals(
                "NVIDIA analysis model is not configured.",
                exception.getMessage()
        );

        verifyNoInteractions(client);
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
/* ------------------------------------------------------------------------------------------------------------------------------------------------------------*/
    @Test
    void detectAmbiguityReturnsFindingsForAmbiguousRequirement() {
        NvidiaChatResponse response = chatResponse(
                """
                {
                  "hasAmbiguity": true,
                  "findings": [
                    {
                      "text": "respond quickly",
                      "reason": "The word 'quickly' does not define a measurable response time.",
                      "suggestion": "Specify the maximum response time, for example within 2 seconds."
                    }
                  ],
                  "confidence": 0.89
                }
                """
        );

        when(client.chatCompletion(any())).thenReturn(response);

        AmbiguityRequest request = new AmbiguityRequest(
                UUID.randomUUID(),
                "The system shall respond quickly to user requests."
        );

        AmbiguityResponse result = provider.detectAmbiguity(request);

        assertTrue(result.hasAmbiguity());
        assertEquals(1, result.findings().size());

        AmbiguityFinding finding = result.findings().get(0);

        assertEquals("respond quickly", finding.text());
        assertEquals(
                "The word 'quickly' does not define a measurable response time.",
                finding.reason()
        );
        assertEquals(
                "Specify the maximum response time, for example within 2 seconds.",
                finding.suggestion()
        );
        assertEquals(new BigDecimal("0.89"), result.confidence());

        verify(client).chatCompletion(any(NvidiaChatRequest.class));
    }

    @Test
    void detectAmbiguityReturnsNoFindingsForClearRequirement() {
        NvidiaChatResponse response = chatResponse(
                """
                {
                  "hasAmbiguity": false,
                  "findings": [],
                  "confidence": 0.95
                }
                """
        );

        when(client.chatCompletion(any())).thenReturn(response);

        AmbiguityRequest request = new AmbiguityRequest(
                UUID.randomUUID(),
                "The system shall return a response within 2 seconds."
        );

        AmbiguityResponse result = provider.detectAmbiguity(request);

        assertFalse(result.hasAmbiguity());
        assertTrue(result.findings().isEmpty());
        assertEquals(new BigDecimal("0.95"), result.confidence());
    }

    @Test
    void detectAmbiguityMapsMultipleFindings() {
        NvidiaChatResponse response = chatResponse(
                """
                {
                  "hasAmbiguity": true,
                  "findings": [
                    {
                      "text": "quickly",
                      "reason": "Response time is not measurable.",
                      "suggestion": "Specify a maximum response time."
                    },
                    {
                      "text": "user-friendly",
                      "reason": "The term does not define measurable usability criteria.",
                      "suggestion": "Define concrete usability requirements."
                    }
                  ],
                  "confidence": 0.91
                }
                """
        );

        when(client.chatCompletion(any())).thenReturn(response);

        AmbiguityResponse result = provider.detectAmbiguity(
                new AmbiguityRequest(
                        UUID.randomUUID(),
                        "The system shall respond quickly and provide a user-friendly interface."
                )
        );

        assertTrue(result.hasAmbiguity());
        assertEquals(2, result.findings().size());

        assertEquals("quickly", result.findings().get(0).text());
        assertEquals("user-friendly", result.findings().get(1).text());

        assertEquals(new BigDecimal("0.91"), result.confidence());
    }

    @Test
    void detectAmbiguityRejectsMissingHasAmbiguity() {
        NvidiaChatResponse response = chatResponse(
                """
                {
                  "findings": [],
                  "confidence": 0.90
                }
                """
        );

        when(client.chatCompletion(any())).thenReturn(response);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.detectAmbiguity(
                        new AmbiguityRequest(
                                UUID.randomUUID(),
                                "Some requirement."
                        )
                )
        );

        assertTrue(exception.getMessage().contains("hasAmbiguity"));
    }

    @Test
    void detectAmbiguityRejectsMissingConfidence() {
        NvidiaChatResponse response = chatResponse(
                """
                {
                  "hasAmbiguity": false,
                  "findings": []
                }
                """
        );

        when(client.chatCompletion(any())).thenReturn(response);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.detectAmbiguity(
                        new AmbiguityRequest(
                                UUID.randomUUID(),
                                "Some requirement."
                        )
                )
        );

        assertTrue(exception.getMessage().contains("confidence"));
    }

    @Test
    void detectAmbiguityRejectsIncompleteFinding() {
        NvidiaChatResponse response = chatResponse(
                """
                {
                  "hasAmbiguity": true,
                  "findings": [
                    {
                      "text": "quickly",
                      "reason": "Response time is not measurable."
                    }
                  ],
                  "confidence": 0.90
                }
                """
        );

        when(client.chatCompletion(any())).thenReturn(response);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.detectAmbiguity(
                        new AmbiguityRequest(
                                UUID.randomUUID(),
                                "The system shall respond quickly."
                        )
                )
        );

        assertTrue(exception.getMessage().contains("suggestion"));
    }

    @Test
    void detectAmbiguityRejectsConfidenceOutOfRange() {
        NvidiaChatResponse response = chatResponse(
                """
                {
                  "hasAmbiguity": true,
                  "findings": [
                    {
                      "text": "quickly",
                      "reason": "Response time is not measurable.",
                      "suggestion": "Specify a maximum response time."
                    }
                  ],
                  "confidence": 1.5
                }
                """
        );

        when(client.chatCompletion(any())).thenReturn(response);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.detectAmbiguity(
                        new AmbiguityRequest(
                                UUID.randomUUID(),
                                "The system shall respond quickly."
                        )
                )
        );

        assertTrue(exception.getMessage().contains("out of range"));
    }

    @Test
    void detectAmbiguityRejectsFalseWithFindings() {
        NvidiaChatResponse response = chatResponse(
                """
                {
                  "hasAmbiguity": false,
                  "findings": [
                    {
                      "text": "quickly",
                      "reason": "Response time is not measurable.",
                      "suggestion": "Specify a maximum response time."
                    }
                  ],
                  "confidence": 0.90
                }
                """
        );

        when(client.chatCompletion(any())).thenReturn(response);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.detectAmbiguity(
                        new AmbiguityRequest(
                                UUID.randomUUID(),
                                "The system shall respond quickly."
                        )
                )
        );
       assertTrue(exception.getMessage().contains("hasAmbiguity is false"));
    }

    @Test
    void detectAmbiguityRejectsTrueWithoutFindings() {
        NvidiaChatResponse response = chatResponse(
                """
                {
                  "hasAmbiguity": true,
                  "findings": [],
                  "confidence": 0.90
                }
                """
        );

        when(client.chatCompletion(any())).thenReturn(response);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.detectAmbiguity(
                        new AmbiguityRequest(
                                UUID.randomUUID(),
                                "The system shall respond quickly."
                        )
                )
        );

        assertTrue(exception.getMessage().contains("hasAmbiguity is true"));
    }

    @Test
    void detectAmbiguityRejectsInvalidJson() {
        NvidiaChatResponse response =
                chatResponse("not valid json at all");

        when(client.chatCompletion(any())).thenReturn(response);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.detectAmbiguity(
                        new AmbiguityRequest(
                                UUID.randomUUID(),
                                "Some requirement."
                        )
                )
        );

        assertTrue(exception.getMessage().contains("not valid JSON"));
    }

    @Test
    void detectAmbiguityStripsMarkdownCodeFences() {
        NvidiaChatResponse response = chatResponse(
                """
                ```json
                {
                  "hasAmbiguity": true,
                  "findings": [
                    {
                      "text": "quickly",
                      "reason": "Response time is not measurable.",
                      "suggestion": "Specify a maximum response time."
                    }
                  ],
                  "confidence": 0.88
                }
                ```
                """
        );

        when(client.chatCompletion(any())).thenReturn(response);

        AmbiguityResponse result = provider.detectAmbiguity(
                new AmbiguityRequest(
                        UUID.randomUUID(),
                        "The system shall respond quickly."
                )
        );

        assertTrue(result.hasAmbiguity());
        assertEquals(1, result.findings().size());
        assertEquals("quickly", result.findings().get(0).text());
        assertEquals(new BigDecimal("0.88"), result.confidence());
    }
    private NvidiaChatResponse chatResponse(String content) {
        return new NvidiaChatResponse(
                List.of(new NvidiaChatResponse.Choice(
                        new NvidiaChatResponse.Message("assistant", content)
                ))
        );
    }
}
