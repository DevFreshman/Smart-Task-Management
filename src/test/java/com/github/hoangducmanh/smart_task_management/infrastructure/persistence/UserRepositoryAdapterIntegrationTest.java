package com.github.hoangducmanh.smart_task_management.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Objects;
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

import com.github.hoangducmanh.smart_task_management.bootstrap.SmartTaskManagementApplication;
import com.github.hoangducmanh.smart_task_management.domain.shared.AuditInfo;
import com.github.hoangducmanh.smart_task_management.domain.user.model.Email;
import com.github.hoangducmanh.smart_task_management.domain.user.model.EmailStatus;
import com.github.hoangducmanh.smart_task_management.domain.user.model.HashedPassword;
import com.github.hoangducmanh.smart_task_management.domain.user.model.User;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserRole;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.user.UserEntity;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.user.UserJpaRepository;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.user.UserPersistenceMapper;
import com.github.hoangducmanh.smart_task_management.infrastructure.persistence.user.UserRepositoryAdapter;



@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    UserRepositoryAdapter.class,
    UserPersistenceMapper.class
})
@ContextConfiguration(classes = SmartTaskManagementApplication.class)
class UserRepositoryAdapterIntegrationTest extends AbstractPostgresContainerTest {
    
    @Autowired
    private UserRepositoryAdapter adapter;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private UserPersistenceMapper userPersistenceMapper;

    private static final Instant NOW = Instant.parse("2024-01-01T00:00:00Z");
    
    @BeforeEach
    void setUp() {
        userJpaRepository.deleteAll();
    }

    private User createTestUser(UUID userId, String email) {
        return User.reconstitute(
            UserId.of(userId),
            Email.of(email),
            EmailStatus.UNVERIFIED,
            HashedPassword.of("hashedPassword123"),
            AuditInfo.of(NOW, NOW, null),
            "Test User",
            UserRole.USER
        );
    }
    
    private UserEntity createTestUserEntity(UUID userId, String email, Instant deletedAt) {
        return UserEntity.builder()
            .userId(userId)
            .email(email)
            .emailStatus("UNVERIFIED")
            .hashedPassword("hashedPassword123")
            .name("Test User")
            .userRole("USER")
            .createdAt(NOW)
            .updatedAt(NOW)
            .deletedAt(deletedAt)
            .build();
    }

