package com.github.hoangducmanh.smart_task_management.domain.user.model;

import com.github.hoangducmanh.smart_task_management.domain.shared.AuditInfo;
import com.github.hoangducmanh.smart_task_management.domain.user.exception.InvalidEmailException;
import com.github.hoangducmanh.smart_task_management.domain.user.exception.InvalidRoleException;
import com.github.hoangducmanh.smart_task_management.domain.user.exception.InvalidUserNameException;
import com.github.hoangducmanh.smart_task_management.domain.user.exception.UserDeletedException;

import java.time.LocalDateTime;
import java.util.Objects;


public class User {
    private final UserId id;                // Unique identifier for the user
    private Email email;                    // User's email address, can be updated
    private EmailStatus emailStatus;        // Status of the user's email (e.g., verified, unverified, pending verification)
    private HashedPassword hashedPassword;  // User's hashed password, can be updated
    private AuditInfo auditInfo;            // Audit information (createdAt, updatedAt, createdBy, updatedBy)
    private String name;                    // User's full name, can be updated
    private Role role;                      // User's role (e.g., admin, user), can be updated 
    
    private User(UserId id, Email email, EmailStatus emailStatus, HashedPassword hashedPassword, AuditInfo auditInfo, String name, Role role) {
        this.id = Objects.requireNonNull(id, "User ID cannot be null");
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.emailStatus = Objects.requireNonNull(emailStatus, "Email status cannot be null");
        this.hashedPassword = Objects.requireNonNull(hashedPassword, "Hashed password cannot be null");
        this.auditInfo = Objects.requireNonNull(auditInfo, "Audit info cannot be null");
        this.name = name; // Name can be null or blank, but we can allow it to be updated later
        this.role = Objects.requireNonNull(role, "Role cannot be null");
    }

    public UserId getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public EmailStatus getEmailStatus() {
        return emailStatus;
    }

    public HashedPassword getHashedPassword() {
        return hashedPassword;
    }

    public AuditInfo getAuditInfo() {
        return auditInfo;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public static User register(UserId id, Email email, HashedPassword hashedPassword,LocalDateTime registeredAt) {
        Objects.requireNonNull(id, "User ID cannot be null");
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(hashedPassword, "Hashed password cannot be null");
        Objects.requireNonNull(registeredAt, "Registered at cannot be null");
        AuditInfo auditInfo = AuditInfo.create(registeredAt);
        return new User(id, email, EmailStatus.UNVERIFIED, hashedPassword, auditInfo, null, Role.USER);
    }
        private void ensureNotDeleted() {
        if (auditInfo.isDeleted()) throw new UserDeletedException("User is deleted");
    }
    public void changeEmail(Email newEmail,  LocalDateTime changedAt) {
        ensureNotDeleted();
        Objects.requireNonNull(changedAt, "Changed at cannot be null");
        Objects.requireNonNull(newEmail, "New email cannot be null");
        if(newEmail.equals(this.email)) {
            throw new InvalidEmailException("New email cannot be the same as the current email");
        }
        this.email = newEmail;
        this.emailStatus = EmailStatus.UNVERIFIED; // Reset email status to pending verification when email changes
        this.auditInfo = this.auditInfo.update(changedAt);
    }

    public void markEmailAsVerified(LocalDateTime verifiedAt) {
        ensureNotDeleted();
        Objects.requireNonNull(verifiedAt, "Verified at cannot be null");
        this.emailStatus = this.emailStatus.verify();
        this.auditInfo = this.auditInfo.update(verifiedAt);
    }

    public void requestEmailVerification(LocalDateTime pendingAt) {
        ensureNotDeleted();
        Objects.requireNonNull(pendingAt, "Pending at cannot be null");
        this.emailStatus = this.emailStatus.requestVerification();
        this.auditInfo = this.auditInfo.update(pendingAt);
    }
    
    public void changePassword(HashedPassword newHashedPassword, LocalDateTime changedAt) {
        ensureNotDeleted();
        Objects.requireNonNull(changedAt, "Changed at cannot be null");
        Objects.requireNonNull(newHashedPassword, "New hashed password cannot be null");
        this.hashedPassword = newHashedPassword;
        this.auditInfo = this.auditInfo.update(changedAt);
    }

    public void changeName(String newName, LocalDateTime changedAt) {
        ensureNotDeleted();
        Objects.requireNonNull(changedAt, "Changed at cannot be null");
        if(newName == null || newName.trim().isEmpty()) {
            throw new InvalidUserNameException("New name cannot be null or blank");
        }
        this.name = newName.trim();
        this.auditInfo = this.auditInfo.update(changedAt);
    }

    public void changeRole(Role newRole, LocalDateTime changedAt) {
        ensureNotDeleted();
        Objects.requireNonNull(newRole, "New role cannot be null");
        Objects.requireNonNull(changedAt, "Changed at cannot be null");
         if (newRole == this.role) {
        throw new InvalidRoleException("New role cannot be the same as current role");
        }
        this.role = newRole;
        this.auditInfo = this.auditInfo.update(changedAt);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof User other)) {
            return false;
        }
        return Objects.equals(other.id, this.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
