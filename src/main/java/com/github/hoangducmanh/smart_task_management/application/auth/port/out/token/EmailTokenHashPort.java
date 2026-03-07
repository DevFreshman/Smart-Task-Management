package com.github.hoangducmanh.smart_task_management.application.auth.port.out.token;

public interface EmailTokenHashPort {
    String hash(String token);
}
