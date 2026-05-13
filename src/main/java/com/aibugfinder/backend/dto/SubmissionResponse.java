package com.aibugfinder.backend.dto;

import com.aibugfinder.backend.entity.SubmissionStatus;
import lombok.Data;

import java.util.List;

@Data
public class SubmissionResponse {
    private SubmissionStatus status;
    private String message;
    private int attemptNumber;
    private int hintLevel;
    private String hint;
    private int xpGained;
    private int totalXp;
    private int level;
    private int streak;
    private List<TestCaseResultResponse> testCases;
    private AnalysisResponse aiFeedback;
}
