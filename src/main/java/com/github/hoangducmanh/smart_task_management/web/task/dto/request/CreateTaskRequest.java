package com.github.hoangducmanh.smart_task_management.web.task.dto.request;

import java.time.LocalDateTime;

import io.micrometer.common.lang.NonNull;
import jakarta.validation.constraints.NotBlank;

public record CreateTaskRequest(
    @NonNull @NotBlank String title,
    String description,
    @NonNull @NotBlank String priority,
    LocalDateTime deadline
) {

}
