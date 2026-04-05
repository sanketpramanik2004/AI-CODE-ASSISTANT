package com.aibugfinder.backend.service;

import com.aibugfinder.backend.dto.AnalysisResponse;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.StructuredResponse;
import com.openai.models.responses.StructuredResponseOutputItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OpenAiCodeAnalysisClient {

    private final String model;
    private final String apiKey;

    @Autowired
    public OpenAiCodeAnalysisClient(Environment environment) {
        this.model = firstNonBlank(
                environment.getProperty("openai.model"),
                "gpt-5.4");
        this.apiKey = firstNonBlank(
                environment.getProperty("openai.api-key"),
                environment.getProperty("openai.apiKey"),
                environment.getProperty("spring.ai.openai.api-key"),
                environment.getProperty("OPENAI_API_KEY"),
                System.getenv("OPENAI_API_KEY"));
    }

    OpenAiCodeAnalysisClient(String model, String apiKey) {
        this.model = model;
        this.apiKey = apiKey;
    }

    public Optional<AnalysisResponse> analyze(String code, String language) {
        if (!isApiKeyConfigured()) {
            return Optional.empty();
        }

        try {
            OpenAIClient client = OpenAIOkHttpClient.builder()
                    .apiKey(apiKey)
                    .build();
            StructuredResponse<AiAnalysisPayload> response = client.responses().create(
                    ResponseCreateParams.builder()
                            .model(model)
                            .instructions(
                                    "You are a careful code-debugging assistant for interview and production code. "
                                            + "Identify real bugs, wrong logic, broken edge-case handling, and risky assumptions. "
                                            + "For DSA or LeetCode-style code, check algorithm correctness, off-by-one mistakes, base cases, "
                                            + "null or empty-input handling, pointer/index updates, and time/space complexity tradeoffs. "
                                            + "Return practical fixes that preserve the intended approach whenever possible.")
                            .input(buildPrompt(code, language))
                            .text(AiAnalysisPayload.class)
                            .build());

            AiAnalysisPayload payload = extractPayload(response)
                    .orElseThrow(() -> new IllegalStateException("OpenAI did not return structured analysis content."));

            AnalysisResponse analysisResponse = new AnalysisResponse();
            analysisResponse.setBugs(payload.bugs == null || payload.bugs.isEmpty()
                    ? List.of("No clear bugs were identified by the model.")
                    : payload.bugs);
            analysisResponse
                    .setFixedCode(payload.fixedCode == null || payload.fixedCode.isBlank() ? code : payload.fixedCode);
            analysisResponse.setExplanation(payload.explanation == null || payload.explanation.isBlank()
                    ? "OpenAI analysis completed."
                    : payload.explanation);
            analysisResponse.setEdgeCasesToTest(payload.edgeCasesToTest == null || payload.edgeCasesToTest.isEmpty()
                    ? List.of("Test null, empty, minimal, and boundary-value inputs.")
                    : payload.edgeCasesToTest);
            analysisResponse.setTimeComplexity(payload.timeComplexity == null || payload.timeComplexity.isBlank()
                    ? "Not provided."
                    : payload.timeComplexity);
            analysisResponse.setSpaceComplexity(payload.spaceComplexity == null || payload.spaceComplexity.isBlank()
                    ? "Not provided."
                    : payload.spaceComplexity);
            analysisResponse.setOptimality(payload.optimality == null || payload.optimality.isBlank()
                    ? "Not provided."
                    : payload.optimality);
            analysisResponse.setLearningResources(payload.learningResources == null || payload.learningResources.isEmpty()
                    ? defaultLearningResources()
                    : payload.learningResources);

            return Optional.of(analysisResponse);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to analyze code with OpenAI: " + rootCauseMessage(exception),
                    exception);
        }
    }

    private Optional<AiAnalysisPayload> extractPayload(StructuredResponse<AiAnalysisPayload> response) {
        for (StructuredResponseOutputItem<AiAnalysisPayload> outputItem : response.output()) {
            if (!outputItem.isMessage()) {
                continue;
            }

            for (var content : outputItem.asMessage().content()) {
                if (content.isOutputText()) {
                    return Optional.of(content.asOutputText());
                }
            }
        }

        return Optional.empty();
    }

    private boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    private String rootCauseMessage(Exception exception) {
        Throwable current = exception;

        while (current.getCause() != null) {
            current = current.getCause();
        }

        String message = current.getMessage();
        return (message == null || message.isBlank()) ? "Unknown failure." : message.trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }

    private List<String> defaultLearningResources() {
        return List.of(
                "Binary search patterns and pitfalls: https://leetcode.com/explore/learn/card/binary-search/",
                "Big-O refresher: https://www.geeksforgeeks.org/analysis-algorithms-big-o-analysis/",
                "Common coding interview mistakes: https://neetcode.io/roadmap");
    }

    private String buildPrompt(String code, String language) {
        String detectedLanguage = language == null || language.isBlank() ? "unknown" : language;

        return """
                Analyze the submitted source code.
                Focus on correctness first, not style.
                Assume this may be a DSA or LeetCode-style solution.
                Check for:
                - wrong answers on edge cases
                - off-by-one errors
                - invalid loop or recursion termination
                - incorrect pointer/index updates
                - null, empty, or singleton input bugs
                - mutation of input when it should be avoided
                - time complexity or space complexity problems when the approach is too slow
                If you suggest fixes, keep the original intent intact and prefer the smallest correct repair.
                In the explanation, mention why the bug happens and why the fix works.
                Also include:
                - a short list of edge cases to test
                - the estimated time complexity
                - the estimated space complexity
                - whether the approach is optimal and, if not, what would improve it
                - 2 to 4 learning resources with direct links relevant to the detected problems

                Language: %s
                Code:
                %s
                """.formatted(detectedLanguage, code);
    }

    static final class AiAnalysisPayload {
        public List<String> bugs;
        public String fixedCode;
        public String explanation;
        public List<String> edgeCasesToTest;
        public String timeComplexity;
        public String spaceComplexity;
        public String optimality;
        public List<String> learningResources;
    }
}
