package com.aibugfinder.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class ProblemTestCaseParser {
    private final ObjectMapper objectMapper;

    public ProblemTestCaseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<TestCaseDefinition> parse(String testCasesJson) {
        if (testCasesJson == null || testCasesJson.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(testCasesJson, new TypeReference<>() {
            });
        } catch (IOException exception) {
            throw new IllegalArgumentException("Problem test cases are not valid JSON.", exception);
        }
    }
}
