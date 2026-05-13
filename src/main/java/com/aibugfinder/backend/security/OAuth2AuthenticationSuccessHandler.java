package com.aibugfinder.backend.security;

import com.aibugfinder.backend.dto.AuthResponse;
import com.aibugfinder.backend.entity.AuthProvider;
import com.aibugfinder.backend.entity.User;
import com.aibugfinder.backend.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final AuthService authService;
    private final String frontendUrl;

    public OAuth2AuthenticationSuccessHandler(AuthService authService,
            @Value("${app.frontend-url}") String frontendUrl) {
        this.authService = authService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User principal = oauthToken.getPrincipal();
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        AuthProvider provider = provider(registrationId);
        Map<String, Object> attributes = principal.getAttributes();

        String providerId = value(attributes, "sub", "id");
        String email = value(attributes, "email");
        String name = value(attributes, "name", "login", "localizedFirstName");

        if (email == null || email.isBlank()) {
            email = registrationId + "-" + providerId + "@oauth.local";
        }

        User user = authService.findOrCreateOAuthUser(email, name, provider, providerId);
        AuthResponse session = authService.issueSession(user, true, response);
        response.sendRedirect(frontendUrl + "/oauth/callback?status=success&provider="
                + URLEncoder.encode(registrationId, StandardCharsets.UTF_8)
                + "&token=" + URLEncoder.encode(session.getAccessToken(), StandardCharsets.UTF_8));
    }

    private AuthProvider provider(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> AuthProvider.GOOGLE;
            case "github" -> AuthProvider.GITHUB;
            case "linkedin" -> AuthProvider.LINKEDIN;
            default -> AuthProvider.LOCAL;
        };
    }

    private String value(Map<String, Object> attributes, String... keys) {
        for (String key : keys) {
            Object value = attributes.get(key);
            if (value != null && !value.toString().isBlank()) {
                return value.toString();
            }
        }

        return null;
    }
}
