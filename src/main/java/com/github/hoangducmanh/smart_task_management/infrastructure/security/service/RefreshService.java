package com.github.hoangducmanh.smart_task_management.infrastructure.security.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.github.hoangducmanh.smart_task_management.application.auth.port.out.token.RefreshTokenGeneratorPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.token.RefreshTokenHashPort;

@Service
public class RefreshService implements RefreshTokenGeneratorPort, RefreshTokenHashPort{
    private static final int TOKEN_BYTE_SIZE = 64;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = java.util.Base64.getUrlEncoder().withoutPadding();
    @Override
    public String generateRefreshToken() {
        byte[] randomBytes = new byte[TOKEN_BYTE_SIZE];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    @Override
    public String hash(String token) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
        byte[] hashedBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
        return base64Encoder.encodeToString(hashedBytes);
    }
}
