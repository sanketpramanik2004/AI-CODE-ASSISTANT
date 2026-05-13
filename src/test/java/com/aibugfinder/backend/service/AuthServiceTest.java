package com.aibugfinder.backend.service;

import com.aibugfinder.backend.dto.AuthRequest;
import com.aibugfinder.backend.dto.AuthResponse;
import com.aibugfinder.backend.dto.SignupRequest;
import com.aibugfinder.backend.entity.RefreshToken;
import com.aibugfinder.backend.entity.User;
import com.aibugfinder.backend.exception.BadRequestException;
import com.aibugfinder.backend.exception.UnauthorizedException;
import com.aibugfinder.backend.repository.UserRepository;
import com.aibugfinder.backend.security.CookieService;
import com.aibugfinder.backend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Test
    void signupHashesPasswordAndReturnsToken() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
        AuthService service = new AuthService(userRepository, passwordEncoder, jwtService, refreshTokenService, new CookieService());
        SignupRequest request = new SignupRequest();
        request.setName("Sanket");
        request.setEmail("SANKET@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        when(userRepository.existsByEmail("sanket@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(7L);
            return user;
        });
        when(jwtService.generateToken("sanket@example.com")).thenReturn("jwt-token");
        when(jwtService.expirationSeconds()).thenReturn(86400L);
        when(refreshTokenService.create(any(User.class))).thenAnswer(invocation -> refreshToken(invocation.getArgument(0)));
        when(refreshTokenService.cookieMaxAgeSeconds(false)).thenReturn(-1);

        AuthResponse response = service.signup(request, new MockHttpServletResponse());

        assertEquals("jwt-token", response.getToken());
        assertEquals("sanket@example.com", response.getUser().getEmail());
    }

    @Test
    void signupRejectsDuplicateEmail() {
        UserRepository userRepository = mock(UserRepository.class);
        AuthService service = service(userRepository, mock(PasswordEncoder.class), mock(JwtService.class), mock(RefreshTokenService.class));
        SignupRequest request = new SignupRequest();
        request.setName("Sanket");
        request.setEmail("sanket@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");

        when(userRepository.existsByEmail("sanket@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> service.signup(request, new MockHttpServletResponse()));
    }

    @Test
    void loginRejectsInvalidPassword() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthService service = service(userRepository, passwordEncoder, mock(JwtService.class), mock(RefreshTokenService.class));
        AuthRequest request = new AuthRequest();
        request.setEmail("sanket@example.com");
        request.setPassword("wrong-password");

        User user = new User();
        user.setEmail("sanket@example.com");
        user.setPassword("hashed-password");
        when(userRepository.findByEmail("sanket@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> service.login(request, new MockHttpServletResponse()));
    }

    @Test
    void loginReturnsJwtForValidCredentials() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
        AuthService service = service(userRepository, passwordEncoder, jwtService, refreshTokenService);
        AuthRequest request = new AuthRequest();
        request.setEmail("sanket@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setName("Sanket");
        user.setEmail("sanket@example.com");
        user.setPassword("hashed-password");
        when(userRepository.findByEmail("sanket@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken("sanket@example.com")).thenReturn("jwt-token");
        when(jwtService.expirationSeconds()).thenReturn(86400L);
        when(refreshTokenService.create(any(User.class))).thenAnswer(invocation -> refreshToken(invocation.getArgument(0)));
        when(refreshTokenService.cookieMaxAgeSeconds(false)).thenReturn(-1);

        AuthResponse response = service.login(request, new MockHttpServletResponse());

        assertEquals("jwt-token", response.getToken());
        assertTrue(response.getUser().getName().contains("Sanket"));
    }

    private AuthService service(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
            RefreshTokenService refreshTokenService) {
        return new AuthService(userRepository, passwordEncoder, jwtService, refreshTokenService, new CookieService());
    }

    private RefreshToken refreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken("refresh-token");
        return refreshToken;
    }
}
