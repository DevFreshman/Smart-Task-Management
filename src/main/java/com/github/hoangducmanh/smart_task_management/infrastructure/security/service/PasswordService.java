package com.github.hoangducmanh.smart_task_management.infrastructure.security.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.hoangducmanh.smart_task_management.application.auth.port.out.password.PasswordHashPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordService implements PasswordHashPort {
    private final PasswordEncoder passEncoder;
    
    @Override
    public String encode(String password) {
        return passEncoder.encode(password);
    }

    @Override
    public boolean matches(String rawPassword, String storedHash) {
        return passEncoder.matches(rawPassword, storedHash);
        }

}
