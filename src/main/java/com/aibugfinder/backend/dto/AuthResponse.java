package com.aibugfinder.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String accessToken;
    private long expiresInSeconds;
    private UserResponse user;

    public AuthResponse(String token, UserResponse user) {
        this.token = token;
        this.accessToken = token;
        this.expiresInSeconds = 0;
        this.user = user;
    }
}
