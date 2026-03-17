package com.github.hoangducmanh.smart_task_management.web.auth.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record RefreshRequest(
    @NotEmpty String refreshToken
) {

}
