package com.github.hoangducmanh.smart_task_management.infrastructure.security.service;

import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.hoangducmanh.smart_task_management.application.auth.port.out.token.AccessTokenGeneratorPort;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService implements AccessTokenGeneratorPort{

    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    @Value("${JWT_EXPIRATION_MS}")
    private long expirationMs;
    
    @Override
    public String generateAccessToken(UserId userId, UserRole role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.value().toString())
                .claim("role", role.name())
                .issuedAt(java.util.Date.from(now))
                .expiration(java.util.Date.from(now.plusMillis(expirationMs)))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), Jwts.SIG.HS256)
                .compact();
    }
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractUserRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
    }
    }

}