package com.altloc.backend.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.altloc.backend.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class TokenFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@SuppressWarnings("null") HttpServletRequest request,
            @SuppressWarnings("null") HttpServletResponse response,
            @SuppressWarnings("null") FilterChain filterChain) throws ServletException, IOException {

        String jwt = extractToken(request);
        if (jwt != null && jwtService.validateToken(jwt)) {
            authenticateUser(jwt);
        } else {
            processRefreshToken(request, response);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT from the request. It checks both cookies and the
     * Authorization
     * header.
     *
     * @param request The HTTP request
     * @return The JWT if found, null otherwise
     */
    private String extractToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            Optional<String> tokenFromCookie = Arrays.stream(request.getCookies())
                    .filter(cookie -> "accessToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst();
            if (tokenFromCookie.isPresent()) {
                log.info("JWT from cookie");
                return tokenFromCookie.get();
            }
        }

        // Check for JWT in the Authorization header
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            log.info("JWT from header");
            return headerAuth.substring(7);
        }

        return null;
    }

    private void authenticateUser(String jwt) {
        String email = jwtService.getUsernameFromToken(jwt);
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
                    null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("User {} authenticated", email);
        }
    }

    private void processRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = request.getHeader("Refresh-Token");
        if (refreshToken != null && jwtService.validateRefreshToken(refreshToken)) {
            String email = jwtService.getUsernameFromToken(refreshToken);
            if (email != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                if (userDetails != null) {
                    String newAccessToken = jwtService.generateAccessToken(
                            SecurityContextHolder.getContext().getAuthentication());
                    response.setHeader("Authorization", "Bearer " + newAccessToken);
                    log.info("New Access Token issued for {}", email);

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails,
                            null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } else if (refreshToken != null) {
            log.warn("Invalid refresh token");
        }
    }
}
