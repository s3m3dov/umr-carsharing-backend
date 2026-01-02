package com.cimhub.authservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails testUserDetails;
    private String secretKey;

    @BeforeEach
    void setUp() {
        // Generate a valid base64-encoded secret key (256 bits for HS256)
        byte[] keyBytes = new byte[32];
        for (int i = 0; i < 32; i++) {
            keyBytes[i] = (byte) i;
        }
        secretKey = Base64.getEncoder().encodeToString(keyBytes);

        jwtService = new JwtService(secretKey);

        testUserDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .roles("USER")
                .build();
    }

    @Test
    void generateToken_whenValidUserDetails_thenReturnsToken() {
        // When
        String token = jwtService.generateToken(testUserDetails);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature
    }

    @Test
    void generateToken_withExtraClaims_thenReturnsToken() {
        // Given
        HashMap<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("customField", "customValue");

        // When
        String token = jwtService.generateToken(extraClaims, testUserDetails);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void extractUsername_whenValidToken_thenReturnsUsername() {
        // Given
        String token = jwtService.generateToken(testUserDetails);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertThat(username).isEqualTo("test@example.com");
    }

    @Test
    void extractUsername_whenInvalidToken_thenThrowsException() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertThatThrownBy(() -> jwtService.extractUsername(invalidToken))
                .isInstanceOf(Exception.class);
    }

    @Test
    void isTokenValid_whenValidTokenAndMatchingUsername_thenReturnsTrue() {
        // Given
        String token = jwtService.generateToken(testUserDetails);

        // When
        boolean isValid = jwtService.isTokenValid(token, "test@example.com");

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_whenValidTokenButDifferentUsername_thenReturnsFalse() {
        // Given
        String token = jwtService.generateToken(testUserDetails);

        // When
        boolean isValid = jwtService.isTokenValid(token, "different@example.com");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenExpired_whenFreshToken_thenReturnsFalse() {
        // Given
        String token = jwtService.generateToken(testUserDetails);

        // When
        boolean isExpired = jwtService.isTokenExpired(token);

        // Then
        assertThat(isExpired).isFalse();
    }

    @Test
    void generateToken_whenCalledMultipleTimes_thenGeneratesDifferentTokens() {
        // When
        String token1 = jwtService.generateToken(testUserDetails);

        // Add small delay to ensure different timestamps
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String token2 = jwtService.generateToken(testUserDetails);

        // Then
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void extractUsername_whenTokenFromDifferentUser_thenReturnsCorrectUsername() {
        // Given
        UserDetails anotherUser = User.builder()
                .username("another@example.com")
                .password("password")
                .roles("USER")
                .build();

        String token = jwtService.generateToken(anotherUser);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertThat(username).isEqualTo("another@example.com");
    }
}
