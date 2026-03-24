package com.github.hoangducmanh.smart_task_management.infrastructure.security.service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import com.github.hoangducmanh.smart_task_management.application.auth.port.out.otp.EmailOTPGeneratorPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.otp.EmailOTPHashPort;

public class EmailService implements EmailOTPGeneratorPort, EmailOTPHashPort {
    
    private final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String DIGITS = "0123456789";
    private final String ALL_CHARACTERS = UPPER + DIGITS;
    private final SecureRandom random = new SecureRandom();
    private final int OTP_LENGTH = 6;
    private final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    @Override
    public String generateEmailOTP() {
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            int index = random.nextInt(ALL_CHARACTERS.length());
            otp.append(ALL_CHARACTERS.charAt(index));
        }
        return otp.toString();
    }

    @Override
    public String hash(String token) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
        byte[] hashedBytes = digest.digest(token.getBytes());
        return base64Encoder.encodeToString(hashedBytes);
    }

}
