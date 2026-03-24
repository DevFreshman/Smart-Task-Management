package com.github.hoangducmanh.smart_task_management.application.auth.port.out.otp;

import java.util.Optional;
import java.util.UUID;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.StoredEmailOTP;

// This repository is responsible for storing email verification tokens. It should store the token in a hashed form for security reasons.
public interface EmailVerificationOTPStore {
    void saveOrReplace(StoredEmailOTP token);
    Optional<StoredEmailOTP> consumeIfMatches(UUID userId, String hashedToken);
}
