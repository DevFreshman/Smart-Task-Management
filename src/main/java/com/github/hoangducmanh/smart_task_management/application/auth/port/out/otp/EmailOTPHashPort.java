package com.github.hoangducmanh.smart_task_management.application.auth.port.out.otp;

public interface EmailOTPHashPort {
    String hash(String token);
}
