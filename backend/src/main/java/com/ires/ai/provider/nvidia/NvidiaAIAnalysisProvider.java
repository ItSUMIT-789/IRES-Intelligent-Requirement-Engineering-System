package com.ires.ai.provider.nvidia;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ires.ai.dto.analysis.AmbiguityFinding;
import com.ires.ai.dto.analysis.AmbiguityRequest;
import com.ires.ai.dto.analysis.AmbiguityResponse;
import com.ires.ai.dto.analysis.ClassificationRequest;
import com.ires.ai.dto.analysis.ClassificationResponse;
import com.ires.ai.dto.analysis.CompletenessRequest;
import com.ires.ai.dto.analysis.CompletenessResponse;
import com.ires.ai.dto.analysis.MissingInformation;
import com.ires.ai.dto.analysis.ConflictDetectionRequest;
import com.ires.ai.dto.analysis.ConflictDetectionResponse;
import com.ires.ai.dto.analysis.DuplicateDetectionRequest;
import com.ires.ai.dto.analysis.DuplicateDetectionResponse;
import com.ires.ai.dto.analysis.QualityAnalysisRequest;
import com.ires.ai.dto.analysis.QualityAnalysisResponse;
import com.ires.ai.dto.analysis.QualityDimension;
import com.ires.ai.provider.nvidia.dto.NvidiaChatMessage;
import com.ires.ai.provider.nvidia.dto.NvidiaChatRequest;
import com.ires.ai.provider.nvidia.dto.NvidiaChatResponse;
import com.ires.ai.service.AIAnalysisProvider;
import com.ires.requirement.entity.Requirement;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;

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
    * NVIDIA analysis capabilities implemented as separate vertical slices.
    */
    @Override
    public AmbiguityResponse detectAmbiguity(AmbiguityRequest request) {
        String systemPrompt = """
                You are a software requirements ambiguity detection engine.

                Analyze the supplied software requirement and determine whether
                it contains ambiguous, vague, subjective, imprecise, or
                non-measurable language.

                A requirement is ambiguous when its meaning or expected behavior
                cannot be determined precisely enough for implementation or
                objective testing.

                Examples of potentially ambiguous terms include:
                - fast
                - quickly
                - user-friendly
                - easy to use
                - sufficient
                - appropriate
                - normally
                - soon
                - efficient
                - reliable

                If the requirement is clear and measurable, hasAmbiguity must be false
                and findings must be an empty array.

                If ambiguity exists, identify each important ambiguous phrase.

                Respond ONLY with a JSON object in exactly this structure:
                {
                "hasAmbiguity": true,
                "findings": [
                    {
                    "text": "ambiguous phrase",
                    "reason": "why the phrase is ambiguous",
                    "suggestion": "how to make the requirement precise"
                    }
                ],
                "confidence": 0.0
                }

                confidence must be a number between 0.0 and 1.0.
                Do not include markdown.
                Do not include any text outside the JSON object.
                """;

        String userPrompt =
                "Analyze this software requirement for ambiguity:\n\n"
                        + request.text();

        NvidiaChatRequest chatRequest = new NvidiaChatRequest(
                resolveModel(),
                List.of(
                        new NvidiaChatMessage("system", systemPrompt),
                        new NvidiaChatMessage("user", userPrompt)
                )
        );

        NvidiaChatResponse chatResponse =
                client.chatCompletion(chatRequest);

        String content = extractContent(chatResponse);
        JsonNode json = parseJson(content);

        if (!json.has("hasAmbiguity")
                || json.get("hasAmbiguity").isNull()
                || !json.get("hasAmbiguity").isBoolean()) {
            throw new IllegalStateException(
                    "NVIDIA response did not contain a valid hasAmbiguity value."
            );
        }

        boolean hasAmbiguity = json.get("hasAmbiguity").booleanValue();

        if (!json.has("findings")
                || json.get("findings").isNull()
                || !json.get("findings").isArray()) {
            throw new IllegalStateException(
                    "NVIDIA response did not contain a valid findings array."
            );
        }

        List<AmbiguityFinding> findings = new ArrayList<>();

        for (JsonNode findingNode : json.get("findings")) {
            String text = extractFindingField(findingNode, "text");
            String reason = extractFindingField(findingNode, "reason");
            String suggestion = extractFindingField(findingNode, "suggestion");

            findings.add(
                    new AmbiguityFinding(
                            text,
                            reason,
                            suggestion
                    )
            );
        }

        if (!hasAmbiguity && !findings.isEmpty()) {
            throw new IllegalStateException(
                    "NVIDIA response is inconsistent: hasAmbiguity is false "
                            + "but findings were returned."
            );
        }

        if (hasAmbiguity && findings.isEmpty()) {
            throw new IllegalStateException(
                    "NVIDIA response is inconsistent: hasAmbiguity is true "
                            + "but no findings were returned."
            );
        }

        BigDecimal confidence = extractConfidence(json);

        return new AmbiguityResponse(
                hasAmbiguity,
                findings,
                confidence
        );
    }

    @Override
    public CompletenessResponse analyzeCompleteness(
            CompletenessRequest request
    ) {
        String systemPrompt = """
                You are a software requirements completeness analysis engine.

                Analyze the supplied software requirement and determine whether
                it contains enough information for implementation and objective
                verification.

                A requirement may be incomplete when important information is
                missing, such as:
                - required inputs
                - expected outputs
                - business rules
                - processing behavior
                - validation rules
                - error handling
                - boundary conditions
                - security requirements
                - performance requirements
                - constraints
                - dependencies
                - acceptance conditions

                Do not mark a requirement incomplete merely because it does not
                specify information that is irrelevant to its stated behavior.

                If the requirement contains enough information for implementation
                and objective verification:
                - isComplete must be true
                - missingInformation must be an empty array
                - clarificationQuestions must be an empty array

                If important information is missing:
                - isComplete must be false
                - identify each important missing aspect
                - provide a useful clarification question for each important gap

                Respond ONLY with a JSON object in exactly this structure:
                {
                "isComplete": false,
                "missingInformation": [
                    {
                    "aspect": "Error Handling",
                    "description": "The requirement does not specify what should happen when processing fails."
                    }
                ],
                "clarificationQuestions": [
                    "What should the system do when processing fails?"
                ],
                "confidence": 0.0
                }

                confidence must be a number between 0.0 and 1.0.
                Do not include markdown.
                Do not include any text outside the JSON object.
                """;

        String userPrompt =
                "Analyze this software requirement for completeness:\n\n"
                        + request.text();

        NvidiaChatRequest chatRequest = new NvidiaChatRequest(
                resolveModel(),
                List.of(
                        new NvidiaChatMessage("system", systemPrompt),
                        new NvidiaChatMessage("user", userPrompt)
                )
        );

        NvidiaChatResponse chatResponse =
                client.chatCompletion(chatRequest);

        String content = extractContent(chatResponse);
        JsonNode json = parseJson(content);

        if (!json.has("isComplete")
                || json.get("isComplete").isNull()
                || !json.get("isComplete").isBoolean()) {
            throw new IllegalStateException(
                    "NVIDIA response did not contain a valid isComplete value."
            );
        }

        boolean isComplete = json.get("isComplete").booleanValue();

        if (!json.has("missingInformation")
                || json.get("missingInformation").isNull()
                || !json.get("missingInformation").isArray()) {
            throw new IllegalStateException(
                    "NVIDIA response did not contain a valid missingInformation array."
            );
        }

        List<MissingInformation> missingInformation = new ArrayList<>();

        for (JsonNode missingNode : json.get("missingInformation")) {
            String aspect = extractCompletenessField(
                    missingNode,
                    "aspect"
            );

            String description = extractCompletenessField(
                    missingNode,
                    "description"
            );

            missingInformation.add(
                    new MissingInformation(
                            aspect,
                            description
                    )
            );
        }

        if (!json.has("clarificationQuestions")
                || json.get("clarificationQuestions").isNull()
                || !json.get("clarificationQuestions").isArray()) {
            throw new IllegalStateException(
                    "NVIDIA response did not contain a valid clarificationQuestions array."
            );
        }

        List<String> clarificationQuestions = new ArrayList<>();

        for (JsonNode questionNode : json.get("clarificationQuestions")) {
            if (questionNode == null
                    || questionNode.isNull()
                    || !questionNode.isTextual()
                    || questionNode.asText("").isBlank()) {
                throw new IllegalStateException(
                        "NVIDIA completeness response contained an invalid clarification question."
                );
            }

            clarificationQuestions.add(
                    questionNode.asText().trim()
            );
        }

        if (isComplete && !missingInformation.isEmpty()) {
            throw new IllegalStateException(
                    "NVIDIA response is inconsistent: isComplete is true "
                            + "but missing information was returned."
            );
        }

        if (isComplete && !clarificationQuestions.isEmpty()) {
            throw new IllegalStateException(
                    "NVIDIA response is inconsistent: isComplete is true "
                            + "but clarification questions were returned."
            );
        }

        if (!isComplete && missingInformation.isEmpty()) {
            throw new IllegalStateException(
                    "NVIDIA response is inconsistent: isComplete is false "
                            + "but no missing information was returned."
            );
        }

        if (!isComplete && clarificationQuestions.isEmpty()) {
            throw new IllegalStateException(
                    "NVIDIA response is inconsistent: isComplete is false "
                            + "but no clarification questions were returned."
            );
        }

        BigDecimal confidence = extractConfidence(json);

        return new CompletenessResponse(
                isComplete,
                missingInformation,
                clarificationQuestions,
                confidence
        );
    }

    @Override
    public QualityAnalysisResponse analyzeQuality(
            QualityAnalysisRequest request
    ) {
        String systemPrompt = """
                You are a software requirements quality analysis engine.

                Analyze the supplied software requirement against five quality
                dimensions:

                1. Clarity
                The requirement should be understandable and free from vague
                or subjective wording.

                2. Specificity
                The requirement should define precise behavior, constraints,
                inputs, outputs, and measurable conditions where relevant.

                3. Testability
                The requirement should be objectively verifiable through tests,
                inspection, or measurable acceptance conditions.

                4. Consistency
                The requirement should not contain contradictory statements,
                incompatible constraints, or internally inconsistent behavior.

                5. Atomicity
                The requirement should express one coherent requirement rather
                than combining multiple independent requirements unnecessarily.

                Score each dimension from 0 to 100.

                The overallScore must be a 0 to 100 assessment of the requirement
                across all five dimensions.

                For every dimension provide:
                - name
                - score
                - finding explaining the observed quality
                - recommendation for improvement

                Use the exact dimension names:
                Clarity
                Specificity
                Testability
                Consistency
                Atomicity

                Respond ONLY with a JSON object in exactly this structure:
                {
                "overallScore": 82,
                "dimensions": [
                    {
                    "name": "Clarity",
                    "score": 85,
                    "finding": "The requirement is mostly clear.",
                    "recommendation": "Replace vague terms with measurable criteria."
                    },
                    {
                    "name": "Specificity",
                    "score": 80,
                    "finding": "Some behavior is underspecified.",
                    "recommendation": "Define the expected behavior explicitly."
                    },
                    {
                    "name": "Testability",
                    "score": 78,
                    "finding": "The requirement lacks measurable acceptance conditions.",
                    "recommendation": "Add objective conditions that can be verified through testing."
                    },
                    {
                    "name": "Consistency",
                    "score": 90,
                    "finding": "The requirement does not contain an internal contradiction.",
                    "recommendation": "Maintain the current consistent structure."
                    },
                    {
                    "name": "Atomicity",
                    "score": 80,
                    "finding": "The requirement contains one main behavior.",
                    "recommendation": "Separate unrelated behaviors if additional behavior is introduced."
                    }
                ],
                "confidence": 0.91
                }

                overallScore and every dimension score must be integers between
                0 and 100.

                confidence must be a number between 0.0 and 1.0.

                Do not include markdown.
                Do not include any text outside the JSON object.
                """;

        String userPrompt =
                "Analyze this software requirement for quality:\n\n"
                        + request.text();

        NvidiaChatRequest chatRequest = new NvidiaChatRequest(
                resolveModel(),
                List.of(
                        new NvidiaChatMessage("system", systemPrompt),
                        new NvidiaChatMessage("user", userPrompt)
                )
        );

        NvidiaChatResponse chatResponse =
                client.chatCompletion(chatRequest);

        String content = extractContent(chatResponse);
        JsonNode json = parseJson(content);

        if (!json.has("overallScore")
                || json.get("overallScore").isNull()
                || !json.get("overallScore").canConvertToInt()) {
            throw new IllegalStateException(
                    "NVIDIA response did not contain a valid overallScore."
            );
        }

        int overallScore = json.get("overallScore").intValue();

        if (overallScore < 0 || overallScore > 100) {
            throw new IllegalStateException(
                    "NVIDIA overallScore is out of range [0, 100]: "
                            + overallScore
            );
        }

        if (!json.has("dimensions")
                || json.get("dimensions").isNull()
                || !json.get("dimensions").isArray()) {
            throw new IllegalStateException(
                    "NVIDIA response did not contain a valid dimensions array."
            );
        }

        List<QualityDimension> dimensions = new ArrayList<>();

        Set<String> requiredDimensions = Set.of(
                "Clarity",
                "Specificity",
                "Testability",
                "Consistency",
                "Atomicity"
        );

        Set<String> returnedDimensions = new HashSet<>();

        for (JsonNode dimensionNode : json.get("dimensions")) {
            if (dimensionNode == null || !dimensionNode.isObject()) {
                throw new IllegalStateException(
                        "NVIDIA quality response contained an invalid dimension."
                );
            }

            String name = extractQualityField(
                    dimensionNode,
                    "name"
            );

            String scoreField = "score";

            if (!dimensionNode.has(scoreField)
                    || dimensionNode.get(scoreField).isNull()
                    || !dimensionNode.get(scoreField).canConvertToInt()) {
                throw new IllegalStateException(
                        "NVIDIA quality dimension did not contain a valid score."
                );
            }

            int score = dimensionNode.get(scoreField).intValue();

            if (score < 0 || score > 100) {
                throw new IllegalStateException(
                        "NVIDIA quality dimension score is out of range [0, 100]: "
                                + score
                );
            }

            String finding = extractQualityField(
                    dimensionNode,
                    "finding"
            );

            String recommendation = extractQualityField(
                    dimensionNode,
                    "recommendation"
            );

            if (!requiredDimensions.contains(name)) {
                throw new IllegalStateException(
                        "NVIDIA returned an unsupported quality dimension: "
                                + name
                );
            }

            if (!returnedDimensions.add(name)) {
                throw new IllegalStateException(
                        "NVIDIA returned duplicate quality dimension: "
                                + name
                );
            }

            dimensions.add(
                    new QualityDimension(
                            name,
                            score,
                            finding,
                            recommendation
                    )
            );
        }

        if (!returnedDimensions.equals(requiredDimensions)) {
            throw new IllegalStateException(
                    "NVIDIA quality response did not contain exactly the required "
                            + "quality dimensions."
            );
        }

        BigDecimal confidence = extractConfidence(json);

        return new QualityAnalysisResponse(
                overallScore,
                dimensions,
                confidence
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
            throw new IllegalStateException(
                "NVIDIA analysis model is not configured."
            );
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

    private String extractFindingField(
            JsonNode findingNode,
            String field
    ) {
        if (findingNode == null
                || !findingNode.has(field)
                || findingNode.get(field).isNull()
                || findingNode.get(field).asText("").isBlank()) {
            throw new IllegalStateException(
                    "NVIDIA ambiguity finding did not contain a "
                            + field + "."
            );
        }
        return findingNode.get(field).asText().trim();
    }

    private String extractCompletenessField(
            JsonNode node,
            String field
    ) {
        if (node == null
                || !node.has(field)
                || node.get(field).isNull()
                || node.get(field).asText("").isBlank()) {
            throw new IllegalStateException(
                    "NVIDIA completeness response did not contain a "
                            + field + "."
            );
        }

        return node.get(field).asText().trim();
    }

    private String extractQualityField(
            JsonNode node,
            String field
    ) {
        if (node == null
                || !node.has(field)
                || node.get(field).isNull()
                || node.get(field).asText("").isBlank()) {
            throw new IllegalStateException(
                    "NVIDIA quality dimension did not contain a "
                            + field + "."
            );
        }

        return node.get(field).asText().trim();
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
