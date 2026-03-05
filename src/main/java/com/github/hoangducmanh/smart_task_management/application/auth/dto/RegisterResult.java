package com.github.hoangducmanh.smart_task_management.application.auth.dto;

public record RegisterResult(String userId, String email, String name) {
    public static RegisterResult of(String userId, String email, String name){
        return new RegisterResult(userId, email, name);
    }
}
