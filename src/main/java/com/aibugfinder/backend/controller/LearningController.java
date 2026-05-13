package com.aibugfinder.backend.controller;

import com.aibugfinder.backend.dto.ProblemDetailResponse;
import com.aibugfinder.backend.dto.ProblemListResponse;
import com.aibugfinder.backend.dto.TrackResponse;
import com.aibugfinder.backend.service.LearningService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class LearningController {
    private final LearningService learningService;

    public LearningController(LearningService learningService) {
        this.learningService = learningService;
    }

    @GetMapping("/tracks")
    public List<TrackResponse> tracks() {
        return learningService.tracks();
    }

    @GetMapping("/problems")
    public List<ProblemListResponse> problems(@RequestParam(value = "trackId", required = false) Long trackId) {
        return learningService.problems(trackId);
    }

    @GetMapping("/problems/{id}")
    public ProblemDetailResponse problem(@PathVariable Long id) {
        return learningService.problem(id);
    }
}
