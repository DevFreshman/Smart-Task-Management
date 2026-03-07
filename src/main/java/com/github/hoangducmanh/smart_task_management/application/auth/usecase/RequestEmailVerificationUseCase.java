package com.github.hoangducmanh.smart_task_management.application.auth.usecase;

import com.github.hoangducmanh.smart_task_management.application.ClockSystem;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.RequestEmailVerificationCommand;
import com.github.hoangducmanh.smart_task_management.application.auth.dto.StoredEmailToken;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.EmailMismatchException;
import com.github.hoangducmanh.smart_task_management.application.auth.exception.UserNotFoundException;
import com.github.hoangducmanh.smart_task_management.application.auth.port.in.RequestEmailVerificationPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.email.SendEmailVerificationPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.token.EmailTokenGeneratorPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.token.EmailTokenHashPort;
import com.github.hoangducmanh.smart_task_management.application.auth.port.out.token.EmailVerificationTokenStore;
import com.github.hoangducmanh.smart_task_management.domain.user.model.Email;
import com.github.hoangducmanh.smart_task_management.domain.user.model.User;
import com.github.hoangducmanh.smart_task_management.domain.user.model.UserId;
import com.github.hoangducmanh.smart_task_management.domain.user.repository.UserRepository;

public class RequestEmailVerificationUseCase implements RequestEmailVerificationPort {
    private final EmailTokenGeneratorPort emailTokenGeneratorPort;
    private final EmailTokenHashPort emailTokenHashPort;
    private final UserRepository userRepository;
    private final SendEmailVerificationPort sendEmailVerificationPort;
    private final EmailVerificationTokenStore emailTokenRepository;
    private final ClockSystem clockSystem;

    public RequestEmailVerificationUseCase(EmailTokenGeneratorPort emailTokenGeneratorPort, EmailTokenHashPort emailTokenHashPort, UserRepository userRepository, 
        SendEmailVerificationPort sendEmailVerificationPort, EmailVerificationTokenStore emailTokenRepository, ClockSystem clockSystem) {
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
        String emailToken = emailTokenGeneratorPort.generateEmailToken();
        String hashedEmailToken = emailTokenHashPort.hash(emailToken);
        emailTokenRepository.saveOrReplace(StoredEmailToken.of(user.getId().value(), user.getEmail().asString(), hashedEmailToken));
        userRepository.save(user);
        sendEmailVerificationPort.sendEmailVerification(user.getEmail().asString(), emailToken);
    }
}
