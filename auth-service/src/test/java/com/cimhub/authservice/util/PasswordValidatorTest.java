package com.cimhub.authservice.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordValidatorTest {

    @Test
    void validate_shouldReturnValidForStrongPassword() {
        PasswordValidator.ValidationResult result = PasswordValidator.validate("StrongPass1!");

        assertTrue(result.valid());
    }

    @Test
    void validate_shouldFailForMissingRequirements() {
        assertFalse(PasswordValidator.validate(null).valid());
        assertFalse(PasswordValidator.validate("short").valid());
        assertFalse(PasswordValidator.validate("lowercase1!").valid());
        assertFalse(PasswordValidator.validate("UPPERCASE1!").valid());
        assertFalse(PasswordValidator.validate("NoDigit!").valid());
        assertFalse(PasswordValidator.validate("NoSpecial1").valid());
    }
}
