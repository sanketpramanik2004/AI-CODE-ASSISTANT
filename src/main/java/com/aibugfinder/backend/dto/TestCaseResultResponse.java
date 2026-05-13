package com.aibugfinder.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TestCaseResultResponse {
    private String input;
    private String expectedOutput;
    private String actualOutput;
    private boolean passed;
}
