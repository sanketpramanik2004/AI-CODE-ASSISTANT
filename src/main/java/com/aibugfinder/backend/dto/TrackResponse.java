package com.aibugfinder.backend.dto;

import com.aibugfinder.backend.entity.Track;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrackResponse {
    private Long id;
    private String name;
    private String description;
    private long solvedProblems;
    private long totalProblems;

    public static TrackResponse from(Track track, long solvedProblems, long totalProblems) {
        return new TrackResponse(
                track.getId(),
                track.getName(),
                track.getDescription(),
                solvedProblems,
                totalProblems);
    }
}
