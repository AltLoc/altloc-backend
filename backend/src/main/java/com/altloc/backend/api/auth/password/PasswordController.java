package com.altloc.backend.api.auth.password;

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
import org.springframework.web.bind.annotation.*;

import com.altloc.backend.config.SecurityConfig;
import com.altloc.backend.model.JwtResponseDTO;
import com.altloc.backend.model.LoginDTO;
import com.altloc.backend.model.RegistrationDTO;
import com.altloc.backend.store.entity.PasswordEntity;
import com.altloc.backend.store.entity.UserEntity;
import com.altloc.backend.store.repository.UserRepository;
import com.altloc.backend.utils.JwtCore;

@Getter
@Setter
@AllArgsConstructor
@RestController
@RequestMapping("/auth/password")
public class PasswordController {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final JwtCore jwtCore;

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
                    .role("USER")
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
            return ResponseEntity.badRequest().body("Something went wrong");
        }
    }

    @PostMapping("/login")
    public JwtResponseDTO login(@Valid @RequestBody LoginDTO requestLogin) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestLogin.getEmail(),
                            requestLogin.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtCore.generateToken(authentication);
            String refreshToken = jwtCore.generateRefreshToken(authentication);

            return JwtResponseDTO.builder()
                    .accessToken(jwt)
                    .token(refreshToken)
                    .build();

        } catch (BadCredentialsException e) {
            System.out.println("login error " + e);
            return JwtResponseDTO.builder()
                    .accessToken(null)
                    .token(null)
                    .build();
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestParam String refreshToken) {
        try {
            // Проверяем, действителен ли refresh токен
            if (jwtCore.validateRefreshToken(refreshToken)) {
                String email = jwtCore.getUsernameFromToken(refreshToken);
                // Генерируем новый access токен
                String newAccessToken = jwtCore.generateToken(authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(email, null)));
                return ResponseEntity.ok(newAccessToken);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error refreshing token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String refreshToken) {
        try {
            jwtCore.deleteRefreshToken(refreshToken);
            return ResponseEntity.ok("Successfully logged out");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during logout");
        }
    }

}