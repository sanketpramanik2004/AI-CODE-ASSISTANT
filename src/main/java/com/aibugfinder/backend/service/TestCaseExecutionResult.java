package com.aibugfinder.backend.service;

public record TestCaseExecutionResult(String input, String expectedOutput, String actualOutput, boolean passed) {
}
