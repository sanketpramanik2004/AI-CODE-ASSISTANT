package com.aibugfinder.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AnalysisResponse {
    private List<String> bugs;
    private String fixedCode;
    private String explanation;
    private List<String> edgeCasesToTest;
    private String timeComplexity;
    private String spaceComplexity;
    private String optimality;
    private List<String> learningResources;
}
