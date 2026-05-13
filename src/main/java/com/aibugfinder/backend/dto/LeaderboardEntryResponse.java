package com.aibugfinder.backend.dto;

import com.aibugfinder.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaderboardEntryResponse {
    private int rank;
    private String name;
    private int xp;
    private int level;
    private int streak;

    public static LeaderboardEntryResponse from(int rank, User user) {
        return new LeaderboardEntryResponse(rank, user.getName(), user.getXp(), user.getLevel(), user.getStreak());
    }
}
