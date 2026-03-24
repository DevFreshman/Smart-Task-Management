package com.github.hoangducmanh.smart_task_management.infrastructure.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.AutoConfigureDataRedis;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.StoredEmailOTP;
import com.github.hoangducmanh.smart_task_management.infrastructure.cache.store.EmailVerificationOTPStoreAdapter;

@DataRedisTest
@AutoConfigureDataRedis
@Import(EmailVerificationOTPStoreAdapter.class)
public class EmailOTPStoreAdapterIntegrationTest extends AbstractRedisContainerTest{
    
    @Autowired
    private StringRedisTemplate redisTemplate;

    private HashOperations<String, String, String> hashOps;

    @BeforeEach
    void setUp() {
        hashOps = redisTemplate.opsForHash();
    }

    @Autowired
    private EmailVerificationOTPStoreAdapter adapter;

    @Nested
    @DisplayName("saveOrReplace method")
    class SaveOrReplaceMethodTests {
        @Test
        @DisplayName("should save email OTP successfully")
        void shouldSaveEmailOTPSuccessfully() {
            // Given
            StoredEmailOTP otp = StoredEmailOTP.of(UUID.randomUUID(), 
            "test@example.com", 
            "hashed-sample-otp");

            // When
            adapter.saveOrReplace(otp);

            // Then
            String key = "email_verification:" + otp.userId().toString();
            String storedEmail = hashOps.get(key, "email");
            assertThat(otp.email()).isEqualTo(storedEmail);
            assertThat(otp.hashedOTP()).isEqualTo(hashOps.get(key, "hashedOTP"));
        }

        @Test
        @DisplayName("should replace existing OTP for the same user")
        void shouldReplaceExistingOTPForSameUser() {
            //Given
            StoredEmailOTP otp1 = StoredEmailOTP.of(UUID.randomUUID(), 
            "test@example.com", "hashed-sample-otp");
            StoredEmailOTP otp2 = StoredEmailOTP.of(otp1.userId(), 
            "new@example.com", "new-hashed-otp");

            //When
            adapter.saveOrReplace(otp1);
            adapter.saveOrReplace(otp2);

            //Then
            String key = "email_verification:" + otp1.userId().toString();
            assertThat(otp2.email()).isEqualTo(hashOps.get(key, "email"));
            assertThat(otp2.hashedOTP()).isEqualTo(hashOps.get(key, "hashedOTP"));
        }
    }

    @Nested
    @DisplayName("consumeIfMatch method")
    class ConsumeIfMatchMethodTests {
        // Suppressing null warnings for the test data setup, as we are directly manipulating the Redis store for testing purposes.(It is safe in this context)
        @Test
        @DisplayName("should consume OTP if it matches")
        @SuppressWarnings("null")
        void shouldConsumeOTPIfMatches() {
            // Given
            UUID userId = UUID.randomUUID();
            StoredEmailOTP otp = StoredEmailOTP.of(userId,
                "test@example.com", "hashed-sample-otp");
            String key = "email_verification:" + userId.toString();
            hashOps.put(key, "email", otp.email());    
            hashOps.put(key, "hashedOTP", otp.hashedOTP());
            
            // When
            var result = adapter.consumeIfMatches(userId, "hashed-sample-otp");
            // Then
            assertThat(result).isPresent();
            assertThat(otp.userId()).isEqualTo(result.get().userId());
            assertThat(otp.email()).isEqualTo(result.get().email());
            assertThat(otp.hashedOTP()).isEqualTo(result.get().hashedOTP());
        }

        @Test
        @DisplayName("should not consume OTP if it does not match")
        @SuppressWarnings("null")
        void shouldNotConsumeOTPIfNotMatches() {
            // Given
            UUID userId = UUID.randomUUID();
            StoredEmailOTP otp = StoredEmailOTP.of(userId,
                "test@example.com", "hashed-sample-otp");
            String key = "email_verification:" + userId.toString();
            hashOps.put(key, "email", otp.email());
            hashOps.put(key, "hashedOTP", otp.hashedOTP());

            // When
            var result = adapter.consumeIfMatches(userId, "wrong-hashed-otp");

            // Then
            assertThat(result).isNotPresent();
        }
        
        @Test
        @DisplayName("should not consume OTP if it does not exist")
        void shouldNotConsumeOTPIfNotExist() {
            // Given
            UUID userId = UUID.randomUUID();

            // When
            var result = adapter.consumeIfMatches(userId, "any-otp");

            // Then
            assertThat(result).isNotPresent();
        }
}
}