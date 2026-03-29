package com.cimhub.authservice.service;

import com.cimhub.authservice.dto.request.ConfirmResetPasswordRequest;
import com.cimhub.authservice.dto.request.ResetPasswordRequest;
import com.cimhub.authservice.dto.response.ResetPasswordResponse;
import com.cimhub.authservice.model.User;
import com.cimhub.authservice.repository.AuthUserRepository;
import com.cimhub.authservice.security.JwtService;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceResetPasswordTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserServiceClient userServiceClient;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(jwtService, authenticationManager, authUserRepository, passwordEncoder, userServiceClient);
    }

    @Test
    void initiatePasswordReset_WithValidUser_ShouldGenerateResetToken() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setPassword("hashedPassword");
        user.setUserType(UserType.DRIVER);

        when(authUserRepository.findByEmail(email.toLowerCase())).thenReturn(user);
        when(authUserRepository.save(any(User.class))).thenReturn(user);

        ResetPasswordRequest request = new ResetPasswordRequest(email);
        ResetPasswordResponse response = authService.initiatePasswordReset(request);

        assertNotNull(response);
        assertEquals("If the email exists, a reset link has been sent", response.message());
        verify(authUserRepository).save(any(User.class));
    }

    @Test
    void initiatePasswordReset_WithNonExistentUser_ShouldReturnGenericMessage() {
        String email = "nonexistent@example.com";

        when(authUserRepository.findByEmail(email.toLowerCase())).thenReturn(null);

        ResetPasswordRequest request = new ResetPasswordRequest(email);
        ResetPasswordResponse response = authService.initiatePasswordReset(request);

        assertNotNull(response);
        assertEquals("If the email exists, a reset link has been sent", response.message());
        verify(authUserRepository, never()).save(any(User.class));
    }

    @Test
    void confirmPasswordReset_WithValidToken_ShouldResetPassword() {
        String token = "valid-reset-token";
        String newPassword = "NewPassword123!";
        Date futureExpiry = new Date(System.currentTimeMillis() + 3600000);

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("oldHashedPassword");
        user.setUserType(UserType.DRIVER);
        user.setResetToken(token);
        user.setResetTokenExpiry(futureExpiry);

        when(authUserRepository.findByResetToken(token)).thenReturn(user);
        when(authUserRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("newHashedPassword");

        ConfirmResetPasswordRequest request = new ConfirmResetPasswordRequest(token, newPassword);
        ResetPasswordResponse response = authService.confirmPasswordReset(request);

        assertNotNull(response);
        assertEquals("Password has been reset successfully", response.message());
        verify(passwordEncoder).encode(newPassword);
        verify(authUserRepository).save(user);
    }

    @Test
    void confirmPasswordReset_WithInvalidToken_ShouldThrowException() {
        String token = "invalid-token";

        when(authUserRepository.findByResetToken(token)).thenReturn(null);

        ConfirmResetPasswordRequest request = new ConfirmResetPasswordRequest(token, "newPassword");

        assertThrows(UnauthorizedException.class, () -> authService.confirmPasswordReset(request));
    }

    @Test
    void confirmPasswordReset_WithExpiredToken_ShouldThrowException() {
        String token = "expired-token";
        Date pastExpiry = new Date(System.currentTimeMillis() - 3600000);

        User user = new User();
        user.setEmail("test@example.com");
        user.setResetToken(token);
        user.setResetTokenExpiry(pastExpiry);

        when(authUserRepository.findByResetToken(token)).thenReturn(user);
        when(authUserRepository.save(any(User.class))).thenReturn(user);

        ConfirmResetPasswordRequest request = new ConfirmResetPasswordRequest(token, "newPassword");

        assertThrows(UnauthorizedException.class, () -> authService.confirmPasswordReset(request));
        verify(authUserRepository).save(user);
    }

    @Test
    void confirmPasswordReset_WithMissingExpiry_ShouldThrowException() {
        String token = "token-without-expiry";

        User user = new User();
        user.setEmail("test@example.com");
        user.setResetToken(token);
        user.setResetTokenExpiry(null);

        when(authUserRepository.findByResetToken(token)).thenReturn(user);

        ConfirmResetPasswordRequest request = new ConfirmResetPasswordRequest(token, "newPassword");

        assertThrows(UnauthorizedException.class, () -> authService.confirmPasswordReset(request));
    }
}
