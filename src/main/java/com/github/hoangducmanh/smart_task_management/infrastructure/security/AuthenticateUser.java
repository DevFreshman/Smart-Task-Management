package com.github.hoangducmanh.smart_task_management.infrastructure.security;

import java.util.List;
import java.util.UUID;

public record AuthenticateUser(
    UUID userId,
    List<String> roles
) {

}
