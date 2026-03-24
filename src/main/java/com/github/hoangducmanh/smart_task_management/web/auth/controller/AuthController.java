package com.github.hoangducmanh.smart_task_management.web.auth.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.hoangducmanh.smart_task_management.application.auth.dto.LogoutCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.port.in.LoginPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.in.LogoutPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.in.RefreshTokenPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.in.RegisterPort;
import com.github.hoangducmanh.smart_task_management.infrastructure.security.AuthenticateUser;
import com.github.hoangducmanh.smart_task_management.web.auth.dto.request.LoginRequest;
import com.github.hoangducmanh.smart_task_management.web.auth.dto.request.RefreshRequest;
import com.github.hoangducmanh.smart_task_management.web.auth.dto.request.RegisterRequest;
import com.github.hoangducmanh.smart_task_management.web.auth.dto.response.LoginResponse;
import com.github.hoangducmanh.smart_task_management.web.auth.dto.response.RefreshResponse;
import com.github.hoangducmanh.smart_task_management.web.auth.mapper.LoginMapper;
import com.github.hoangducmanh.smart_task_management.web.auth.mapper.RefreshMapper;
import com.github.hoangducmanh.smart_task_management.web.auth.mapper.RegisterMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/auth")
public class AuthController {
    private final LoginPort loginUseCase;
    private final LogoutPort logoutUseCase;
    private final RegisterPort registerUseCase;
    private final RefreshTokenPort refreshTokenUseCase;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = LoginMapper.toResponse(
            loginUseCase.execute(LoginMapper.toCommand(request)));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
            registerUseCase.execute(RegisterMapper.toCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal AuthenticateUser authenticateUser) {
        if(authenticateUser == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        logoutUseCase.execute(LogoutCommand.of(authenticateUser.userId()));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshResponse response = RefreshMapper.toResponse(
            refreshTokenUseCase.execute(RefreshMapper.toCommand(request)));
        return ResponseEntity.ok(response);
    }
    
}
