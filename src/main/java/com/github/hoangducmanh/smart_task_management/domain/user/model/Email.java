package com.github.hoangducmanh.smart_task_management.domain.user.model;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import com.github.hoangducmanh.smart_task_management.domain.user.exception.InvalidEmailException;

public final record Email(String value) {
    private static final Pattern EMAIL_REGEX = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
        "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    public Email {
        Objects.requireNonNull(value, "Email cannot be null or blank");
        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            throw new InvalidEmailException("Email cannot be empty");
        }
        String normalizedEmail = trimmedValue.toLowerCase(Locale.ROOT);
        if (!EMAIL_REGEX.matcher(normalizedEmail).matches()) {
            throw new InvalidEmailException("Invalid email format");
        }
        value = normalizedEmail;
    }
    public static Email of(String value) {
        return new Email(value);
    }
    public String getDomain() {
        return value.substring(value.indexOf('@') + 1);
    }
}
