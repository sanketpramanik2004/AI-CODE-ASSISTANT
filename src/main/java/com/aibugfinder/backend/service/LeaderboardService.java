package com.aibugfinder.backend.service;

import com.aibugfinder.backend.dto.LeaderboardEntryResponse;
import com.aibugfinder.backend.entity.User;
import com.aibugfinder.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LeaderboardService {
    private final UserRepository userRepository;

    public LeaderboardService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<LeaderboardEntryResponse> topUsers() {
        AtomicInteger rank = new AtomicInteger(1);
        return userRepository.findTop10ByOrderByXpDescLevelDesc().stream()
                .map(user -> LeaderboardEntryResponse.from(rank.getAndIncrement(), user))
                .toList();
    }

    public LeaderboardEntryResponse rankFor(User user) {
        int rank = Math.toIntExact(userRepository.countUsersAhead(user.getXp(), user.getLevel(), user.getId()) + 1);
        return LeaderboardEntryResponse.from(rank, user);
    }
}
