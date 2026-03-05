package com.github.hoangducmanh.smart_task_management.application.auth.port.out;

public interface TokenHashPort {
    // Hash refresh token using SHA-256 (deterministic), optionally with server-side pepper.
    String hash(String token);
}
