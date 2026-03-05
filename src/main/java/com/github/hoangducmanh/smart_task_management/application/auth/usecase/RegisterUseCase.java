package com.github.hoangducmanh.smart_task_management.application.auth.usecase;

import java.time.Instant;

import com.github.hoangducmanh.smart_task_management.application.ClockSystem;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.RegisterCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.RegisterResult;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.EmailAlreadyExistsException;
import com.github.hoangducmanh.smart_task_management.application.auth.port.in.RegisterPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.PasswordHashPort;
import com.github.hoangducmanh.smart_task_management.domain.user.model.Email;
import com.github.hoangducmanh.smart_task_management.domain.user.model.HashedPassword;
import com.github.hoangducmanh.smart_task_management.domain.user.model.User;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import com.github.hoangducmanh.smart_task_management.domain.user.repository.UserRepository;

public class RegisterUseCase implements RegisterPort{
    private final UserRepository userRepository;
    private final PasswordHashPort passwordHashPort;
    private final ClockSystem clockSystem;
    public RegisterUseCase(UserRepository userRepository, PasswordHashPort passwordHashPort, ClockSystem clockSystem){
        this.userRepository = userRepository;
        this.passwordHashPort = passwordHashPort;
        this.clockSystem = clockSystem;
    }
    @Override
    public RegisterResult execute(RegisterCommand registerCommand) {
        Email email = Email.of(registerCommand.email());
        if(userRepository.existsByEmail(email)) throw new EmailAlreadyExistsException("Email already exists");
        HashedPassword hashedPassword = HashedPassword.of(passwordHashPort.encode(registerCommand.password()));
        UserId userId = UserId.generate();
        Instant now = clockSystem.now();
        String name = registerCommand.name();
        User user = User.register(userId, email, name, hashedPassword, now);
        User savedUser = userRepository.save(user);
        return RegisterResult.of(savedUser.getId().asString(), savedUser.getEmail().asString(), savedUser.getName());
    }

}
