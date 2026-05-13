package com.aibugfinder.backend.service;

import com.aibugfinder.backend.entity.RefreshToken;
import com.aibugfinder.backend.entity.User;
import com.aibugfinder.backend.exception.UnauthorizedException;
import com.aibugfinder.backend.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Transactional
    public RefreshToken create(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000));
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken rotate(String token) {
        RefreshToken current = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Refresh session is invalid."));

        if (!current.isActive()) {
            throw new UnauthorizedException("Refresh session expired.");
        }

        current.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(current);
        return create(current.getUser());
    }

    @Transactional
    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(refreshToken);
        });
    }

    @Transactional
    public void revokeAll(User user) {
        refreshTokenRepository.deleteByUserId(user.getId());
    }

    public int cookieMaxAgeSeconds(boolean rememberMe) {
        if (!rememberMe) {
            return -1;
        }

        return Math.toIntExact(refreshExpirationMs / 1000);
    }
}
