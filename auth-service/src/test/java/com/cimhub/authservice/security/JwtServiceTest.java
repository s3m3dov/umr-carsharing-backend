package com.cimhub.authservice.security;

import com.cimhub.common.enums.UserType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final SecretKey SECRET = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final String BASE64_SECRET = Base64.getEncoder().encodeToString(SECRET.getEncoded());

    @Test
    void generateToken_withRole_shouldEmbedRoleAndUsername() {
        JwtService jwtService = new JwtService(BASE64_SECRET, 60_000);
        UserDetails userDetails = new User("driver@example.com", "pwd", List.of());

        String token = jwtService.generateToken(userDetails, UserType.DRIVER.name());

        assertNotNull(token);
        assertEquals("driver@example.com", jwtService.extractUsername(token));
        assertEquals(UserType.DRIVER.name(), jwtService.extractRole(token));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void isTokenValid_shouldReturnFalseForDifferentUserAndExpiredToken() {
        JwtService jwtService = new JwtService(BASE64_SECRET, 10_000);
        UserDetails userDetails = new User("user@example.com", "pwd", List.of());
        String token = jwtService.generateToken(userDetails, UserType.PASSENGER.name());

        assertFalse(jwtService.isTokenValid(token, "other@example.com"));

        Instant now = Instant.now();
        String expiredToken = Jwts.builder()
                .setSubject("user@example.com")
                .setIssuedAt(Date.from(now.minus(Duration.ofMinutes(10))))
                .setExpiration(Date.from(now.minus(Duration.ofMinutes(1))))
                .signWith(SECRET, SignatureAlgorithm.HS256)
                .compact();

        assertTrue(jwtService.isTokenExpired(expiredToken));
        assertFalse(jwtService.isTokenValid(expiredToken, "user@example.com"));
    }
}
