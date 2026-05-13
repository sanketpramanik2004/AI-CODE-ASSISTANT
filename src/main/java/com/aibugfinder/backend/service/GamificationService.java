package com.aibugfinder.backend.service;

import com.aibugfinder.backend.entity.Difficulty;
import com.aibugfinder.backend.entity.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class GamificationService {

    public int awardForSolvedProblem(User user, Difficulty difficulty) {
        int xpGained = xpFor(difficulty);
        user.setXp(user.getXp() + xpGained);
        user.setLevel(levelFor(user.getXp()));
        updateStreak(user);
        return xpGained;
    }

    public int levelFor(int xp) {
        return (int) Math.floor(Math.sqrt(xp / 10.0));
    }

    private int xpFor(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 10;
            case MEDIUM -> 25;
            case HARD -> 50;
        };
    }

    private void updateStreak(User user) {
        LocalDate today = LocalDate.now();
        LocalDate lastSolvedDate = user.getLastSolvedDate();

        if (today.equals(lastSolvedDate)) {
            return;
        }

        if (today.minusDays(1).equals(lastSolvedDate)) {
            user.setStreak(user.getStreak() + 1);
        } else {
            user.setStreak(1);
        }

        user.setLastSolvedDate(today);
    }
}
