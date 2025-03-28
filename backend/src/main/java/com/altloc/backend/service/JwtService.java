package com.altloc.backend.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import com.altloc.backend.store.entities.auth.RefreshTokenEntity;
import com.altloc.backend.store.repositories.RefreshTokenRepository;
import com.altloc.backend.model.UserDetailsImpl;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${backend.jwt.secret}")
    private String secretKey;

    @Value("${backend.jwt.expiration}")
    private long expiration;

    @Value("${backend.jwt.refreshExpiration}")
    private long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    // public JwtService(RefreshTokenRepository refreshTokenRepository,
    // UserRepository userRepository) {
    // this.refreshTokenRepository = refreshTokenRepository;
    // }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(userDetails.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(userDetails.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String refreshToken) {

        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(refreshToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Invalid JWT refreshToken: {}", e.getMessage());
            return false;
        }
    }

    public void deleteRefreshToken(String refreshToken) {
        Optional<RefreshTokenEntity> tokenOpt = refreshTokenRepository.findByRefreshToken(refreshToken);
        if (tokenOpt.isPresent()) {
            refreshTokenRepository.delete(tokenOpt.get());
            logger.info("Refresh token deleted successfully");
        } else {
            logger.warn("Attempted to delete non-existent refresh token");
        }
    }
}
