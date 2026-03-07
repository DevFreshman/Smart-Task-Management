package com.github.hoangducmanh.smart_task_management.application.auth.port.out.token;

public interface RefreshTokenHashPort {
    // Hash refresh token using SHA-256 (deterministic), optionally with server-side pepper.
    String hash(String token);
}
