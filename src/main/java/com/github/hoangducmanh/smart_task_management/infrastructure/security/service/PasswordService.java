package com.github.hoangducmanh.smart_task_management.infrastructure.security.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.hoangducmanh.smart_task_management.application.auth.port.out.password.PasswordHashPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordService implements PasswordHashPort {
    private final BCryptPasswordEncoder passsEncoder;
    
    @Override
    public String encode(String password) {
        return passsEncoder.encode(password);
    }

    @Override
    public boolean matches(String rawPassword, String storedHash) {
        return passsEncoder.matches(rawPassword, storedHash);
        }

}
