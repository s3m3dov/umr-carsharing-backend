package com.cimhub.authservice.service;

import com.cimhub.authservice.config.TokenProperties;
import com.cimhub.authservice.dto.request.ChangePasswordRequest;
import com.cimhub.authservice.dto.request.ConfirmResetPasswordRequest;
import com.cimhub.authservice.dto.request.ResetPasswordRequest;
import com.cimhub.authservice.dto.response.ResetPasswordResponse;
import com.cimhub.authservice.model.User;
import com.cimhub.authservice.repository.AuthUserRepository;
import com.cimhub.authservice.security.TokenHasher;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.BadRequestException;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.common.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordManagementServiceTest {

    @Mock private AuthUserRepository authUserRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditService auditService;

    private TokenProperties tokenProperties;
    private PasswordManagementService passwordManagementService;

    @BeforeEach
    void setUp() {
        tokenProperties = new TokenProperties();
        tokenProperties.setResetTokenExpiryMs(3600000L);
        
        passwordManagementService = new PasswordManagementService(
                authUserRepository,
                passwordEncoder,
                auditService,
                tokenProperties);
    }

    // ── initiatePasswordReset ─────────────────────────────────────────────

    @Test
    void initiatePasswordReset_WithValidUser_ShouldGenerateResetToken() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setPassword("hashedPassword");
        user.setUserType(UserType.DRIVER);

        when(authUserRepository.findByEmail(email.toLowerCase())).thenReturn(Optional.of(user));
        when(authUserRepository.save(any(User.class))).thenReturn(user);

        ResetPasswordRequest request = new ResetPasswordRequest(email);
        ResetPasswordResponse response = passwordManagementService.initiatePasswordReset(request);

        assertNotNull(response);
        assertEquals("If the email exists, a reset link has been sent", response.message());
        verify(authUserRepository).save(any(User.class));
    }

    @Test
    void initiatePasswordReset_WithNonExistentUser_ShouldReturnGenericMessage() {
        String email = "nonexistent@example.com";

        when(authUserRepository.findByEmail(email.toLowerCase())).thenReturn(Optional.empty());

        ResetPasswordRequest request = new ResetPasswordRequest(email);
        ResetPasswordResponse response = passwordManagementService.initiatePasswordReset(request);

        assertNotNull(response);
        assertEquals("If the email exists, a reset link has been sent", response.message());
        verify(authUserRepository, never()).save(any());
    }

    // ── confirmPasswordReset ────────────────────────────────────────────────

    @Test
    void confirmPasswordReset_WithValidToken_ShouldResetPassword() {
        String rawToken = "valid-reset-token";
        String hashedToken = TokenHasher.hashToken(rawToken);
        String newPassword = "NewPassword123!";
        Date futureExpiry = new Date(System.currentTimeMillis() + 3600000);

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("oldHashedPassword");
        user.setUserType(UserType.DRIVER);
        user.setResetToken(rawToken);
        user.setResetTokenHash(hashedToken);
        user.setResetTokenExpiry(futureExpiry);

        when(authUserRepository.findByResetTokenHash(hashedToken)).thenReturn(Optional.of(user));
        when(authUserRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("newHashedPassword");

        ConfirmResetPasswordRequest request = new ConfirmResetPasswordRequest(rawToken, newPassword);
        ResetPasswordResponse response = passwordManagementService.confirmPasswordReset(request);

        assertNotNull(response);
        assertEquals("Password has been reset successfully", response.message());
        verify(passwordEncoder).encode(newPassword);
        verify(authUserRepository).save(user);
    }

    @Test
    void confirmPasswordReset_WithInvalidToken_ShouldThrowException() {
        String token = "invalid-token";
        String hashedToken = TokenHasher.hashToken(token);

        when(authUserRepository.findByResetTokenHash(hashedToken)).thenReturn(Optional.empty());

        ConfirmResetPasswordRequest request = new ConfirmResetPasswordRequest(token, "newPassword");

        assertThrows(UnauthorizedException.class, () -> passwordManagementService.confirmPasswordReset(request));
    }

    @Test
    void confirmPasswordReset_WithExpiredToken_ShouldThrowException() {
        String rawToken = "expired-token";
        String hashedToken = TokenHasher.hashToken(rawToken);
        Date pastExpiry = new Date(System.currentTimeMillis() - 3600000);

        User user = new User();
        user.setEmail("test@example.com");
        user.setResetToken(rawToken);
        user.setResetTokenHash(hashedToken);
        user.setResetTokenExpiry(pastExpiry);

        when(authUserRepository.findByResetTokenHash(hashedToken)).thenReturn(Optional.of(user));
        when(authUserRepository.save(any(User.class))).thenReturn(user);

        ConfirmResetPasswordRequest request = new ConfirmResetPasswordRequest(rawToken, "newPassword");

        assertThrows(UnauthorizedException.class, () -> passwordManagementService.confirmPasswordReset(request));
    }

    // ── changePassword ──────────────────────────────────────────────────

    @Test
    void changePassword_withCorrectCurrentPassword_shouldHashAndSave() {
        User user = new User();
        user.setUserId("user-1");
        user.setEmail("user@example.com");
        user.setPassword("old-hashed");

        when(authUserRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass", "old-hashed")).thenReturn(true);
        when(passwordEncoder.encode("NewPass")).thenReturn("new-hashed");
        when(authUserRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ResetPasswordResponse response = passwordManagementService.changePassword(
                "user@example.com", new ChangePasswordRequest("OldPass", "NewPass"));

        assertEquals("Password has been changed successfully", response.message());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(authUserRepository).save(captor.capture());
        assertEquals("new-hashed", captor.getValue().getPassword());
    }

    @Test
    void changePassword_withWrongCurrentPassword_shouldThrowUnauthorized() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("real-hashed");

        when(authUserRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass", "real-hashed")).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> passwordManagementService.changePassword("user@example.com",
                        new ChangePasswordRequest("WrongPass", "NewPass")));

        verify(authUserRepository, never()).save(any());
    }

    @Test
    void changePassword_whenUserNotFound_shouldThrowUnauthorized() {
        when(authUserRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class,
                () -> passwordManagementService.changePassword("ghost@example.com",
                        new ChangePasswordRequest("any", "any")));
    }

    @Test
    void changePassword_withWeakNewPassword_shouldThrowBadRequest() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("old-hashed");

        when(authUserRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass", "old-hashed")).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> passwordManagementService.changePassword("user@example.com",
                        new ChangePasswordRequest("OldPass", "weak")));
    }
}