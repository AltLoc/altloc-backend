// package com.altloc.backend.config;

// import java.io.IOException;

// import
// org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import com.altloc.backend.service.JwtService;

// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import lombok.RequiredArgsConstructor;

// @RequiredArgsConstructor
// @Component
// public class TokenFilter extends OncePerRequestFilter {

// private JwtService jwtService;
// private UserDetailsService userDetailsService;

// @Override
// protected void doFilterInternal(@SuppressWarnings("null") HttpServletRequest
// request,
// @SuppressWarnings("null") HttpServletResponse response,
// @SuppressWarnings("null") FilterChain filterChain)
// throws ServletException, IOException {

// String jwt = null;
// String accessTokenFromCookie = null;

// jakarta.servlet.http.Cookie[] cookies = request.getCookies();
// if (cookies != null) {
// for (jakarta.servlet.http.Cookie cookie : cookies) {
// if ("accessToken".equals(cookie.getName())) {
// accessTokenFromCookie = cookie.getValue();
// break;
// }
// }
// }

// if (accessTokenFromCookie != null) {
// jwt = accessTokenFromCookie;
// System.out.println("JWT get from cookie: " + jwt);
// } else {
// String headerAuth = request.getHeader("Authorization");
// if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
// jwt = headerAuth.substring(7);
// System.out.println("JWT get from header: " + jwt);
// }
// }

// if (jwt != null && jwtService.validateToken(jwt)) {
// String email = jwtService.getUsernameFromToken(jwt);
// System.out.println("Username from JWT: " + email);

// if (email != null && SecurityContextHolder.getContext().getAuthentication()
// == null) {
// UserDetails userDetails = userDetailsService.loadUserByUsername(email);

// UsernamePasswordAuthenticationToken authentication = new
// UsernamePasswordAuthenticationToken(
// userDetails, null, userDetails.getAuthorities());
// SecurityContextHolder.getContext().setAuthentication(authentication);
// }
// } else {

// String refreshTokenHeader = request.getHeader("Refresh-Token");
// if (refreshTokenHeader != null) {
// String refreshToken = refreshTokenHeader;
// System.out.println("Refresh Token get from header: " + refreshToken);
// if (jwtService.validateRefreshToken(refreshToken)) {
// String email = jwtService.getUsernameFromToken(refreshToken);
// System.out.println("Username from Refresh Token: " + email);

// if (email != null) {
// String newAccessToken = jwtService.generateAccessToken(
// SecurityContextHolder.getContext().getAuthentication());
// response.setHeader("Authorization", "Bearer " + newAccessToken);

// UserDetails userDetails = userDetailsService.loadUserByUsername(email);
// if (userDetails != null) {
// UsernamePasswordAuthenticationToken auth = new
// UsernamePasswordAuthenticationToken(
// userDetails, null, userDetails.getAuthorities());
// SecurityContextHolder.getContext().setAuthentication(auth);
// }
// }
// } else {
// System.out.println("Refresh Token is invalid");
// }
// }
// }

// filterChain.doFilter(request, response);
// }
// }

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
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String jwt = extractToken(request);
        if (jwt != null && jwtService.validateToken(jwt)) {
            authenticateUser(jwt);
        } else {
            processRefreshToken(request, response);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        // Получаем accessToken из cookie
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

        // Получаем accessToken из заголовка Authorization
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
