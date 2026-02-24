package com.github.hoangducmanh.smart_task_management.domain.user.model;

import com.github.hoangducmanh.smart_task_management.domain.user.exception.InvalidEmailStatusTransitionException;

public enum EmailStatus {
    VERIFIED("verified"){
        @Override
        public EmailStatus requestVerification() {
            throw new InvalidEmailStatusTransitionException("Cannot request verification for a verified email");
            }
        @Override
        public EmailStatus verify() {
            throw new InvalidEmailStatusTransitionException("Email is already verified");
        }},
    UNVERIFIED("unverified"){
        @Override
        public EmailStatus requestVerification() {
            return PENDING_VERIFICATION;
        }
        @Override
        public EmailStatus verify() {
            throw new InvalidEmailStatusTransitionException("Cannot verify an unverified email without pending verification");
        }
    },
    PENDING_VERIFICATION("pending_verification"){
        @Override
        public EmailStatus requestVerification() {
            throw new InvalidEmailStatusTransitionException("Email is already pending verification");
        }
        @Override
        public EmailStatus verify() {
            return VERIFIED;
        }
    };

    private final String status;
    EmailStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }
    public static EmailStatus fromStatus(String status) {
        for (EmailStatus emailStatus : values()) {
            if (emailStatus.status.equalsIgnoreCase(status)) {
                return emailStatus;
            }
        }
        throw new IllegalArgumentException("Unknown email status: " + status);
    }
    public abstract EmailStatus requestVerification();
    public abstract EmailStatus verify();
}
