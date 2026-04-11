package com.cimhub.authservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cimhub.authservice.dto.request.ChangePasswordRequest;
import com.cimhub.authservice.dto.request.ConfirmResetPasswordRequest;
import com.cimhub.authservice.dto.request.LoginRequest;
import com.cimhub.authservice.dto.request.RegisterUserRequest;
import com.cimhub.authservice.dto.request.ResetPasswordRequest;
import com.cimhub.authservice.dto.request.ValidateTokenRequest;
import com.cimhub.authservice.dto.response.LoginResponse;
import com.cimhub.authservice.dto.response.ResetPasswordResponse;
import com.cimhub.authservice.dto.response.SignUpResponse;
import com.cimhub.authservice.dto.response.ValidateTokenResponse;
import com.cimhub.authservice.service.AuthService;
import com.cimhub.authservice.service.PasswordManagementService;
import com.cimhub.authservice.service.RegistrationService;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.UnauthorizedException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private RegistrationService registrationService;

    @Mock
    private PasswordManagementService passwordManagementService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService, registrationService, passwordManagementService);
    }

    @Test
    void registerUser_shouldReturnCreatedResponse() {
        RegisterUserRequest request = new RegisterUserRequest(
                "John", "Driver", "john.driver@example.com", "Password123!", UserType.DRIVER, "+49 123", 31, "LIC-1");
        SignUpResponse signUpResponse = SignUpResponse.builder()
                .token("token-1")
                .email("john.driver@example.com")
                .role(UserType.DRIVER.name())
                .build();
        when(registrationService.registerUser(request)).thenReturn(signUpResponse);

        ResponseEntity<?> response = authController.registerUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(registrationService).registerUser(request);
    }

    @Test
    void login_shouldReturnOkResponse() {
        LoginRequest request = new LoginRequest("driver@example.com", "Password123!");
        LoginResponse loginResponse = new LoginResponse("jwt", "driver@example.com", UserType.DRIVER.name());
        when(authService.login(request)).thenReturn(loginResponse);

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService).login(request);
    }

    @Test
    void validateToken_whenValid_shouldReturnOk() {
        ValidateTokenRequest request = new ValidateTokenRequest("good-token");
        when(authService.validateToken(request))
                .thenReturn(new ValidateTokenResponse(true, "driver@example.com", UserType.DRIVER.name()));

        ResponseEntity<?> response = authController.validateToken(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService).validateToken(request);
    }

    @Test
    void validateToken_whenInvalid_shouldThrowUnauthorized() {
        ValidateTokenRequest request = new ValidateTokenRequest("bad-token");
        when(authService.validateToken(request)).thenReturn(new ValidateTokenResponse(false, null, null));

        assertThrows(UnauthorizedException.class, () -> authController.validateToken(request));
        verify(authService).validateToken(request);
    }

    @Test
    void resetPassword_shouldReturnOkResponse() {
        ResetPasswordRequest request = new ResetPasswordRequest("user@example.com");
        when(passwordManagementService.initiatePasswordReset(request))
                .thenReturn(new ResetPasswordResponse("If the email exists, a reset link has been sent"));

        ResponseEntity<?> response = authController.resetPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(passwordManagementService).initiatePasswordReset(request);
    }

    @Test
    void confirmResetPassword_shouldReturnOkResponse() {
        ConfirmResetPasswordRequest request = new ConfirmResetPasswordRequest("token", "NewPassword123!");
        when(passwordManagementService.confirmPasswordReset(request))
                .thenReturn(new ResetPasswordResponse("Password has been reset successfully"));

        ResponseEntity<?> response = authController.confirmResetPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(passwordManagementService).confirmPasswordReset(request);
    }

    @Test
    void changePassword_shouldUseAuthenticatedPrincipalAndReturnOk() {
        ChangePasswordRequest request = new ChangePasswordRequest("OldPassword123!", "NewPassword123!");
        User principal = new User("user@example.com", "pwd", List.of());
        when(passwordManagementService.changePassword("user@example.com", request))
                .thenReturn(new ResetPasswordResponse("Password has been changed successfully"));

        ResponseEntity<?> response = authController.changePassword(principal, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(passwordManagementService).changePassword("user@example.com", request);
    }
}
