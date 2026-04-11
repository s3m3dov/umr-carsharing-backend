package com.cimhub.authservice.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordValidatorTest {

    @Test
    void validate_shouldReturnValidForStrongPassword() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("StrongPass1!");

        assertTrue(result.valid());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"short", "lowercase1!", "UPPERCASE1!", "NoDigit!", "NoSpecial1"})
    void validate_shouldFailForMissingRequirements(String password) {
        assertFalse(PasswordValidator.validate(password).valid());
    }
}
