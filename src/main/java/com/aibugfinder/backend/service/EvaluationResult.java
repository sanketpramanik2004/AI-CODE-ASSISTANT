package com.aibugfinder.backend.service;

import java.util.List;

public record EvaluationResult(boolean allPassed, List<TestCaseExecutionResult> testCases) {
}
