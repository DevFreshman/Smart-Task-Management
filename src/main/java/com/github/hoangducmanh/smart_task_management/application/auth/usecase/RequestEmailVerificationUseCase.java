package com.github.hoangducmanh.smart_task_management.application.auth.usecase;

import com.github.hoangducmanh.smart_task_management.application.TimeProvider;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.command.RequestEmailVerificationCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.store.StoredEmailOTP;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.EmailMismatchException;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.UserNotFoundException;
import com.github.hoangducmanh.smart_task_management.application.auth.port.in.RequestEmailVerificationPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.email.SendEmailVerificationPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.otp.EmailOTPGeneratorPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.otp.EmailOTPHashPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.otp.EmailVerificationOTPStore;
import com.github.hoangducmanh.smart_task_management.domain.user.model.Email;
import com.github.hoangducmanh.smart_task_management.domain.user.model.User;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import com.github.hoangducmanh.smart_task_management.domain.user.repository.UserRepository;

public class RequestEmailVerificationUseCase implements RequestEmailVerificationPort {
    private final EmailOTPGeneratorPort emailTokenGeneratorPort;
    private final EmailOTPHashPort emailTokenHashPort;
    private final UserRepository userRepository;
    private final SendEmailVerificationPort sendEmailVerificationPort;
    private final EmailVerificationOTPStore emailTokenRepository;
    private final TimeProvider clockSystem;

    public RequestEmailVerificationUseCase(EmailOTPGeneratorPort emailTokenGeneratorPort, EmailOTPHashPort emailTokenHashPort, UserRepository userRepository, 
        SendEmailVerificationPort sendEmailVerificationPort, EmailVerificationOTPStore emailTokenRepository, TimeProvider clockSystem) {
        this.emailTokenGeneratorPort = emailTokenGeneratorPort;
        this.emailTokenHashPort = emailTokenHashPort;
        this.userRepository = userRepository;
        this.sendEmailVerificationPort = sendEmailVerificationPort;
        this.emailTokenRepository = emailTokenRepository;
        this.clockSystem = clockSystem;
    }
    
    @Override
    public void execute(RequestEmailVerificationCommand command) {
        UserId userId = UserId.of(command.userId());
        Email email = Email.of(command.email());
        
        User user = userRepository.findById(userId).orElseThrow(
            () -> new UserNotFoundException("User not found for the given user ID")
        );
        if (!user.getEmail().equals(email)) { 
            throw new EmailMismatchException("Provided email does not match user's email");
        }
        user.requestEmailVerification(clockSystem.now());
        String emailToken = emailTokenGeneratorPort.generateEmailOTP();
        String hashedEmailToken = emailTokenHashPort.hash(emailToken);
        emailTokenRepository.saveOrReplace(StoredEmailOTP.of(user.getId().value(), user.getEmail().asString(), hashedEmailToken));
        userRepository.save(user);
        sendEmailVerificationPort.sendEmailVerification(user.getEmail().asString(), emailToken);
    }
}
