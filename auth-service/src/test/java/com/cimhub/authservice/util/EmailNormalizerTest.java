package com.cimhub.authservice.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailNormalizerTest {

    @Test
    void normalize_shouldLowercaseAndTrim() {
        assertEquals("john.driver@example.com", EmailNormalizer.normalize(" John.Driver@Example.COM "));
    }

    @Test
    void normalize_withNull_shouldReturnNull() {
        assertNull(EmailNormalizer.normalize(null));
    }

    @Test
    void isValid_shouldReturnTrueForStandardEmail() {
        assertTrue(EmailNormalizer.isValid("user.name+tag@example.co"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "invalid-email"})
    void isValid_shouldReturnFalseForBlankOrMalformedEmail(String email) {
        assertFalse(EmailNormalizer.isValid(email));
    }
}
