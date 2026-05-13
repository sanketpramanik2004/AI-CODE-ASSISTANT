package com.aibugfinder.backend.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.StructuredResponse;
import com.openai.models.responses.StructuredResponseOutputItem;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AiCodeEvaluationClient {
    private final String model;
    private final String apiKey;

    public AiCodeEvaluationClient(Environment environment) {
        this.model = firstNonBlank(environment.getProperty("openai.model"), "gpt-5-mini");
        this.apiKey = firstNonBlank(
                environment.getProperty("openai.api-key"),
                environment.getProperty("OPENAI_API_KEY"),
                System.getenv("OPENAI_API_KEY"));
    }

    public Optional<Boolean> isCorrect(String title, String description, String testCases, String code, String language) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }

        try {
            OpenAIClient client = OpenAIOkHttpClient.builder()
                    .apiKey(apiKey)
                    .build();

            StructuredResponse<AiEvaluationPayload> response = client.responses().create(
                    ResponseCreateParams.builder()
                            .model(model)
                            .instructions("""
                                    You judge whether a submitted solution is correct for a coding problem.
                                    Return true only if the code appears to satisfy the problem statement and the listed test cases.
                                    Ignore minor style issues and missing optional null checks unless the problem statement requires them.
                                    """)
                            .input("""
                                    Problem title: %s
                                    Problem description:
                                    %s

                                    Test cases:
                                    %s

                                    Language: %s
                                    Submitted code:
                                    %s
                                    """.formatted(title, description, testCases, language, code))
                            .text(AiEvaluationPayload.class)
                            .build());

            return extractPayload(response).map(payload -> payload.correct);
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private Optional<AiEvaluationPayload> extractPayload(StructuredResponse<AiEvaluationPayload> response) {
        for (StructuredResponseOutputItem<AiEvaluationPayload> outputItem : response.output()) {
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

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }

    static final class AiEvaluationPayload {
        public boolean correct;
        public List<String> reasons;
    }
}
