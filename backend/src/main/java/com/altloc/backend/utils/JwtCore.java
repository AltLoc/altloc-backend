package com.altloc.backend.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import com.altloc.backend.store.entity.RefreshTokenEntity;
import com.altloc.backend.store.repository.RefreshTokenRepository;
import com.altloc.backend.store.repository.UserRepository;
import com.altloc.backend.model.UserDetailsImpl;

import java.security.Key;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtCore {

    @Value("${backend.jwt.secret}")
    private String secret;

    @Value("${backend.jwt.expiration}")
    private long expiration;

    @Value("${backend.jwt.refreshExpiration}")
    private long refreshExpiration;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public JwtCore(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(userDetails.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        String refreshToken = UUID.randomUUID().toString();

        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()

                .user(userRepository.findByEmail(((UserDetailsImpl) authentication.getPrincipal()).getEmail())
                        .orElseThrow(() -> new RuntimeException("User not found")))
                .refreshToken(refreshToken)
                .createdAt(new Date())
                .expirationDate(new Date(System.currentTimeMillis() + refreshExpiration))
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return refreshToken;
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
            return false;
        }
    }

    // public boolean validateRefreshToken(String refreshToken) {
    // return refreshTokenRepository.findByRefreshToken(refreshToken).isPresent();
    // }

    public boolean validateRefreshToken(String refreshToken) {
        // Проверяем, существует ли Refresh Token в базе данных
        Optional<RefreshTokenEntity> storedToken = refreshTokenRepository.findByRefreshToken(refreshToken);

        System.out.println("Stored token: " + storedToken.toString());

        // Если токен не найден в базе, возвращаем false
        if (!storedToken.isPresent()) {
            System.out.println("Token not found in DB");
            return false;
        }

        // Проверяем, не истек ли срок действия Refresh Token
        RefreshTokenEntity token = storedToken.get();
        if (token.getExpirationDate().before(new Date())) {
            System.out.println("Token expired");
            return false;
        }

        // Теперь можем дополнительно проверять подпись токена, если требуется
        // (например, через декодирование)
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey()) // Ваш секретный ключ для подписи токенов
                    .build()
                    .parseClaimsJws(refreshToken); // Если подпись не верна, будет выброшено исключение
            return true; // Если подпись верна и срок действия не истек
        } catch (Exception e) {
            return false; // Если подпись неверна или токен поврежден
        }
    }

    public void deleteRefreshToken(String refreshToken) {
        refreshTokenRepository.findByRefreshToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }
}
