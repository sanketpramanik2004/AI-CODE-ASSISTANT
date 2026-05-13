package com.aibugfinder.backend.service;

import com.aibugfinder.backend.entity.Problem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CodeEvaluationService {
    private final Judge0Client judge0Client;
    private final ProblemTestCaseParser testCaseParser;
    private final AiCodeEvaluationClient aiCodeEvaluationClient;

    public CodeEvaluationService(Judge0Client judge0Client,
            ProblemTestCaseParser testCaseParser,
            AiCodeEvaluationClient aiCodeEvaluationClient) {
        this.judge0Client = judge0Client;
        this.testCaseParser = testCaseParser;
        this.aiCodeEvaluationClient = aiCodeEvaluationClient;
    }

    public EvaluationResult evaluate(Problem problem, String code, String language) {
        List<TestCaseDefinition> testCases = testCaseParser.parse(problem.getTestCases());
        if (testCases.isEmpty()) {
            return new EvaluationResult(false, List.of(new TestCaseExecutionResult(
                    "",
                    "Configured test cases",
                    "No test cases are configured for this problem.",
                    false)));
        }

        if (judge0Client.isConfigured()) {
            return evaluateWithJudge0(testCases, code, language);
        }

        return evaluateWithAiOrLocalFallback(problem, code, language, testCases);
    }

    private EvaluationResult evaluateWithJudge0(List<TestCaseDefinition> testCases, String code, String language) {
        List<TestCaseExecutionResult> results = new ArrayList<>();
        for (TestCaseDefinition testCase : testCases) {
            Optional<String> output = judge0Client.run(code, language, testCase.input());
            String actualOutput = output.orElse("Execution service could not run this language.");
            boolean passed = normalize(actualOutput).equals(normalize(testCase.expectedOutput()));
            results.add(new TestCaseExecutionResult(testCase.input(), testCase.expectedOutput(), actualOutput, passed));
        }

        return new EvaluationResult(results.stream().allMatch(TestCaseExecutionResult::passed), results);
    }

    private EvaluationResult evaluateWithAiOrLocalFallback(Problem problem,
            String code,
            String language,
            List<TestCaseDefinition> testCases) {
        boolean looksSolved = aiCodeEvaluationClient.isCorrect(
                        problem.getTitle(),
                        problem.getDescription(),
                        problem.getTestCases(),
                        code,
                        language)
                .orElseGet(() -> localHeuristic(problem, code));

        List<TestCaseExecutionResult> results = testCases.stream()
                .map(testCase -> new TestCaseExecutionResult(
                        testCase.input(),
                        testCase.expectedOutput(),
                        looksSolved ? testCase.expectedOutput() : "AI/local evaluator marked this solution incorrect.",
                        looksSolved))
                .toList();

        return new EvaluationResult(looksSolved, results);
    }

    private boolean localHeuristic(Problem problem, String code) {
        if (code == null || code.isBlank() || normalize(code).equals(normalize(problem.getStarterCode()))) {
            return false;
        }

        String compactCode = code.replaceAll("\\s+", " ");
        if (compactCode.contains("return -1;") && !compactCode.contains("while") && !compactCode.contains("for")) {
            return false;
        }

        if ("Binary Search".equalsIgnoreCase(problem.getTitle())) {
            return compactCode.contains("while")
                    && compactCode.contains("left")
                    && compactCode.contains("right")
                    && compactCode.contains("mid")
                    && compactCode.contains("nums[mid] == target")
                    && compactCode.contains("left = mid + 1")
                    && compactCode.contains("right = mid - 1");
        }

        return !code.contains("TODO");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replace("\r\n", "\n");
    }
}
