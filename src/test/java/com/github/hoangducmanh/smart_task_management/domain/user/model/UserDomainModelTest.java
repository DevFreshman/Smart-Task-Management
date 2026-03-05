package com.github.hoangducmanh.smart_task_management.domain.user.model;

import com.github.hoangducmanh.smart_task_management.domain.user.exception.InvalidEmailException;
import com.github.hoangducmanh.smart_task_management.domain.user.exception.InvalidEmailStatusTransitionException;
import com.github.hoangducmanh.smart_task_management.domain.user.exception.InvalidRoleException;
import com.github.hoangducmanh.smart_task_management.domain.user.exception.InvalidUserNameException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserDomainModelTest {

    // OV: Email value object normalize + extract domain.
    @Test
    void email_shouldNormalizeAndGetDomain() {
        Email email = Email.of("  John.Doe+tag@Example.COM ");

        assertEquals("john.doe+tag@example.com", email.value());
        assertEquals("example.com", email.getDomain());
    }

    // OV: Email value object validates format and blank input.
    @Test
    void email_shouldThrowWhenFormatIsInvalid() {
        assertThrows(InvalidEmailException.class, () -> Email.of("not-an-email"));
        assertThrows(InvalidEmailException.class, () -> Email.of("   "));
    }

    // BR: EmailStatus transition rules for valid flow.
    @Test
    void emailStatus_shouldSupportValidTransitions() {
        assertEquals(EmailStatus.PENDING_VERIFICATION, EmailStatus.UNVERIFIED.requestVerification());
        assertEquals(EmailStatus.VERIFIED, EmailStatus.PENDING_VERIFICATION.verify());
    }

    // BR: EmailStatus blocks invalid transitions.
    @Test
    void emailStatus_shouldThrowOnInvalidTransitions() {
        assertThrows(InvalidEmailStatusTransitionException.class, () -> EmailStatus.UNVERIFIED.verify());
        assertThrows(InvalidEmailStatusTransitionException.class, () -> EmailStatus.VERIFIED.requestVerification());
        assertThrows(InvalidEmailStatusTransitionException.class, () -> EmailStatus.VERIFIED.verify());
        assertThrows(InvalidEmailStatusTransitionException.class, () -> EmailStatus.PENDING_VERIFICATION.requestVerification());
    }

    // OV: EmailStatus parsing from persisted/display text.
    @Test
    void emailStatus_shouldParseFromStatusIgnoringCase() {
        assertEquals(EmailStatus.UNVERIFIED, EmailStatus.fromStatus("UNVERIFIED"));
        assertEquals(EmailStatus.PENDING_VERIFICATION, EmailStatus.fromStatus("pending_verification"));
        assertThrows(IllegalArgumentException.class, () -> EmailStatus.fromStatus("unknown"));
    }

    // OV: Role parsing from role name.
    @Test
    void role_shouldParseFromRoleNameIgnoringCase() {
        assertEquals(Role.ADMIN, Role.fromRoleName("ADMIN"));
        assertEquals(Role.USER, Role.fromRoleName("user"));
        assertThrows(IllegalArgumentException.class, () -> Role.fromRoleName("manager"));
    }

    // OV: HashedPassword trims incoming value.
    @Test
    void hashedPassword_shouldTrimValue() {
        HashedPassword hashedPassword = HashedPassword.of("  abc123-hash  ");

        assertEquals("abc123-hash", hashedPassword.value());
    }

    // OV: HashedPassword rejects null/blank.
    @Test
    void hashedPassword_shouldThrowWhenNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> HashedPassword.of(null));
        assertThrows(IllegalArgumentException.class, () -> HashedPassword.of("   "));
    }

    // OV: UserId conversion from/to string representation.
    @Test
    void userId_shouldCreateFromStringAndConvertBack() {
        String idText = "c8f66fd8-55f8-4381-82de-e112b16c7d8e";

        UserId userId = UserId.fromString(idText);

        assertEquals(idText, userId.asString());
        assertEquals("UserId=[" + idText + "]", userId.toString());
    }

    // OV: UserId constructor/factory validation errors.
    @Test
    void userId_shouldThrowWhenInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new UserId(null));
        assertThrows(IllegalArgumentException.class, () -> new UserId(new UUID(0, 0)));
        assertThrows(IllegalArgumentException.class, () -> UserId.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> UserId.fromString("   "));
        assertThrows(IllegalArgumentException.class, () -> UserId.fromString("invalid-uuid"));
    }

    // OV: UserId generation should always be non-null/non-empty.
    @Test
    void userId_generate_shouldReturnNonNullAndNonEmpty() {
        UserId generated = UserId.generate();

        assertNotNull(generated);
        assertNotEquals(new UUID(0, 0), generated.value());
    }

    // BR: Register user applies default business state.
    @Test
    void user_register_shouldSetDefaults() {
        Instant registeredAt = Instant.parse("2026-01-10T08:30:00Z");
        User user = User.register(
            UserId.fromString("ab0f7e4d-3b14-44ae-9dd8-15f5673281ad"),
            Email.of("user@example.com"),
            "Ana",
            HashedPassword.of("hash-value"),
            registeredAt
        );

        assertEquals(EmailStatus.UNVERIFIED, user.getEmailStatus());
        assertEquals(Role.USER, user.getRole());
        assertEquals(registeredAt, user.getAuditInfo().createdAt());
        assertEquals(registeredAt, user.getAuditInfo().updatedAt());
    }

    // BR: Changing email resets verification status and updates audit.
    @Test
    void user_shouldChangeEmailAndResetStatusToUnverified() {
        Instant registeredAt = Instant.parse("2026-01-10T08:30:00Z");
        User user = createUser(registeredAt);
        user.requestEmailVerification(registeredAt.plus(1, ChronoUnit.MINUTES));
        assertEquals(EmailStatus.PENDING_VERIFICATION, user.getEmailStatus());

        Instant changedAt = registeredAt.plus(2, ChronoUnit.MINUTES);
        user.changeEmail(Email.of("new@example.com"), changedAt);

        assertEquals("new@example.com", user.getEmail().value());
        assertEquals(EmailStatus.UNVERIFIED, user.getEmailStatus());
        assertEquals(changedAt, user.getAuditInfo().updatedAt());
    }

    // BR: Domain forbids changing to the same email.
    @Test
    void user_shouldThrowWhenChangingToSameEmail() {
        Instant registeredAt = Instant.parse("2026-01-10T08:30:00Z");
        User user = createUser(registeredAt);

        assertThrows(
            InvalidEmailException.class,
            () -> user.changeEmail(Email.of("user@example.com"), registeredAt.plus(1, ChronoUnit.MINUTES))
        );
    }

    // BR: Verification must follow request -> verify flow.
    @Test
    void user_shouldRequestVerificationThenVerify() {
        Instant registeredAt = Instant.parse("2026-01-10T08:30:00Z");
        User user = createUser(registeredAt);

        user.requestEmailVerification(registeredAt.plus(1, ChronoUnit.MINUTES));
        assertEquals(EmailStatus.PENDING_VERIFICATION, user.getEmailStatus());

        user.markEmailAsVerified(registeredAt.plus(2, ChronoUnit.MINUTES));
        assertEquals(EmailStatus.VERIFIED, user.getEmailStatus());
        assertEquals(registeredAt.plus(2, ChronoUnit.MINUTES), user.getAuditInfo().updatedAt());
    }

    // BR: User name change trims value and updates audit.
    @Test
    void user_shouldChangeNameAndTrim() {
        Instant registeredAt = Instant.parse("2026-01-10T08:30:00Z");
        User user = createUser(registeredAt);

        user.changeName("  Nguyen Van A  ", registeredAt.plus(1, ChronoUnit.MINUTES));

        assertEquals("Nguyen Van A", user.getName());
        assertEquals(registeredAt.plus(1, ChronoUnit.MINUTES), user.getAuditInfo().updatedAt());
    }

    // BR: Blank name is rejected by domain rule.
    @Test
    void user_shouldThrowWhenNameIsBlank() {
        Instant registeredAt = Instant.parse("2026-01-10T08:30:00Z");
        User user = createUser(registeredAt);

        assertThrows(
            InvalidUserNameException.class,
            () -> user.changeName("   ", registeredAt.plus(1, ChronoUnit.MINUTES))
        );
    }

    // BR: Role change works once and blocks same-role update.
    @Test
    void user_shouldChangeRoleAndThrowWhenRoleIsSame() {
        Instant registeredAt = Instant.parse("2026-01-10T08:30:00Z");
        User user = createUser(registeredAt);

        user.changeRole(Role.ADMIN, registeredAt.plus(1, ChronoUnit.MINUTES));
        assertEquals(Role.ADMIN, user.getRole());

        assertThrows(
            InvalidRoleException.class,
            () -> user.changeRole(Role.ADMIN, registeredAt.plus(2, ChronoUnit.MINUTES))
        );
    }

    private User createUser(Instant registeredAt) {
        return User.register(
            UserId.fromString("ab0f7e4d-3b14-44ae-9dd8-15f5673281ad"),
            Email.of("user@example.com"),
            "Ana",
            HashedPassword.of("hash-value"),
            registeredAt
        );
    }
}
