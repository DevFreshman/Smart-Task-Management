package com.github.hoangducmanh.smart_task_management.application.auth.port.out.token;

import java.util.Optional;
import java.util.UUID;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.StoredEmailToken;

// This repository is responsible for storing email verification tokens. It should store the token in a hashed form for security reasons.
public interface EmailVerificationTokenStore {
    void saveOrReplace(StoredEmailToken token);
    Optional<StoredEmailToken> consumeIfMatches(UUID userId, String hashedToken);
}
