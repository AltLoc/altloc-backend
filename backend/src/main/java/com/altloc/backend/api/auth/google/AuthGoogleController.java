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

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

@RequiredArgsConstructor
@Component
public class AuthGoogleController implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final GoogleRepository googleRepository;
    private final MinioService minioService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String googleId = oauthUser.getAttribute("sub");
        String avatarUrl = oauthUser.getAttribute("picture");

        if (email == null || email.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not found in Google response");
            return;
        }

        if (googleId == null || googleId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Google ID not found in Google response");
            return;
        }

        // Check if the user already exists in the database
        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    UserEntity newUser = new UserEntity();
                    newUser.setEmail(email);
                    newUser.setUsername(email.split("@")[0]);
                    newUser.setEmailVerified(true);
                    return userRepository.save(newUser);
                });

        // Check if the user already has a Google account linked
        if (user.getGoogleAccount() == null) {
            boolean alreadyLinked = googleRepository.findByGoogleId(googleId).isPresent();
            if (alreadyLinked) {
                response.sendError(HttpServletResponse.SC_CONFLICT, "Google ID already linked to another user");
                return;
            }

            GoogleEntity googleAccount = GoogleEntity.builder()
                    .user(user)
                    .googleId(googleId)
                    .build();
            user.setGoogleAccount(googleAccount);
            userRepository.save(user);
        }

        // Handle avatar URL and upload the avatar image to MinIO
        // if (avatarUrl != null && !avatarUrl.isEmpty()) {
        // try {
        // String avatarKey = "users/avatar/" + googleId + ".jpg"; // Generate a unique
        // key for the avatar
        // try (InputStream avatarStream = URI.create(avatarUrl).toURL().openStream()) {
        // long avatarSize = avatarStream.available();
        // String contentType = "image/jpeg"; // Assuming the avatar is a JPEG image
        // minioService.uploadFile(avatarKey, avatarStream, avatarSize, contentType); //
        // Upload the avatar
        // // image to MinIO
        // }
        // user.setAvatarKey(avatarKey);
        // userRepository.save(user);
        // } catch (Exception e) {
        // throw new RuntimeException("Error uploading avatar from Google: " +
        // e.getMessage(), e);
        // }
        // }

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            try {
                String avatarKey = "users/avatar/" + googleId + ".jpg";

                try (InputStream avatarStream = URI.create(avatarUrl).toURL().openStream();
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

                    byte[] data = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = avatarStream.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, bytesRead);
                    }

                    byte[] avatarBytes = buffer.toByteArray();
                    long avatarSize = avatarBytes.length;
                    String contentType = "image/jpeg"; // предположим JPEG

                    // повторно открываем поток из массива байтов
                    try (InputStream uploadStream = new java.io.ByteArrayInputStream(avatarBytes)) {
                        minioService.uploadFile(avatarKey, uploadStream, avatarSize, contentType);
                    }
                }

                user.setAvatarKey(avatarKey);
                userRepository.save(user);

            } catch (Exception e) {
                throw new RuntimeException("Ошибка загрузки аватара из Google: " + e.getMessage(), e);
            }
        }

        Authentication authToken = new UsernamePasswordAuthenticationToken(userService.loadUserByUsername(email), null);
        String accessToken = jwtService.generateAccessToken(authToken);
        String refreshToken = jwtService.generateRefreshToken(authToken);

        setAuthCookies(response, accessToken, refreshToken);

        response.sendRedirect("http://localhost:3000/user/quests");
    }

    private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        setAuthCookie(response, "accessToken", accessToken, 60 * 15);
        setAuthCookie(response, "refreshToken", refreshToken, 60 * 60 * 24 * 7);
    }

    private void setAuthCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // true if using HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}
