package com.github.hoangducmanh.smart_task_management.application.auth.dto;

public record LoginCommand(String email, String password) {
    public static LoginCommand of(String email, String password){
        return new LoginCommand(email, password);
    }
}
