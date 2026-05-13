package com.aibugfinder.backend.dto;

import lombok.Data;

@Data
public class SubmitCodeRequest {
    private Long problemId;
    private String language;
    private String code;
}
