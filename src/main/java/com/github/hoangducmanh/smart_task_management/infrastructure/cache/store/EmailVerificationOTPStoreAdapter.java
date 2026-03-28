package com.github.hoangducmanh.smart_task_management.infrastructure.cache.store;


import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.store.StoredEmailOTP;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.otp.EmailVerificationOTPStore;

public class EmailVerificationOTPStoreAdapter implements EmailVerificationOTPStore {
    
    private final StringRedisTemplate redisTemplate;
    private final String keyPrefix = "email_verification:";
    private final Duration TTL = Duration.ofMinutes(5);

    public EmailVerificationOTPStoreAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @SuppressWarnings("null")
    @Override
    public void saveOrReplace(StoredEmailOTP otp) {
        UUID userId = otp.userId();
        String email = otp.email();
        String hashedOtp = otp.hashedOTP();
        String key = keyPrefix + userId.toString();
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        hashOps.put(key, "email", email);
        hashOps.put(key, "hashedOTP", hashedOtp);
        redisTemplate.expire(key, TTL);

    }

    @Override
    public Optional<StoredEmailOTP> consumeIfMatches(UUID userId, String hashedToken) {
        String key = keyPrefix + userId.toString();
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        Map<String, String> entries = hashOps.entries(key);
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }
        String email = hashOps.get(key, "email");
        String storedHashedOtp = hashOps.get(key, "hashedOTP");

        if (storedHashedOtp != null && storedHashedOtp.equals(hashedToken)) {
            redisTemplate.delete(key);
            return Optional.of(new StoredEmailOTP(userId, email, hashedToken));
        }

        return Optional.empty();
    }

}
