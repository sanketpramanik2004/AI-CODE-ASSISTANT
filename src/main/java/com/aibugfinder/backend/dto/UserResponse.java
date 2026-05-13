package com.aibugfinder.backend.dto;

import com.aibugfinder.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private int xp;
    private int level;
    private int streak;

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getXp(),
                user.getLevel(),
                user.getStreak());
    }
}
