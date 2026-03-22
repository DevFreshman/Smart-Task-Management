package com.github.hoangducmanh.smart_task_management.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.StoredRefreshToken;
import com.github.hoangducmanh.smart_task_management.bootstrap.SmartTaskManagementApplication;
import com.github.hoangducmanh.smart_task_management.domain.user.model.EmailStatus;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserRole;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.refresh.RefreshTokenAdapter;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.refresh.RefreshTokenEntity;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.refresh.RefreshTokenJpaRepository;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.refresh.RefreshTokenJpaRepositoryImpl;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.user.UserEntity;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.user.UserJpaRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


@DataJpaTest(
    properties = { "REFRESH_TOKEN_EXPIRATION_MS=604800000"}
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    RefreshTokenJpaRepositoryImpl.class,
    RefreshTokenAdapter.class,
})
@ContextConfiguration(classes = SmartTaskManagementApplication.class)
class RefreshRepositoryAdapterIntegrationTest extends AbstractPostgresContainerTest {


    @Autowired
    private RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Autowired
    private  RefreshTokenAdapter refreshTokenAdapter;

    @Autowired
    private UserJpaRepository userJpaRepository;

    private static final Instant NOW = Instant.parse("2024-01-01T00:00:00Z");

    @PersistenceContext
    private EntityManager entityManager;

    private StoredRefreshToken createTestStoredRefreshToken(UUID userId, String hashToken) {
        return new StoredRefreshToken(hashToken, userId);
    }
    @BeforeEach
    void setUp() {
        refreshTokenJpaRepository.deleteAll();
    }

    private UserEntity createTestUserEntity(UUID userId) {
        return UserEntity.builder()
                .userId(userId)
                .name("testuser")
                .email("testuser@example.com")
                .emailStatus(EmailStatus.UNVERIFIED.name())
                .userRole(UserRole.USER.name())
                .hashedPassword("hashedpassword")
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();

    }

    
    @Nested
    @DisplayName("consumeAndGetUserId method")
    class ConsumeAndGetUserIdTest {

        @Test
        @DisplayName("should return userId when hashToken exists and not expired, and delete the token")
        void shouldReturnUserIdWhenHashTokenExistsAndNotExpired() {
            // Given
                // Save User
                UUID userId = UUID.randomUUID();
                UserEntity userEntity = createTestUserEntity(userId);
                userJpaRepository.saveAndFlush(userEntity);

                // Save a valid refresh token for the user
                // Note: We use the adapter to save the token to ensure it is properly set up with expiration time and Entity can not be directly created
                refreshTokenAdapter.save(createTestStoredRefreshToken(userId, "validHashToken"), NOW);
                refreshTokenJpaRepository.flush();
            // When
                Optional<UUID> result = refreshTokenAdapter.consumeAndGetUserId("validHashToken", NOW.plusSeconds(300));
            // Then
                assertThat(result).isPresent();
                assertThat(result.get()).isEqualTo(userId);

                entityManager.clear(); // Clear persistence context to ensure we read fresh data from the database  
                // Verify token is revoked
                Optional<RefreshTokenEntity> tokenEntityOpt = refreshTokenJpaRepository.findById("validHashToken");
                assertThat(tokenEntityOpt).isPresent();
                RefreshTokenEntity tokenEntity = tokenEntityOpt.get();
                assertThat(tokenEntity.getRevokedAt()).isNotNull();
        }

        @Test
        @DisplayName("should return empty when hashToken does not exist")
        void shouldReturnEmptyWhenHashTokenDoesNotExist() {
            // When
            Optional<UUID> result = refreshTokenAdapter.consumeAndGetUserId("nonExistingHashToken", NOW);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty when token is already revoked")
        void shouldReturnEmptyWhenTokenIsRevoked() {
            // Given
            // Save User 
            UUID userId = UUID.randomUUID();
            UserEntity userEntity = createTestUserEntity(userId);
            userJpaRepository.saveAndFlush(userEntity);

            refreshTokenAdapter.save(createTestStoredRefreshToken(userId, "revokedHashToken"), NOW);
            refreshTokenAdapter.consumeAndGetUserId("revokedHashToken", NOW);

            // When
            Optional<UUID> result = refreshTokenAdapter.consumeAndGetUserId("revokedHashToken", NOW);

            // Then
            assertThat(result).isEmpty();
 
        }
    }

