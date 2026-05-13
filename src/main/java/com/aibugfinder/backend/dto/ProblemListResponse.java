package com.aibugfinder.backend.dto;

import com.aibugfinder.backend.entity.Difficulty;
import com.aibugfinder.backend.entity.Problem;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProblemListResponse {
    private Long id;
    private String title;
    private Difficulty difficulty;
    private Long trackId;
    private boolean solved;

    public static ProblemListResponse from(Problem problem, boolean solved) {
        return new ProblemListResponse(
                problem.getId(),
                problem.getTitle(),
                problem.getDifficulty(),
                problem.getTrack().getId(),
                solved);
    }
}
