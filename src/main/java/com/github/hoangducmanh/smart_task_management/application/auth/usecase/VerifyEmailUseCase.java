package com.github.hoangducmanh.smart_task_management.application.auth.usecase;

import com.github.hoangducmanh.smart_task_management.application.TimeProvider;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.command.VerifyEmailCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.store.StoredEmailOTP;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.EmailMismatchException;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.TokenDoesNotMatchException;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.UserNotFoundException;
import com.github.hoangducmanh.smart_task_management.application.auth.port.in.VerifyEmailPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.otp.EmailOTPHashPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.otp.EmailVerificationOTPStore;
import com.github.hoangducmanh.smart_task_management.domain.user.model.Email;
import com.github.hoangducmanh.smart_task_management.domain.user.model.User;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import com.github.hoangducmanh.smart_task_management.domain.user.repository.UserRepository;

public class VerifyEmailUseCase implements VerifyEmailPort {
    private final UserRepository userRepository;
    private final EmailVerificationOTPStore emailTokenRepository;
    private final EmailOTPHashPort emailTokenHashPort;
    private final TimeProvider clockSystem;

    public VerifyEmailUseCase(UserRepository userRepository, EmailVerificationOTPStore emailTokenRepository, EmailOTPHashPort emailTokenHashPort, TimeProvider clockSystem) {
        this.userRepository = userRepository;
        this.emailTokenRepository = emailTokenRepository;
        this.emailTokenHashPort = emailTokenHashPort;
        this.clockSystem = clockSystem;
    }
    @Override
    public void execute(VerifyEmailCommand command) {
        UserId userId = UserId.of(command.userId());
        Email email = Email.of(command.email());
        String rawToken = command.token();

        String hashedToken = emailTokenHashPort.hash(rawToken);

        User user = userRepository.findById(userId).orElseThrow(
            () -> new UserNotFoundException("User not found for the given user ID")
        );
        
        if(!user.getEmail().equals(email)) {
            throw new EmailMismatchException("Provided email does not match user's email");
        }
        
        StoredEmailOTP storedToken = emailTokenRepository.consumeIfMatches(userId.value(), hashedToken).orElseThrow(
            () -> new TokenDoesNotMatchException("Email verification token does not match")
        );
        
        if(!storedToken.email().equals(email.value())) {
            throw new EmailMismatchException("Provided email does not match the email associated with the token");
        }
        // change email status to VERIFIED
        user.markEmailAsVerified(clockSystem.now());
        // Update the user's email status to VERIFIED
        userRepository.save(user);

    }

}