package com.cimhub.authservice.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenHasherTest {

    @Test
    void generateSecureToken_shouldProduceUrlSafeNonEmptyTokens() {
        String token1 = TokenHasher.generateSecureToken();
        String token2 = TokenHasher.generateSecureToken();

        assertFalse(token1.isBlank());
        assertFalse(token2.isBlank());
        assertNotEquals(token1, token2);
    }

    @Test
    void hashToken_shouldBeDeterministicAndVerifiable() {
        String rawToken = "sample-token";

        String hash1 = TokenHasher.hashToken(rawToken);
        String hash2 = TokenHasher.hashToken(rawToken);

        assertEquals(hash1, hash2);
        assertTrue(TokenHasher.verifyToken(rawToken, hash1));
        assertFalse(TokenHasher.verifyToken("other-token", hash1));
    }
}
