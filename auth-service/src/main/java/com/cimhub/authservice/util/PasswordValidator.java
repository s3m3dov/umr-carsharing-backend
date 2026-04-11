package com.cimhub.authservice.util;

import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@UtilityClass
public class PasswordValidator {

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    public static ValidationResult validate(String password) {
        if (password == null || password.isBlank()) {
            return ValidationResult.of(false, "Password cannot be empty");
        }
        if (password.length() < 8) {
            return ValidationResult.of(false, "Password must be at least 8 characters");
        }
        if (!UPPERCASE.matcher(password).find()) {
            return ValidationResult.of(false, "Password must contain at least one uppercase letter");
        }
        if (!LOWERCASE.matcher(password).find()) {
            return ValidationResult.of(false, "Password must contain at least one lowercase letter");
        }
        if (!DIGIT.matcher(password).find()) {
            return ValidationResult.of(false, "Password must contain at least one digit");
        }
        if (!SPECIAL.matcher(password).find()) {
            return ValidationResult.of(false, "Password must contain at least one special character");
        }
        return ValidationResult.of(true, "Password is valid");
    }

    public record ValidationResult(boolean valid, String message) {
        public static ValidationResult of(boolean valid, String message) {
            return new ValidationResult(valid, message);
        }
    }
}