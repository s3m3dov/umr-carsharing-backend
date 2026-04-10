package com.cimhub.authservice.service;

import com.cimhub.authservice.config.TokenProperties;
import com.cimhub.authservice.dto.request.RegisterUserRequest;
import com.cimhub.authservice.dto.response.SignUpResponse;
import com.cimhub.authservice.model.User;
import com.cimhub.authservice.repository.AuthUserRepository;
import com.cimhub.authservice.security.JwtService;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.BadRequestException;
import com.cimhub.common.exception.UserAlreadyExistsException;
import com.cimhub.common.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock private AuthUserRepository authUserRepository;
    @Mock private UserServiceClient userServiceClient;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditService auditService;

    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(
                authUserRepository,
                userServiceClient,
                jwtService,
                passwordEncoder,
                auditService);
    }

    // ── registerUser ──────────────────────────────────────────────────────────

    @Test
    void registerUser_withNewEmail_shouldSaveUserAndReturnToken() {
        RegisterUserRequest request = new RegisterUserRequest(
                "John", "Driver", "John.Driver@Example.com",
                "Password123!", UserType.DRIVER, "+49 123", 30, "LIC-001");

        when(authUserRepository.findByEmail("john.driver@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed");
        when(authUserRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateTokenWithRole(any(), anyString())).thenReturn("jwt-token");

        SignUpResponse response = registrationService.registerUser(request);

        assertEquals("jwt-token", response.token());
        assertEquals("john.driver@example.com", response.email());
        assertEquals("DRIVER", response.role());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(authUserRepository).save(captor.capture());
        assertEquals("john.driver@example.com", captor.getValue().getEmail());
        assertEquals("hashed", captor.getValue().getPassword());
        assertEquals(UserType.DRIVER, captor.getValue().getUserType());
    }

    @Test
    void registerUser_passengerEmailIsNormalizedToLowercase() {
        RegisterUserRequest request = new RegisterUserRequest(
                "Jane", "P", "Jane.P@Example.COM",
                "Pw123!", UserType.PASSENGER, "+1", 25, null);

        when(authUserRepository.findByEmail("jane.p@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(authUserRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateTokenWithRole(any(), anyString())).thenReturn("token");

        SignUpResponse response = registrationService.registerUser(request);

        assertEquals("jane.p@example.com", response.email());
    }

    @Test
    void registerUser_whenEmailAlreadyInAuthRepo_shouldThrowUserAlreadyExists() {
        RegisterUserRequest request = new RegisterUserRequest(
                "John", "Driver", "existing@example.com",
                "Password123!", UserType.DRIVER, "+49 123", 30, "LIC-EXISTS");

        when(authUserRepository.findByEmail("existing@example.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class, () -> registrationService.registerUser(request));
        verify(authUserRepository, never()).save(any());
    }

    @Test
    void registerUser_whenDriverLicenseMissing_shouldThrowBadRequest() {
        RegisterUserRequest request = new RegisterUserRequest(
                "John", "Driver", "profile@example.com",
                "Password123!", UserType.DRIVER, "+49 123", 30, null);

        assertThrows(BadRequestException.class, () -> registrationService.registerUser(request));
        verify(authUserRepository, never()).save(any());
    }

    @Test
    void registerUser_whenProfileCreationFails_shouldRollbackAuthUserAndRethrow() {
        RegisterUserRequest request = new RegisterUserRequest(
                "John", "Driver", "rollback@example.com",
                "Password123!", UserType.DRIVER, "+49 123", 30, "LIC-ROLLBACK");

        when(authUserRepository.findByEmail("rollback@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(authUserRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("user-service down"))
                .when(userServiceClient).createDriverSubProfile(
                        "rollback@example.com",
                        "John",
                        "Driver",
                        "+49 123",
                        30,
                        "LIC-ROLLBACK");

        assertThrows(RuntimeException.class, () -> registrationService.registerUser(request));

        verify(authUserRepository).deleteById("rollback@example.com");
    }

    @Test
    void registerUser_withWeakPassword_shouldThrowBadRequest() {
        RegisterUserRequest request = new RegisterUserRequest(
                "John", "Driver", "weakpass@example.com",
                "weak", UserType.DRIVER, "+49 123", 30, "LIC-001");

        assertThrows(BadRequestException.class, () -> registrationService.registerUser(request));
    }
}