package com.github.hoangducmanh.smart_task_management.application.auth.usecase;

import com.github.hoangducmanh.smart_task_management.application.ClockSystem;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.RegisterCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.RegisterResult;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.EmailAlreadyExistsException;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.PasswordHashPort;
import com.github.hoangducmanh.smart_task_management.domain.user.exception.InvalidEmailException;
import com.github.hoangducmanh.smart_task_management.domain.user.model.Email;
import com.github.hoangducmanh.smart_task_management.domain.user.model.EmailStatus;
import com.github.hoangducmanh.smart_task_management.domain.user.model.HashedPassword;
import com.github.hoangducmanh.smart_task_management.domain.user.model.Role;
import com.github.hoangducmanh.smart_task_management.domain.user.model.User;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import com.github.hoangducmanh.smart_task_management.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RegisterUseCaseTest {

    private UserRepository userRepository;
    private PasswordHashPort passwordHashPort;
    private ClockSystem clockSystem;
    private RegisterUseCase registerUseCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordHashPort = mock(PasswordHashPort.class);
        clockSystem = mock(ClockSystem.class);
        registerUseCase = new RegisterUseCase(userRepository, passwordHashPort, clockSystem);
    }

    // Email có khoảng trắng + chữ hoa để verify normalize. Dùng ArgumentCaptor để assert giá trị thực truyền vào mock.
    // Assert toàn bộ state User: email normalized, password hashed, EmailStatus.UNVERIFIED, Role.USER, createdAt == updatedAt == now.
    @Test
    void execute_shouldRegisterUserAndReturnResult() {
        RegisterCommand command = RegisterCommand.of("  John.Doe@Example.COM ", "raw-password", "John Doe");
        Instant now = Instant.parse("2026-03-03T10:15:00Z");

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordHashPort.encode("raw-password")).thenReturn("hashed-password");
        when(clockSystem.now()).thenReturn(now);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResult result = registerUseCase.execute(command);

        ArgumentCaptor<Email> emailCaptor = ArgumentCaptor.forClass(Email.class);
        verify(userRepository).existsByEmail(emailCaptor.capture());
        assertEquals("john.doe@example.com", emailCaptor.getValue().asString());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("john.doe@example.com", savedUser.getEmail().asString());
        assertEquals("hashed-password", savedUser.getHashedPassword().value());
        assertEquals("John Doe", savedUser.getName());
        assertEquals(EmailStatus.UNVERIFIED, savedUser.getEmailStatus());
        assertEquals(Role.USER, savedUser.getRole());
        assertEquals(now, savedUser.getAuditInfo().createdAt());
        assertEquals(now, savedUser.getAuditInfo().updatedAt());

        assertNotNull(UUID.fromString(result.userId()));
        assertEquals("john.doe@example.com", result.email());
        assertEquals("John Doe", result.name());
        verify(passwordHashPort).encode("raw-password");
        verify(clockSystem).now();
    }

    // Verify throw đúng exception. Quan trọng hơn: verify side effects không xảy ra — không hash password, không gọi clock, không save user.
    @Test
    void execute_shouldThrowWhenEmailAlreadyExists() {
        RegisterCommand command = RegisterCommand.of("user@example.com", "raw-password", "John Doe");
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(
            EmailAlreadyExistsException.class,
            () -> registerUseCase.execute(command)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository).existsByEmail(Email.of("user@example.com"));
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordHashPort);
        verifyNoInteractions(clockSystem);
    }

    // Email invalid bị reject ngay tại Email.of() trong domain layer, trước khi chạm repository.
    // Exception là InvalidEmailException (domain), không phải AuthException (application).
    @Test
    void execute_shouldThrowWhenEmailIsInvalid() {
        RegisterCommand command = RegisterCommand.of("not-an-email", "raw-password", "John Doe");

        assertThrows(InvalidEmailException.class, () -> registerUseCase.execute(command));

        verify(userRepository, never()).existsByEmail(any(Email.class));
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordHashPort);
        verifyNoInteractions(clockSystem);
    }

    //Repository trả về persistedUser với data khác hoàn toàn. Verify RegisterResult được build từ savedUser, không phải local variables.
    @Test
    void execute_shouldUseSavedUserDataForResult() {
        RegisterCommand command = RegisterCommand.of("user@example.com", "raw-password", "John Doe");
        Instant now = Instant.parse("2026-03-03T10:15:00Z");
        User persistedUser = User.register(
            UserId.fromString("9d2d95c0-d0da-4e0e-90cc-228e5c4d4a95"),
            Email.of("persisted@example.com"),
            "Persisted Name",
            HashedPassword.of("persisted-hash"),
            now.plusSeconds(60)
        );

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordHashPort.encode("raw-password")).thenReturn("hashed-password");
        when(clockSystem.now()).thenReturn(now);
        when(userRepository.save(any(User.class))).thenReturn(persistedUser);

        RegisterResult result = registerUseCase.execute(command);

        assertEquals("9d2d95c0-d0da-4e0e-90cc-228e5c4d4a95", result.userId());
        assertEquals("persisted@example.com", result.email());
        assertEquals("Persisted Name", result.name());
    }
}
