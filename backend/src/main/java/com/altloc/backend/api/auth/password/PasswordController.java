package com.altloc.backend.api.auth.password;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.altloc.backend.config.SecurityConfig;
import com.altloc.backend.model.LoginDTO;
import com.altloc.backend.model.LoginResponse;
import com.altloc.backend.model.RegistrationDTO;
import com.altloc.backend.service.JwtService;
import com.altloc.backend.service.UserService;
import com.altloc.backend.store.entity.PasswordEntity;
import com.altloc.backend.store.entity.UserEntity;
import com.altloc.backend.store.repository.UserRepository;

@Getter
@Setter
@AllArgsConstructor
@RestController
@RequestMapping("/auth/password")
public class PasswordController {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final JwtService jwtService;

    @Autowired
    private final UserService userDetailsService;

    @Autowired
    private final AuthenticationManager authenticationManager;

    @Autowired
    private final SecurityConfig securityConfig;

    @PostMapping("/registration")
    public ResponseEntity<?> registration(@Valid @RequestBody RegistrationDTO request) {
        try {
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
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Something went wrong during registration");
        }
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginDTO requestLogin, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestLogin.getEmail(),
                            requestLogin.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtService.generateAccessToken(authentication);
            String refreshToken = jwtService.generateRefreshToken(authentication);

            Cookie accessTokenCookie = new Cookie("accessToken", jwt);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(true); // Switch to true in production (HTTPS)
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(60 * 15);

            Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(60 * 60 * 24 * 7);

            response.addCookie(accessTokenCookie);
            response.addCookie(refreshTokenCookie);

            return LoginResponse.builder()
                    .accessToken(jwt)
                    .refreshToken(refreshToken)
                    .build();

        } catch (BadCredentialsException e) {
            System.out.println("login error " + e);
            return null;
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Получаем куки из запроса
            Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No cookies found");
            }

            String refreshToken = null;

            // Ищем куку с именем "refreshToken"
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }

            if (refreshToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token not found");
            }

            System.out.println("Refresh token: " + refreshToken);

            if (jwtService.validateRefreshToken(refreshToken)) {
                String email = jwtService.getUsernameFromToken(refreshToken);

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                String newAccessToken = jwtService.generateAccessToken(
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

                Cookie accessTokenCookie = new Cookie("accessToken", newAccessToken);
                accessTokenCookie.setHttpOnly(true);
                accessTokenCookie.setSecure(true); // Включить в продакшене (HTTPS)
                accessTokenCookie.setPath("/");
                accessTokenCookie.setMaxAge(60 * 15); // 15 минут

                response.addCookie(accessTokenCookie);

                return ResponseEntity.ok("Successfully refreshed token");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error refreshing token");
        }
    }

    // @PostMapping("/refresh-token")
    // public ResponseEntity<?> refreshToken(@RequestParam String refreshToken,
    // HttpServletResponse response) {

    // try {
    // System.out.println("Refresh token: " + refreshToken);

    // if (jwtService.validateRefreshToken(refreshToken)) {
    // String email = jwtService.getUsernameFromToken(refreshToken);

    // UserDetails userDetails = userDetailsService.loadUserByUsername(email);

    // String newAccessToken = jwtService.generateAccessToken(
    // new UsernamePasswordAuthenticationToken(userDetails, null,
    // userDetails.getAuthorities()));

    // Cookie accessTokenCookie = new Cookie("accessToken", newAccessToken);
    // accessTokenCookie.setHttpOnly(true);
    // accessTokenCookie.setSecure(true); // Switch to true in production (HTTPS)
    // accessTokenCookie.setPath("/");
    // accessTokenCookie.setMaxAge(60 * 15);

    // response.addCookie(accessTokenCookie);

    // return ResponseEntity.ok("Successfully refreshed token");
    // } else {
    // return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh
    // token");
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error
    // refreshing token");
    // }
    // }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        try {
            Cookie accessTokenCookie = new Cookie("accessToken", null);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(true); // Switch to true in production (HTTPS)
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(0);

            Cookie refreshTokenCookie = new Cookie("refreshToken", null);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0);

            response.addCookie(accessTokenCookie);
            response.addCookie(refreshTokenCookie);
            return ResponseEntity.ok("Successfully logged out");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during logout");
        }
    }

}