    @Test
    @DisplayName("should map between User domain model and UserEntity correctly")
    void sholdMapBetweenDomainAndEntityCorrectly() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId, "test@example.com");
        // When
        UserEntity entity = userPersistenceMapper.toEntity(user);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getEmail()).isEqualTo("test@example.com");
        assertThat(entity.getName()).isEqualTo("Test User");
        assertThat(entity.getUserRole()).isEqualTo("USER");
        assertThat(entity.getHashedPassword()).isEqualTo("hashedPassword123");
        assertThat(entity.getCreatedAt()).isEqualTo(NOW);
        assertThat(entity.getUpdatedAt()).isEqualTo(NOW);
    }


    @Nested
    @DisplayName("findById method")
    class FindByIdTest {
        
        @Test
        @DisplayName("should return user when userId exists and not deleted")
        void shouldReturnUserWhenUserIdExistsAndNotDeleted() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity entity = createTestUserEntity(userId, "test@example.com", null);
            userJpaRepository.saveAndFlush(entity);

            // When
            Optional<User> result = adapter.findById(UserId.of(userId));

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId().value()).isEqualTo(userId);
            assertThat(result.get().getEmail().value()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("should return empty when userId does not exist")
        void shouldReturnEmptyWhenUserIdDoesNotExist() {
            // Given
            UUID nonExistentUserId = UUID.randomUUID();

            // When
            Optional<User> result = adapter.findById(UserId.of(nonExistentUserId));

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty when user is soft deleted")
        void shouldReturnEmptyWhenUserIsSoftDeleted() {
            // Given
            UUID userId = UUID.randomUUID();
            Instant deletedAt = NOW.plusSeconds(3600);
            UserEntity entity = createTestUserEntity(userId, "deleted@example.com", deletedAt);
            userJpaRepository.saveAndFlush(entity);

            // When
            Optional<User> result = adapter.findById(UserId.of(userId));

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEmail method")
    class FindByEmailTest {
        
        @Test
        @DisplayName("should return user when email exists and not deleted")
        void shouldReturnUserWhenEmailExistsAndNotDeleted() {
            // Given
            UUID userId = UUID.randomUUID();
            String email = "existing@example.com";
            UserEntity entity = createTestUserEntity(userId, email, null);
            userJpaRepository.saveAndFlush(entity);

            // When
            Optional<User> result = adapter.findByEmail(Email.of(email));

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail().value()).isEqualTo(email);
            assertThat(result.get().getId().value()).isEqualTo(userId);
        }

        @Test
        @DisplayName("should return empty when email does not exist")
        void shouldReturnEmptyWhenEmailDoesNotExist() {
            // Given
            String nonExistentEmail = "nonexistent@example.com";

            // When
            Optional<User> result = adapter.findByEmail(Email.of(nonExistentEmail));

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty when user is soft deleted")
        void shouldReturnEmptyWhenUserIsSoftDeleted() {
            // Given
            UUID userId = UUID.randomUUID();
            String email = "softdeleted@example.com";
            Instant deletedAt = NOW.plusSeconds(3600);
            UserEntity entity = createTestUserEntity(userId, email, deletedAt);
            userJpaRepository.saveAndFlush(entity);

            // When
            Optional<User> result = adapter.findByEmail(Email.of(email));

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("save method")
    class SaveTest {
        
        @Test
        @DisplayName("should save and return user")
        void shouldSaveAndReturnUser() {
            // Given
            UUID userId = UUID.randomUUID();
            User user = createTestUser(userId, "newuser@example.com");

            // When
            User savedUser = adapter.save(user);

            // Then
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getId().value()).isEqualTo(userId);
            assertThat(savedUser.getEmail().value()).isEqualTo("newuser@example.com");
            assertThat(savedUser.getName()).isEqualTo("Test User");
            assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);

            // Verify persistence
            Optional<UserEntity> persistedEntity = userJpaRepository.findById(userId);
            assertThat(persistedEntity).isPresent();
            assertThat(persistedEntity.get().getEmail()).isEqualTo("newuser@example.com");
        }
    }

    @Nested
    @DisplayName("existsByEmail method")
    class ExistsByEmailTest {
        
        @Test
        @DisplayName("should return true when email exists and not deleted")
        void shouldReturnTrueWhenEmailExistsAndNotDeleted() {
            // Given
            UUID userId = UUID.randomUUID();
            String email = "exists@example.com";
            UserEntity entity = createTestUserEntity(userId, email, null);
            userJpaRepository.saveAndFlush(Objects.requireNonNull(entity));

            // When
            boolean result = adapter.existsByEmail(Email.of(email));

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when email does not exist")
        void shouldReturnFalseWhenEmailDoesNotExist() {
            // Given
            String nonExistentEmail = "doesnotexist@example.com";

            // When
            boolean result = adapter.existsByEmail(Email.of(nonExistentEmail));

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when user is soft deleted")
        void shouldReturnFalseWhenUserIsSoftDeleted() {
            // Given
            UUID userId = UUID.randomUUID();
            String email = "deleteduser@example.com";
            Instant deletedAt = NOW.plusSeconds(3600);
            UserEntity entity = createTestUserEntity(userId, email, deletedAt);
            userJpaRepository.saveAndFlush(Objects.requireNonNull(entity));

            // When
            boolean result = adapter.existsByEmail(Email.of(email));

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("existsById method")
    class ExistsByIdTest {
        
        @Test
        @DisplayName("should return true when userId exists and not deleted")
        void shouldReturnTrueWhenUserIdExistsAndNotDeleted() {
            // Given
            UUID userId = UUID.randomUUID();
            UserEntity entity = createTestUserEntity(userId, "activeuser@example.com", null);
            userJpaRepository.saveAndFlush(entity);

            // When
            boolean result = adapter.existsById(UserId.of(userId));

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when userId does not exist")
        void shouldReturnFalseWhenUserIdDoesNotExist() {
            // Given
            UUID nonExistentUserId = UUID.randomUUID();

            // When
            boolean result = adapter.existsById(UserId.of(nonExistentUserId));

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when user is soft deleted")
        void shouldReturnFalseWhenUserIsSoftDeleted() {
            // Given
            UUID userId = UUID.randomUUID();
            Instant deletedAt = NOW.plusSeconds(3600);
            UserEntity entity = createTestUserEntity(userId, "softdeleteduser@example.com", deletedAt);
            userJpaRepository.saveAndFlush(entity);

            // When
            boolean result = adapter.existsById(UserId.of(userId));

            // Then
            assertThat(result).isFalse();
        }
    }
}
