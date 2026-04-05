package com.aibugfinder.backend.service;

import com.aibugfinder.backend.dto.AnalysisResponse;
import com.aibugfinder.backend.dto.CodeRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class AnalysisService {
    private static final Pattern OPENAI_KEY_PATTERN = Pattern.compile("sk-[A-Za-z0-9_-]+");

    private final OpenAiCodeAnalysisClient openAiCodeAnalysisClient;
    private final UploadedCodeExtractor uploadedCodeExtractor;

    public AnalysisService(OpenAiCodeAnalysisClient openAiCodeAnalysisClient,
            UploadedCodeExtractor uploadedCodeExtractor) {
        this.openAiCodeAnalysisClient = openAiCodeAnalysisClient;
        this.uploadedCodeExtractor = uploadedCodeExtractor;
    }

    public AnalysisResponse analyzeCode(CodeRequest request) {
        String code = request != null && request.getCode() != null ? request.getCode() : "";
        String language = request != null ? request.getLanguage() : null;

        if (code.isBlank()) {
            AnalysisResponse response = new AnalysisResponse();
            response.setBugs(List.of("No code was provided for analysis."));
            response.setFixedCode("");
            response.setExplanation("Send source code in the request body to analyze it.");
            response.setEdgeCasesToTest(List.of("Add a code snippet first, then test empty, null, and boundary inputs."));
            response.setTimeComplexity("Not available.");
            response.setSpaceComplexity("Not available.");
            response.setOptimality("Not available.");
            response.setLearningResources(defaultLearningResources());
            return response;
        }

        try {
            return openAiCodeAnalysisClient.analyze(code, language)
                    .orElseGet(() -> buildFallbackResponse(code));
        } catch (RuntimeException exception) {
            AnalysisResponse fallbackResponse = buildFallbackResponse(code);
            fallbackResponse.setExplanation(
                    fallbackResponse.getExplanation() + " OpenAI integration error: " + sanitize(exception.getMessage()));
            return fallbackResponse;
        }
    }

    public AnalysisResponse analyzeUploadedFile(MultipartFile file, String language) {
        if (file == null || file.isEmpty()) {
            AnalysisResponse response = new AnalysisResponse();
            response.setBugs(List.of("No file was uploaded for analysis."));
            response.setFixedCode("");
            response.setExplanation("Upload a source file or a .zip repository archive in the multipart 'file' field.");
            response.setEdgeCasesToTest(List.of("Try uploading a non-empty source file or zip archive."));
            response.setTimeComplexity("Not available.");
            response.setSpaceComplexity("Not available.");
            response.setOptimality("Not available.");
            response.setLearningResources(defaultLearningResources());
            return response;
        }

        try {
            UploadedCodeExtractor.ExtractedCode extractedCode = uploadedCodeExtractor.extract(file, language);
            CodeRequest request = new CodeRequest();
            request.setCode(extractedCode.code());
            request.setLanguage(extractedCode.language());

            AnalysisResponse response = analyzeCode(request);
            response.setExplanation("Source: " + extractedCode.description() + ". " + response.getExplanation());
            return response;
        } catch (IllegalArgumentException exception) {
            AnalysisResponse response = new AnalysisResponse();
            response.setBugs(List.of(exception.getMessage()));
            response.setFixedCode("");
            response.setExplanation("Upload a text source file or a zip archive containing source files.");
            response.setEdgeCasesToTest(List.of("Try a supported code file like .java, .py, .js, or a zip archive."));
            response.setTimeComplexity("Not available.");
            response.setSpaceComplexity("Not available.");
            response.setOptimality("Not available.");
            response.setLearningResources(defaultLearningResources());
            return response;
        } catch (IOException exception) {
            AnalysisResponse response = new AnalysisResponse();
            response.setBugs(List.of("The uploaded file could not be read."));
            response.setFixedCode("");
            response.setExplanation("Failed to read the uploaded content: " + sanitize(exception.getMessage()));
            response.setEdgeCasesToTest(List.of("Retry the upload and confirm the file is readable."));
            response.setTimeComplexity("Not available.");
            response.setSpaceComplexity("Not available.");
            response.setOptimality("Not available.");
            response.setLearningResources(defaultLearningResources());
            return response;
        }
    }

    private AnalysisResponse buildFallbackResponse(String code) {
        AnalysisResponse response = new AnalysisResponse();
        List<String> bugs = new ArrayList<>();

        if (code.contains("==")) {
            bugs.add("Possible misuse of '==' instead of equals()");
        }

        if (bugs.isEmpty()) {
            bugs.add("No obvious rule-based issues were detected. Configure OPENAI_API_KEY to enable deeper AI analysis.");
        }

        response.setBugs(bugs);
        response.setFixedCode(code);
        response.setExplanation("This is the local fallback analysis. Set OPENAI_API_KEY to use OpenAI-powered bug finding and code fixes.");
        response.setEdgeCasesToTest(List.of(
                "Test empty input.",
                "Test the smallest valid input.",
                "Test boundary index or pointer positions.",
                "Test duplicate and repeated values if relevant."));
        response.setTimeComplexity("Unknown in fallback mode.");
        response.setSpaceComplexity("Unknown in fallback mode.");
        response.setOptimality("Fallback mode cannot reliably judge whether the approach is optimal.");
        response.setLearningResources(defaultLearningResources());

        return response;
    }

    private List<String> defaultLearningResources() {
        return List.of(
                "LeetCode patterns guide: https://leetcode.com/discuss/study-guide/1688903/solved-all-two-pointers-problems-in-100-days",
                "Binary search study plan: https://leetcode.com/explore/learn/card/binary-search/",
                "Big-O cheat sheet: https://www.bigocheatsheet.com/");
    }

    private String sanitize(String message) {
        if (message == null || message.isBlank()) {
            return "Unknown failure.";
        }

        String singleLineMessage = message.replace('\n', ' ').trim();
        return OPENAI_KEY_PATTERN.matcher(singleLineMessage).replaceAll("[REDACTED_API_KEY]");
    }
}
