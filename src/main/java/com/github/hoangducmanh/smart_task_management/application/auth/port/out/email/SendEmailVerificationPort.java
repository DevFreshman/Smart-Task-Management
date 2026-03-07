package com.github.hoangducmanh.smart_task_management.application.auth.port.out.email;

public interface SendEmailVerificationPort {
    void sendEmailVerification(String email, String verificationToken);
}
