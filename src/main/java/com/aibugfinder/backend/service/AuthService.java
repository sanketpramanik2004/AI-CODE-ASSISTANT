package com.aibugfinder.backend.service;

import com.aibugfinder.backend.dto.AuthRequest;
import com.aibugfinder.backend.dto.AuthResponse;
import com.aibugfinder.backend.dto.RefreshRequest;
import com.aibugfinder.backend.dto.SignupRequest;
import com.aibugfinder.backend.dto.UserResponse;
import com.aibugfinder.backend.entity.AuthProvider;
import com.aibugfinder.backend.entity.RefreshToken;
import com.aibugfinder.backend.entity.User;
import com.aibugfinder.backend.exception.BadRequestException;
import com.aibugfinder.backend.exception.UnauthorizedException;
import com.aibugfinder.backend.repository.UserRepository;
import com.aibugfinder.backend.security.CookieService;
import com.aibugfinder.backend.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CookieService cookieService;

    public AuthService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            CookieService cookieService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.cookieService = cookieService;
    }

    public AuthResponse signup(SignupRequest request, HttpServletResponse response) {
        validateSignup(request);
        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists.");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(normalizePassword(request.getPassword())));
        user.setProvider(AuthProvider.LOCAL);

        User savedUser = userRepository.save(user);
        return issueSession(savedUser, request.isRememberMe(), response);
    }

    public AuthResponse login(AuthRequest request, HttpServletResponse response) {
        if (request == null || isBlank(request.getEmail()) || isBlank(request.getPassword())) {
            throw new BadRequestException("Email and password are required.");
        }

        User user = userRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (!passwordEncoder.matches(normalizePassword(request.getPassword()), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        return issueSession(user, request.isRememberMe(), response);
    }

    public AuthResponse refresh(RefreshRequest request, String refreshCookie, HttpServletResponse response) {
        String refreshToken = firstNonBlank(refreshCookie, request == null ? null : request.getRefreshToken());
        if (isBlank(refreshToken)) {
            throw new UnauthorizedException("Refresh session is missing.");
        }

        RefreshToken rotated = refreshTokenService.rotate(refreshToken);
        cookieService.addRefreshCookie(response, rotated.getToken(), refreshTokenService.cookieMaxAgeSeconds(true));
        return accessResponse(rotated.getUser());
    }

    public void logout(String refreshCookie, HttpServletResponse response) {
        refreshTokenService.revoke(refreshCookie);
        cookieService.clearRefreshCookie(response);
    }

    public AuthResponse issueSession(User user, boolean rememberMe, HttpServletResponse response) {
        RefreshToken refreshToken = refreshTokenService.create(user);
        cookieService.addRefreshCookie(response, refreshToken.getToken(), refreshTokenService.cookieMaxAgeSeconds(rememberMe));
        return accessResponse(user);
    }

    public User findOrCreateOAuthUser(String email, String name, AuthProvider provider, String providerId) {
        String normalizedEmail = normalizeEmail(email);
        return userRepository.findByEmail(normalizedEmail)
                .map(existingUser -> {
                    existingUser.setProvider(provider);
                    existingUser.setProviderId(providerId);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    User user = new User();
                    user.setName(isBlank(name) ? normalizedEmail : name.trim());
                    user.setEmail(normalizedEmail);
                    user.setProvider(provider);
                    user.setProviderId(providerId);
                    return userRepository.save(user);
                });
    }

    private AuthResponse accessResponse(User user) {
        String accessToken = jwtService.generateToken(user.getEmail());
        AuthResponse response = new AuthResponse();
        response.setToken(accessToken);
        response.setAccessToken(accessToken);
        response.setExpiresInSeconds(jwtService.expirationSeconds());
        response.setUser(UserResponse.from(user));
        return response;
    }

    private void validateSignup(SignupRequest request) {
        if (request == null || isBlank(request.getName()) || isBlank(request.getEmail()) || isBlank(request.getPassword())) {
            throw new BadRequestException("Name, email, and password are required.");
        }

        if (request.getPassword().length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters.");
        }

        if (!normalizePassword(request.getPassword()).equals(normalizePassword(request.getConfirmPassword()))) {
            throw new BadRequestException("Passwords do not match.");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizePassword(String password) {
        return password == null ? "" : password.trim();
    }

    private String firstNonBlank(String first, String second) {
        return !isBlank(first) ? first : second;
    }
}
