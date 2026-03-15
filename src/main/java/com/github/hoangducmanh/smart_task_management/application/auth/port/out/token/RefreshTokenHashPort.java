package com.github.hoangducmanh.smart_task_management.application.auth.port.out.token;

public interface RefreshTokenHashPort {
    String hash(String token);
}
