package com.aibugfinder.backend.controller;

import com.aibugfinder.backend.dto.AuthRequest;
import com.aibugfinder.backend.dto.AuthResponse;
import com.aibugfinder.backend.dto.RefreshRequest;
import com.aibugfinder.backend.dto.SignupRequest;
import com.aibugfinder.backend.dto.UserResponse;
import com.aibugfinder.backend.service.AuthenticatedUserService;
import com.aibugfinder.backend.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
    private final AuthService authService;
    private final AuthenticatedUserService authenticatedUserService;

    public AuthController(AuthService authService, AuthenticatedUserService authenticatedUserService) {
        this.authService = authService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody SignupRequest request, HttpServletResponse response) {
        return authService.signup(request, response);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request, HttpServletResponse response) {
        return authService.login(request, response);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody(required = false) RefreshRequest request,
            @CookieValue(value = "refresh_token", required = false) String refreshCookie,
            HttpServletResponse response) {
        return authService.refresh(request, refreshCookie, response);
    }

    @PostMapping("/logout")
    public void logout(@CookieValue(value = "refresh_token", required = false) String refreshCookie,
            HttpServletResponse response) {
        authService.logout(refreshCookie, response);
    }

    @GetMapping("/me")
    public UserResponse me() {
        return UserResponse.from(authenticatedUserService.currentUser());
    }
}