    @Nested
    @DisplayName("save method")
    class SaveTest {
        @Test
        @DisplayName("should save active refresh token successfully")
        void shouldSaveActiveRefreshTokenSuccessfully() {
            // Given
            UUID userId = UUID.randomUUID();
            // When
            refreshTokenAdapter.save(createTestStoredRefreshToken(userId, "activeHashToken"), NOW);
            // Then
            Optional<RefreshTokenEntity> tokenOpt = refreshTokenJpaRepository.findById("activeHashToken");
            assertThat(tokenOpt).isPresent();
        }
    }

    @Nested
    @DisplayName("revokeByUserId method")
    class RevokeByUserIdTest {

        @Test
        @DisplayName("should revoke all tokens of the user")
        void shouldRevokeAllTokensOfTheUser() {
            // Given
            // Save User
            UUID userId = UUID.randomUUID();
            UserEntity userEntity = createTestUserEntity(userId);
            userJpaRepository.saveAndFlush(userEntity);

            refreshTokenAdapter.save(createTestStoredRefreshToken(userId, "hashToken1"), NOW.plusMillis(600));
            refreshTokenAdapter.save(createTestStoredRefreshToken(userId, "hashToken2"), NOW.plusMillis(1200));
            refreshTokenJpaRepository.flush();
            // When
            refreshTokenAdapter.revokeByUserId(userId, NOW.plusSeconds(3600));

            // Clear persistence context to ensure we read fresh data from the database
            entityManager.clear();
            // Then
            Optional<RefreshTokenEntity> token1Opt = refreshTokenJpaRepository.findById("hashToken1");
            Optional<RefreshTokenEntity> token2Opt = refreshTokenJpaRepository.findById("hashToken2");

            assertThat(token1Opt).isPresent();
            assertThat(token1Opt.get().getRevokedAt()).isNotNull();
            assertThat(token2Opt).isPresent();
            assertThat(token2Opt.get().getRevokedAt()).isNotNull();
        }

        @Test
        @DisplayName("should not change already revoked tokens")
        void shouldNotChangeAlreadyRevokedTokens() {
            // Given

            // Save User
            UUID userId = UUID.randomUUID();
            UserEntity userEntity = createTestUserEntity(userId);
            userJpaRepository.saveAndFlush(userEntity);

            refreshTokenAdapter.save(createTestStoredRefreshToken(userId, "hashToken1"), NOW);
            refreshTokenJpaRepository.flush();
            Instant firstRevoke = NOW.plusSeconds(3600);
            refreshTokenAdapter.revokeByUserId(userId, firstRevoke);
            refreshTokenAdapter.save(createTestStoredRefreshToken(userId, "hashToken2"), NOW);
            refreshTokenJpaRepository.flush();
            Instant secondRevoke = NOW.plusSeconds(7200);
            refreshTokenAdapter.revokeByUserId(userId, secondRevoke);
            // When
            refreshTokenAdapter.revokeByUserId(userId, NOW.plusSeconds(8100));

            // Clear persistence context to ensure we read fresh data from the database
            entityManager.clear();
            // Then
            Optional<RefreshTokenEntity> token1Opt = refreshTokenJpaRepository.findById("hashToken1");
            Optional<RefreshTokenEntity> token2Opt = refreshTokenJpaRepository.findById("hashToken2");

            assertThat(token1Opt).isPresent();
            assertThat(token1Opt.get().getRevokedAt()).isNotNull().isEqualTo(firstRevoke);
            assertThat(token2Opt).isPresent();
            assertThat(token2Opt.get().getRevokedAt()).isNotNull().isEqualTo(secondRevoke);
        }

        @Test
        @DisplayName("should do nothing if token expires before revocation time")
        void shouldDoNothingIfTokenExpiresBeforeRevocationTime() {
            // Given
            // Save User
            UUID userId = UUID.randomUUID();
            UserEntity userEntity = createTestUserEntity(userId);
            userJpaRepository.saveAndFlush(userEntity);

            refreshTokenAdapter.save(createTestStoredRefreshToken(userId, "hashToken1"), NOW.minusMillis(604800000));

            // When
            // plus 100ms to make sure the token is expired before revocation time
            refreshTokenAdapter.revokeByUserId(userId, NOW.plusMillis(100));

            // Then
            Optional<RefreshTokenEntity> token1Opt = refreshTokenJpaRepository.findById("hashToken1");
            assertThat(token1Opt).isPresent();
            assertThat(token1Opt.get().getRevokedAt()).isNull();
        }
    }
}
