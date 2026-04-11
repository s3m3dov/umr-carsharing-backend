package com.cimhub.authservice.service;

import com.cimhub.authservice.dto.request.LoginRequest;
import com.cimhub.authservice.dto.request.ValidateTokenRequest;
import com.cimhub.authservice.dto.response.LoginResponse;
import com.cimhub.authservice.dto.response.ValidateTokenResponse;
import com.cimhub.authservice.repository.AuthUserRepository;
import com.cimhub.authservice.security.JwtService;
import com.cimhub.authservice.testsupport.AuthFixtures;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.common.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private AuthUserRepository authUserRepository;
    @Mock private AuditService auditService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                jwtService,
                authenticationManager,
                authUserRepository,
                auditService);
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_withValidCredentials_shouldReturnToken() {
        var user = AuthFixtures.user("driver@example.com", UserType.DRIVER);

        when(authUserRepository.findByEmail("driver@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateTokenWithRole(any(), anyString())).thenReturn("valid-token");

        LoginResponse response = authService.login(new LoginRequest("driver@example.com", "Password123!"));

        assertEquals("valid-token", response.token());
        assertEquals("driver@example.com", response.email());
        assertEquals("DRIVER", response.role());
    }

    @Test
    void login_emailIsNormalizedToLowercase() {
        var user = AuthFixtures.user("driver@example.com", UserType.DRIVER);

        when(authUserRepository.findByEmail("driver@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateTokenWithRole(any(), anyString())).thenReturn("token");

        LoginResponse response = authService.login(new LoginRequest("Driver@Example.COM", "Password123!"));

        assertEquals("driver@example.com", response.email());
    }

    @Test
    void login_withWrongCredentials_shouldThrowUnauthorized() {
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThrows(UnauthorizedException.class,
                () -> authService.login(new LoginRequest("bad@example.com", "wrong")));

        verify(jwtService, never()).generateTokenWithRole(any(), anyString());
    }

    // ── validateToken ─────────────────────────────────────────────────────────

    @Test
    void validateToken_withValidToken_shouldReturnTrueAndExtractClaims() {
        when(jwtService.extractUsername("good-token")).thenReturn("driver@example.com");
        when(jwtService.isTokenValid("good-token", "driver@example.com")).thenReturn(true);
        when(jwtService.extractRole("good-token")).thenReturn("DRIVER");

        ValidateTokenResponse response = authService.validateToken(new ValidateTokenRequest("good-token"));

        assertTrue(response.isValid());
        assertEquals("driver@example.com", response.email());
        assertEquals("DRIVER", response.role());
    }

    @Test
    void validateToken_withExpiredToken_shouldReturnFalseWithoutThrowing() {
        when(jwtService.extractUsername("expired-token"))
                .thenThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "expired"));

        ValidateTokenResponse response = authService.validateToken(new ValidateTokenRequest("expired-token"));

        assertFalse(response.isValid());
    }

    @Test
    void validateToken_withMalformedToken_shouldReturnFalseWithoutThrowing() {
        when(jwtService.extractUsername("garbage"))
                .thenThrow(new io.jsonwebtoken.MalformedJwtException("bad token"));

        ValidateTokenResponse response = authService.validateToken(new ValidateTokenRequest("garbage"));

        assertFalse(response.isValid());
    }

    @Test
    void validateToken_whenTokenValidReturnsInvalid_shouldReflectFalse() {
        when(jwtService.extractUsername("stale-token")).thenReturn("user@example.com");
        when(jwtService.isTokenValid("stale-token", "user@example.com")).thenReturn(false);
        when(jwtService.extractRole("stale-token")).thenReturn("PASSENGER");

        ValidateTokenResponse response = authService.validateToken(new ValidateTokenRequest("stale-token"));

        assertFalse(response.isValid());
    }
}
