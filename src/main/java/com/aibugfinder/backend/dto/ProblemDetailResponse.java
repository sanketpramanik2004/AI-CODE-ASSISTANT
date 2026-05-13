package com.aibugfinder.backend.dto;

import com.aibugfinder.backend.entity.Difficulty;
import com.aibugfinder.backend.entity.Problem;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProblemDetailResponse {
    private Long id;
    private String title;
    private String description;
    private Difficulty difficulty;
    private Long trackId;
    private String trackName;
    private String starterCode;
    private String testCases;
    private boolean solved;

    public static ProblemDetailResponse from(Problem problem, boolean solved) {
        return new ProblemDetailResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getDescription(),
                problem.getDifficulty(),
                problem.getTrack().getId(),
                problem.getTrack().getName(),
                problem.getStarterCode(),
                problem.getTestCases(),
                solved);
    }
}
