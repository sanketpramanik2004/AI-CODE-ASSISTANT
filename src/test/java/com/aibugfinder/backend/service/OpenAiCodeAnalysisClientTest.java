package com.aibugfinder.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenAiCodeAnalysisClientTest {

    @Test
    void returnsEmptyWhenNoApiKeyIsConfigured() {
        OpenAiCodeAnalysisClient client = new OpenAiCodeAnalysisClient("gpt-5.4", "   ");

        Optional<?> result = client.analyze("public class Demo {}", "java");

        assertTrue(result.isEmpty());
    }

    @Test
    void readsApiKeyFromSpringProperty() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("openai.model", "gpt-5.4")
                .withProperty("openai.api-key", "test-key");

        OpenAiCodeAnalysisClient client = new OpenAiCodeAnalysisClient(environment);

        assertEquals("test-key", readField(client, "apiKey"));
        assertEquals("gpt-5.4", readField(client, "model"));
    }

    private Object readField(OpenAiCodeAnalysisClient client, String name) {
        try {
            var field = OpenAiCodeAnalysisClient.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(client);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(exception);
        }
    }
}
