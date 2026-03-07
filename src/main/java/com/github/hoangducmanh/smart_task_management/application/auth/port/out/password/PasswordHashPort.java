package com.github.hoangducmanh.smart_task_management.application.auth.port.out.password;

public interface PasswordHashPort {
    String encode(String password);
    boolean matches(String rawPassword, String storedHash);
}
