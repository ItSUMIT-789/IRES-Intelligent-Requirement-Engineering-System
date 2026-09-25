package com.ires.ai.provider.nvidia;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ires.ai.dto.analysis.AmbiguityRequest;
import com.ires.ai.dto.analysis.AmbiguityResponse;
import com.ires.ai.dto.analysis.ClassificationRequest;
import com.ires.ai.dto.analysis.ClassificationResponse;
import com.ires.ai.dto.analysis.CompletenessRequest;
import com.ires.ai.dto.analysis.CompletenessResponse;
import com.ires.ai.dto.analysis.ConflictDetectionRequest;
import com.ires.ai.dto.analysis.ConflictDetectionResponse;
import com.ires.ai.dto.analysis.DuplicateDetectionRequest;
import com.ires.ai.dto.analysis.DuplicateDetectionResponse;
import com.ires.ai.dto.analysis.QualityAnalysisRequest;
import com.ires.ai.dto.analysis.QualityAnalysisResponse;
import com.ires.ai.provider.nvidia.dto.NvidiaChatMessage;
import com.ires.ai.provider.nvidia.dto.NvidiaChatRequest;
import com.ires.ai.provider.nvidia.dto.NvidiaChatResponse;
import com.ires.ai.service.AIAnalysisProvider;
import com.ires.requirement.entity.Requirement;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public class NvidiaAIAnalysisProvider implements AIAnalysisProvider {

    private static final Set<String> VALID_CLASSIFICATIONS = Set.of(
            "FUNCTIONAL", "NON_FUNCTIONAL", "BUSINESS", "TECHNICAL", "UNCLEAR"
    );

    private final NvidiaApiClient client;
    private final String model;
    private final ObjectMapper objectMapper;

    public NvidiaAIAnalysisProvider(
            NvidiaApiClient client,
            String model
    ) {
        this.client = client;
        this.model = model;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public ClassificationResponse classify(ClassificationRequest request) {
        String systemPrompt = """
                You are a software requirements classification engine.
                Classify the given requirement into exactly one category.
                Valid categories: FUNCTIONAL, NON_FUNCTIONAL, BUSINESS, TECHNICAL, UNCLEAR.
                Respond ONLY with a JSON object in this exact format:
                {"classification": "<CATEGORY>", "confidence": <0.0-1.0>, "reason": "<explanation>"}
                Do not include any other text outside the JSON object.""";

        String userPrompt = "Classify this software requirement:\n\n" + request.text();

        NvidiaChatRequest chatRequest = new NvidiaChatRequest(
                resolveModel(),
                List.of(
                        new NvidiaChatMessage("system", systemPrompt),
                        new NvidiaChatMessage("user", userPrompt)
                )
        );

        NvidiaChatResponse chatResponse = client.chatCompletion(chatRequest);

        String content = extractContent(chatResponse);
        JsonNode json = parseJson(content);

        String classification = extractRequiredString(json, "classification");

        if (!VALID_CLASSIFICATIONS.contains(classification)) {
            throw new IllegalStateException(
                    "NVIDIA returned an invalid classification: " + classification
            );
        }

        BigDecimal confidence = extractConfidence(json);

        String reason = json.has("reason")
                ? json.get("reason").asText("")
                : "";

        if (reason.isBlank()) {
            throw new IllegalStateException(
                    "NVIDIA response did not contain a reason."
            );
        }

        return new ClassificationResponse(
                classification,
                confidence,
                reason
        );
    }

    /*
     * These capabilities will be implemented in subsequent vertical slices.
     */
    @Override
    public AmbiguityResponse detectAmbiguity(AmbiguityRequest request) {
        throw new UnsupportedOperationException(
                "NVIDIA ambiguity detection is not implemented yet."
        );
    }

    @Override
    public CompletenessResponse analyzeCompleteness(
            CompletenessRequest request
    ) {
        throw new UnsupportedOperationException(
                "NVIDIA completeness analysis is not implemented yet."
        );
    }

    @Override
    public QualityAnalysisResponse analyzeQuality(
            QualityAnalysisRequest request
    ) {
        throw new UnsupportedOperationException(
                "NVIDIA quality analysis is not implemented yet."
        );
    }

    @Override
    public DuplicateDetectionResponse detectDuplicates(
            DuplicateDetectionRequest request
    ) {
        throw new UnsupportedOperationException(
                "NVIDIA duplicate detection is not implemented yet."
        );
    }

    @Override
    public ConflictDetectionResponse detectConflicts(
            ConflictDetectionRequest request
    ) {
        throw new UnsupportedOperationException(
                "NVIDIA conflict detection is not implemented yet."
        );
    }

    @Override
    public AIAnalysisResult analyze(Requirement requirement) {
        throw new UnsupportedOperationException(
                "Legacy AI analysis is not implemented by the NVIDIA provider."
        );
    }

    private String resolveModel() {
        if (model == null || model.isBlank()) {
            return "meta/llama-3.1-70b-instruct";
        }
        return model;
    }

    private String extractContent(NvidiaChatResponse response) {
        if (response == null) {
            throw new IllegalStateException(
                    "NVIDIA returned an empty response."
            );
        }

        if (response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException(
                    "NVIDIA response did not contain any choices."
            );
        }

        NvidiaChatResponse.Choice firstChoice = response.choices().get(0);

        if (firstChoice.message() == null
                || firstChoice.message().content() == null
                || firstChoice.message().content().isBlank()) {
            throw new IllegalStateException(
                    "NVIDIA response did not contain message content."
            );
        }

        return firstChoice.message().content().trim();
    }

    private JsonNode parseJson(String content) {
        // Strip markdown code fences if present
        String cleaned = content;
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        cleaned = cleaned.trim();

        try {
            return objectMapper.readTree(cleaned);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "NVIDIA response is not valid JSON: " + content, e
            );
        }
    }

    private String extractRequiredString(JsonNode json, String field) {
        if (!json.has(field)
                || json.get(field).isNull()
                || json.get(field).asText("").isBlank()) {
            throw new IllegalStateException(
                    "NVIDIA response did not contain a " + field + "."
            );
        }
        return json.get(field).asText().trim().toUpperCase();
    }

    private BigDecimal extractConfidence(JsonNode json) {
        if (!json.has("confidence") || json.get("confidence").isNull()) {
            throw new IllegalStateException(
                    "NVIDIA response did not contain a confidence value."
            );
        }

        BigDecimal confidence;
        try {
            confidence = json.get("confidence").decimalValue();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "NVIDIA response contains an invalid confidence value.", e
            );
        }

        if (confidence.compareTo(BigDecimal.ZERO) < 0
                || confidence.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalStateException(
                    "NVIDIA confidence value is out of range [0, 1]: " + confidence
            );
        }

        return confidence;
    }
}
