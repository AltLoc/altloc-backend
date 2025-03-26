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
    protected void doFilterInternal(@SuppressWarnings("null") HttpServletRequest request,
            @SuppressWarnings("null") HttpServletResponse response, @SuppressWarnings("null") FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = null;
        String accessTokenFromCookie = null;

        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    accessTokenFromCookie = cookie.getValue();
                    break;
                }
            }
        }

        if (accessTokenFromCookie != null) {
            jwt = accessTokenFromCookie;
            System.out.println("JWT get from cookie: " + jwt);
        } else {
            String headerAuth = request.getHeader("Authorization");
            if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
                jwt = headerAuth.substring(7);
                System.out.println("JWT get from header: " + jwt);
            }
        }

        if (jwt != null && jwtCore.validateToken(jwt)) {
            String email = jwtCore.getUsernameFromToken(jwt);
            System.out.println("Username from JWT: " + email);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } else {

            String refreshTokenHeader = request.getHeader("Refresh-Token");
            if (refreshTokenHeader != null) {
                String refreshToken = refreshTokenHeader;
                System.out.println("Refresh Token get from header: " + refreshToken);
                if (jwtCore.validateRefreshToken(refreshToken)) {
                    String email = jwtCore.getUsernameFromToken(refreshToken);
                    System.out.println("Username from Refresh Token: " + email);

                    if (email != null) {
                        String newAccessToken = jwtCore.generateAccessToken(
                                SecurityContextHolder.getContext().getAuthentication());
                        response.setHeader("Authorization", "Bearer " + newAccessToken);

                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
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

        filterChain.doFilter(request, response);
    }
}
