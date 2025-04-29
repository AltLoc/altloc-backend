package com.altloc.backend.api.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.altloc.backend.service.JwtService;
import com.altloc.backend.store.entities.UserEntity;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public void loginWithPassword(HttpServletResponse response, String email, String password) {
        Authentication auth = (Authentication) authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        generateAndSetTokens(response, auth);
    }

    public Authentication loginWithOAuth2(HttpServletResponse response, OAuth2User oauthUser) {
        String email = oauthUser.getAttribute("email");
        UserEntity user = (UserEntity) userDetailsService.loadUserByUsername(email);
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null);
        generateAndSetTokens(response, auth);
        return auth;
    }

    private void generateAndSetTokens(HttpServletResponse response, Authentication auth) {
        String accessToken = jwtService.generateAccessToken(auth);
        String refreshToken = jwtService.generateRefreshToken(auth);

        setCookie(response, "accessToken", accessToken, 60 * 15); // 15 мин
        setCookie(response, "refreshToken", refreshToken, 60 * 60 * 24 * 7); // 7 дней
    }

    private void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}
