package com.altloc.backend.api.auth.password;

import com.altloc.backend.config.SecurityConfig;
import com.altloc.backend.model.LoginDTO;
import com.altloc.backend.model.LoginResponse;
import com.altloc.backend.model.RegistrationDTO;
import com.altloc.backend.service.JwtService;
import com.altloc.backend.service.UserService;
import com.altloc.backend.store.entity.PasswordEntity;
import com.altloc.backend.store.entity.UserEntity;
import com.altloc.backend.store.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final SecurityConfig securityConfig;

    public ResponseEntity<?> registerUser(RegistrationDTO request) {
        if (userRepository.existsUserByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose a different username");
        }
        if (userRepository.existsUserByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Choose a different email");
        }

        UserEntity user = UserEntity.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .emailVerified(false)
                .role(null)
                .score(0)
                .level(1)
                .avatarKey("")
                .currency(0)
                .build();

        PasswordEntity passwordEntity = PasswordEntity.builder()
                .user(user)
                .passwordHashed(securityConfig.passwordEncoder().encode(request.getPassword()))
                .build();

        user.setPasswordAccount(passwordEntity);
        userRepository.save(user);

        return ResponseEntity.ok("User successfully registered");
    }

    public LoginResponse loginUser(LoginDTO requestLogin, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestLogin.getEmail(),
                            requestLogin.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtService.generateAccessToken(authentication);
            String refreshToken = jwtService.generateRefreshToken(authentication);

            setAuthCookies(response, jwt, refreshToken);

            return new LoginResponse(jwt, refreshToken);
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
    }

    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No cookies found");
        }

        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }

        if (refreshToken == null || !jwtService.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        String email = jwtService.getUsernameFromToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String newAccessToken = jwtService.generateAccessToken(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        setAuthCookie(response, "accessToken", newAccessToken, 60 * 15);
        return ResponseEntity.ok("Successfully refreshed token");
    }

    public ResponseEntity<?> logoutUser(HttpServletResponse response) {
        clearAuthCookies(response);
        return ResponseEntity.ok("Successfully logged out");
    }

    private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        setAuthCookie(response, "accessToken", accessToken, 60 * 15);
        setAuthCookie(response, "refreshToken", refreshToken, 60 * 60 * 24 * 7);
    }

    private void setAuthCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private void clearAuthCookies(HttpServletResponse response) {
        setAuthCookie(response, "accessToken", null, 0);
        setAuthCookie(response, "refreshToken", null, 0);
    }
}
