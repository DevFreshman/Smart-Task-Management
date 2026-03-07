package com.github.hoangducmanh.smart_task_management.application.auth.dto;

import java.util.UUID;

public record RequestEmailVerificationCommand(UUID userId, String email) {
    public static RequestEmailVerificationCommand of(UUID userId, String email){
        return new RequestEmailVerificationCommand(userId, email);
    }

}
