package com.altloc.backend.api.auth.google;

import com.altloc.backend.service.JwtService;
import com.altloc.backend.service.MinioService;
import com.altloc.backend.service.UserService;
import com.altloc.backend.store.entities.UserEntity;
import com.altloc.backend.store.entities.auth.GoogleEntity;
import com.altloc.backend.store.repositories.UserRepository;
import com.altloc.backend.store.repositories.auth.GoogleRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthGoogleController implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final GoogleRepository googleRepository;
    private final MinioService minioService;

    @Value("${frontend.redirect.url:http://localhost:3000/user/quests}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String googleId = oauthUser.getAttribute("sub");
        String avatarUrl = oauthUser.getAttribute("picture");

        if (email == null || email.isEmpty() || googleId == null || googleId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing email or Google ID");
            return;
        }

        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() -> registerNewUser(email));

        Optional<GoogleEntity> existingGoogleAccount = googleRepository.findByGoogleId(googleId);
        if (existingGoogleAccount.isPresent() && !existingGoogleAccount.get().getUser().getId().equals(user.getId())) {
            response.sendError(HttpServletResponse.SC_CONFLICT, "Google ID already linked to another user");
            return;
        }

        if (user.getGoogleAccount() == null) {
            linkGoogleAccount(user, googleId);
        }

        if (avatarUrl != null && !avatarUrl.isEmpty() && user.getAvatarKey() == null) {
            try {
                uploadAndSetAvatar(user, avatarUrl, googleId);
            } catch (Exception e) {
                log.warn("Failed to upload avatar for user {}: {}", email, e.getMessage());
            }
        }

        String accessToken = jwtService.generateAccessToken(createAuthToken(email));
        String refreshToken = jwtService.generateRefreshToken(createAuthToken(email));

        setAuthCookies(response, accessToken, refreshToken);
        response.sendRedirect(frontendRedirectUrl);
    }

    private UserEntity registerNewUser(String email) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setUsername(email.split("@")[0]);
        user.setEmailVerified(true);
        return userRepository.save(user);
    }

    private void linkGoogleAccount(UserEntity user, String googleId) {
        GoogleEntity googleAccount = GoogleEntity.builder()
                .user(user)
                .googleId(googleId)
                .build();
        user.setGoogleAccount(googleAccount);
        userRepository.save(user);
    }

    private void uploadAndSetAvatar(UserEntity user, String avatarUrl, String googleId) throws IOException {
        String avatarKey = "users/avatar/" + googleId + ".jpg";

        try (InputStream avatarStream = URI.create(avatarUrl).toURL().openStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            byte[] data = new byte[8192];
            int bytesRead;
            while ((bytesRead = avatarStream.read(data)) != -1) {
                buffer.write(data, 0, bytesRead);
            }

            byte[] avatarBytes = buffer.toByteArray();
            long avatarSize = avatarBytes.length;

            try (InputStream uploadStream = new java.io.ByteArrayInputStream(avatarBytes)) {
                minioService.uploadFile(avatarKey, uploadStream, avatarSize, "image/jpeg");
            }

            user.setAvatarKey(avatarKey);
            userRepository.save(user);
        }
    }

    private Authentication createAuthToken(String email) {
        return new UsernamePasswordAuthenticationToken(userService.loadUserByUsername(email), null);
    }

    private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        setAuthCookie(response, "accessToken", accessToken, 60 * 15);
        setAuthCookie(response, "refreshToken", refreshToken, 60 * 60 * 24 * 7);
    }

    private void setAuthCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Set to true in production
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}
