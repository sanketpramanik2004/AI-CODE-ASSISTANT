package com.aibugfinder.backend.controller;

import com.aibugfinder.backend.dto.LeaderboardEntryResponse;
import com.aibugfinder.backend.service.AuthenticatedUserService;
import com.aibugfinder.backend.service.LeaderboardService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class LeaderboardController {
    private final LeaderboardService leaderboardService;
    private final AuthenticatedUserService authenticatedUserService;

    public LeaderboardController(LeaderboardService leaderboardService, AuthenticatedUserService authenticatedUserService) {
        this.leaderboardService = leaderboardService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping("/leaderboard")
    public List<LeaderboardEntryResponse> leaderboard() {
        return leaderboardService.topUsers();
    }

    @GetMapping("/leaderboard/me")
    public LeaderboardEntryResponse myRank() {
        return leaderboardService.rankFor(authenticatedUserService.currentUser());
    }
}
