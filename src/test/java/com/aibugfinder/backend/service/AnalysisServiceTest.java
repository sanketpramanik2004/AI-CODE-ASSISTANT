package com.aibugfinder.backend.service;

import com.aibugfinder.backend.dto.AnalysisResponse;
import com.aibugfinder.backend.dto.CodeRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalysisServiceTest {

    @Test
    void returnsAiResponseWhenClientProvidesOne() {
        OpenAiCodeAnalysisClient client = mock(OpenAiCodeAnalysisClient.class);
        UploadedCodeExtractor extractor = mock(UploadedCodeExtractor.class);
        AnalysisService service = new AnalysisService(client, extractor);
        CodeRequest request = new CodeRequest();
        request.setCode("if (a == b) { return true; }");
        request.setLanguage("java");

        AnalysisResponse aiResponse = new AnalysisResponse();
        aiResponse.setBugs(List.of("Comparison is suspicious."));
        aiResponse.setFixedCode("if (a.equals(b)) { return true; }");
        aiResponse.setExplanation("Use equals for object comparison.");
        aiResponse.setEdgeCasesToTest(List.of("a and b are null", "a and b have same value but different references"));
        aiResponse.setTimeComplexity("O(1)");
        aiResponse.setSpaceComplexity("O(1)");
        aiResponse.setOptimality("Optimal for this comparison.");
        aiResponse.setLearningResources(List.of("Java equals vs ==: https://docs.oracle.com/javase/tutorial/java/nutsandbolts/op2.html"));

        when(client.analyze(request.getCode(), request.getLanguage())).thenReturn(Optional.of(aiResponse));

        AnalysisResponse result = service.analyzeCode(request);

        assertEquals(aiResponse, result);
    }

    @Test
    void fallsBackWhenAiClientIsUnavailable() {
        OpenAiCodeAnalysisClient client = mock(OpenAiCodeAnalysisClient.class);
        UploadedCodeExtractor extractor = mock(UploadedCodeExtractor.class);
        AnalysisService service = new AnalysisService(client, extractor);
        CodeRequest request = new CodeRequest();
        request.setCode("if (a == b) { return true; }");
        request.setLanguage("java");

        when(client.analyze(request.getCode(), request.getLanguage())).thenReturn(Optional.empty());

        AnalysisResponse result = service.analyzeCode(request);

        assertTrue(result.getBugs().contains("Possible misuse of '==' instead of equals()"));
        assertEquals(request.getCode(), result.getFixedCode());
        assertTrue(result.getEdgeCasesToTest().contains("Test empty input."));
        assertEquals("Unknown in fallback mode.", result.getTimeComplexity());
        assertEquals("Unknown in fallback mode.", result.getSpaceComplexity());
        assertFalse(result.getLearningResources().isEmpty());
    }

    @Test
    void redactsApiKeyWhenAiClientThrows() {
        OpenAiCodeAnalysisClient client = mock(OpenAiCodeAnalysisClient.class);
        UploadedCodeExtractor extractor = mock(UploadedCodeExtractor.class);
        AnalysisService service = new AnalysisService(client, extractor);
        CodeRequest request = new CodeRequest();
        request.setCode("public class Demo {} ");
        request.setLanguage("java");

        when(client.analyze(request.getCode(), request.getLanguage()))
                .thenThrow(new IllegalStateException(
                        "Failed to analyze code with OpenAI: 401: Incorrect API key provided: sk-proj-secretValue123"));

        AnalysisResponse result = service.analyzeCode(request);

        assertTrue(result.getExplanation().contains("[REDACTED_API_KEY]"));
        assertFalse(result.getExplanation().contains("sk-proj-secretValue123"));
    }

    @Test
    void analyzesUploadedFileUsingExtractedCode() throws IOException {
        OpenAiCodeAnalysisClient client = mock(OpenAiCodeAnalysisClient.class);
        UploadedCodeExtractor extractor = mock(UploadedCodeExtractor.class);
        AnalysisService service = new AnalysisService(client, extractor);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "BinarySearch.java",
                "text/plain",
                "class BinarySearch {}".getBytes(StandardCharsets.UTF_8));

        when(extractor.extract(file, null)).thenReturn(new UploadedCodeExtractor.ExtractedCode(
                "class BinarySearch {}",
                "java",
                "uploaded file 'BinarySearch.java'"));

        AnalysisResponse aiResponse = new AnalysisResponse();
        aiResponse.setBugs(List.of("Binary search bounds are wrong."));
        aiResponse.setFixedCode("class BinarySearch { }");
        aiResponse.setExplanation("Bounds fixed.");
        aiResponse.setEdgeCasesToTest(List.of("empty array"));
        aiResponse.setTimeComplexity("O(log n)");
        aiResponse.setSpaceComplexity("O(1)");
        aiResponse.setOptimality("Optimal.");
        aiResponse.setLearningResources(List.of("Binary search tutorial: https://leetcode.com/explore/learn/card/binary-search/"));

        when(client.analyze("class BinarySearch {}", "java")).thenReturn(Optional.of(aiResponse));

        AnalysisResponse result = service.analyzeUploadedFile(file, null);

        assertTrue(result.getExplanation().startsWith("Source: uploaded file 'BinarySearch.java'."));
        assertEquals("O(log n)", result.getTimeComplexity());
        assertEquals(1, result.getLearningResources().size());
    }
}
