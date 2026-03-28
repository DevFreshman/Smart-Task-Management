package com.github.hoangducmanh.smart_task_management.application.auth.dto.command;

public record RegisterCommand(String email, String password, String name) {
    public static RegisterCommand of(String email, String password, String name){
        return new RegisterCommand(email, password, name);
    }
}
