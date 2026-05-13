package com.aibugfinder.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Component
public class Judge0Client {
    private final RestClient restClient;
    private final String baseUrl;
    private final String apiKey;
    private final String host;

    public Judge0Client(@Value("${judge0.base-url}") String baseUrl,
            @Value("${judge0.api-key}") String apiKey,
            @Value("${judge0.host}") String host) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.host = host;
        this.restClient = RestClient.builder().build();
    }

    public boolean isConfigured() {
        return baseUrl != null && !baseUrl.isBlank() && apiKey != null && !apiKey.isBlank();
    }

    public Optional<String> run(String code, String language, String stdin) {
        if (!isConfigured()) {
            return Optional.empty();
        }

        Integer languageId = languageId(language);
        if (languageId == null) {
            return Optional.empty();
        }

        Judge0SubmitResponse submitted = restClient.post()
                .uri(baseUrl + "/submissions?base64_encoded=true&wait=true")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-RapidAPI-Key", apiKey)
                .header("X-RapidAPI-Host", host)
                .body(Map.of(
                        "language_id", languageId,
                        "source_code", encode(code),
                        "stdin", encode(stdin == null ? "" : stdin)))
                .retrieve()
                .body(Judge0SubmitResponse.class);

        if (submitted == null) {
            return Optional.empty();
        }

        if (submitted.stderr() != null && !submitted.stderr().isBlank()) {
            return Optional.of(decode(submitted.stderr()));
        }

        if (submitted.compile_output() != null && !submitted.compile_output().isBlank()) {
            return Optional.of(decode(submitted.compile_output()));
        }

        return Optional.of(decode(submitted.stdout()));
    }

    private Integer languageId(String language) {
        if (language == null) {
            return null;
        }

        return switch (language.trim().toLowerCase()) {
            case "java" -> 62;
            case "python", "python3" -> 71;
            case "javascript", "js" -> 63;
            case "typescript", "ts" -> 74;
            case "cpp", "c++" -> 54;
            case "c" -> 50;
            case "go" -> 60;
            case "rust" -> 73;
            default -> null;
        };
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8).trim();
    }

    public record Judge0SubmitResponse(String stdout, String stderr, String compile_output) {
    }
}
