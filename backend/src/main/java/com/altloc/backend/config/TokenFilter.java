package com.altloc.backend.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.altloc.backend.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtCore;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @SuppressWarnings("null") HttpServletRequest request,
            @SuppressWarnings("null") HttpServletResponse response,
            @SuppressWarnings("null") FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = null;
        String email = null;
        UserDetails userDetails = null;
        String refreshToken = null;
        String headerAuth = request.getHeader("Authorization");

        try {
            // Извлечение JWT токена из заголовка
            if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
                jwt = headerAuth.substring(7);
                System.out.println("JWT get from header: " + jwt);
            }

            // Проверка Access Token
            if (jwt != null && jwtCore.validateToken(jwt)) {
                email = jwtCore.getUsernameFromToken(jwt);
                System.out.println("Username from JWT: " + email);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    userDetails = userDetailsService.loadUserByUsername(email);

                    // Создание объекта Authentication для доступа в контексте
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } else {
                // Если Access Token не валиден, проверяем Refresh Token
                String refreshTokenHeader = request.getHeader("Refresh-Token");
                if (refreshTokenHeader != null) {
                    refreshToken = refreshTokenHeader;
                    System.out.println("Refresh Token get from header: " + refreshToken);
                    if (jwtCore.validateRefreshToken(refreshToken)) {
                        email = jwtCore.getUsernameFromToken(refreshToken);
                        System.out.println("Username from Refresh Token: " + email);

                        if (email != null) {
                            String newAccessToken = jwtCore.generateToken(
                                    SecurityContextHolder.getContext().getAuthentication());
                            response.setHeader("Authorization", "Bearer " + newAccessToken);

                            userDetails = userDetailsService.loadUserByUsername(email);
                            if (userDetails != null) {
                                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                                SecurityContextHolder.getContext().setAuthentication(auth);
                            }
                        }
                    } else {
                        System.out.println("Refresh Token is invalid");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in JWT processing: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}
