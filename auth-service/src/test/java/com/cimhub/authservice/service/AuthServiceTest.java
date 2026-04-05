package com.cimhub.authservice.service;

import com.cimhub.authservice.dto.request.ChangePasswordRequest;
import com.cimhub.authservice.dto.request.LoginRequest;
import com.cimhub.authservice.dto.request.RegisterUserRequest;
import com.cimhub.authservice.dto.request.ValidateTokenRequest;
import com.cimhub.authservice.dto.response.LoginResponse;
import com.cimhub.authservice.dto.response.ResetPasswordResponse;
import com.cimhub.authservice.dto.response.SignUpResponse;
import com.cimhub.authservice.dto.response.ValidateTokenResponse;
import com.cimhub.authservice.model.User;
import com.cimhub.authservice.repository.AuthUserRepository;
import com.cimhub.authservice.security.JwtService;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.common.exception.UserAlreadyExistsException;
import com.cimhub.common.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private AuthUserRepository authUserRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserServiceClient userServiceClient;
    @Mock private AuditService auditService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                jwtService,
                authenticationManager,
                authUserRepository,
                passwordEncoder,
                userServiceClient,
                auditService);
    }

    // ── registerUser ──────────────────────────────────────────────────────────

    @Test
    void registerUser_withNewEmail_shouldSaveUserAndReturnToken() {
        RegisterUserRequest request = new RegisterUserRequest(
                "John", "Driver", "John.Driver@Example.com",
                "Password123!", UserType.DRIVER, "+49 123", 30, "LIC-001");

        when(authUserRepository.findByEmail("john.driver@example.com")).thenReturn(Optional.empty());
        when(userServiceClient.profileExists("john.driver@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed");
        when(authUserRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(), anyString())).thenReturn("jwt-token");

        SignUpResponse response = authService.registerUser(request);

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
    void registerUser_emailIsNormalizedToLowercase() {
        RegisterUserRequest request = new RegisterUserRequest(
                "Jane", "P", "Jane.P@Example.COM",
                "Pw123!", UserType.PASSENGER, "+1", 25, null);

        when(authUserRepository.findByEmail("jane.p@example.com")).thenReturn(Optional.empty());
        when(userServiceClient.profileExists("jane.p@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(authUserRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(), anyString())).thenReturn("token");

        SignUpResponse response = authService.registerUser(request);

        assertEquals("jane.p@example.com", response.email());
    }

    @Test
    void registerUser_whenEmailAlreadyInAuthRepo_shouldThrowUserAlreadyExists() {
        RegisterUserRequest request = new RegisterUserRequest(
                "John", "Driver", "existing@example.com",
                "Password123!", UserType.DRIVER, "+49 123", 30, null);

        when(authUserRepository.findByEmail("existing@example.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerUser(request));
        verify(authUserRepository, never()).save(any());
    }

    @Test
    void registerUser_whenProfileExistsInUserService_shouldThrowUserAlreadyExists() {
        RegisterUserRequest request = new RegisterUserRequest(
                "John", "Driver", "profile@example.com",
                "Password123!", UserType.DRIVER, "+49 123", 30, null);

        when(authUserRepository.findByEmail("profile@example.com")).thenReturn(Optional.empty());
        when(userServiceClient.profileExists("profile@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerUser(request));
        verify(authUserRepository, never()).save(any());
    }

    @Test
    void registerUser_whenProfileCreationFails_shouldRollbackAuthUserAndRethrow() {
        RegisterUserRequest request = new RegisterUserRequest(
                "John", "Driver", "rollback@example.com",
                "Password123!", UserType.DRIVER, "+49 123", 30, null);

        when(authUserRepository.findByEmail("rollback@example.com")).thenReturn(Optional.empty());
        when(userServiceClient.profileExists("rollback@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(authUserRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("user-service down")).when(userServiceClient).createProfile(any());

        assertThrows(RuntimeException.class, () -> authService.registerUser(request));

        // Auth record must be deleted to keep stores consistent
        verify(authUserRepository).deleteById("rollback@example.com");
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_withValidCredentials_shouldReturnToken() {
        User user = new User();
        user.setEmail("driver@example.com");
        user.setPassword("hashed");
        user.setUserType(UserType.DRIVER);

        when(authUserRepository.findByEmail("driver@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(), anyString())).thenReturn("valid-token");

        LoginResponse response = authService.login(new LoginRequest("driver@example.com", "Password123!"));

        assertEquals("valid-token", response.token());
        assertEquals("driver@example.com", response.email());
        assertEquals("DRIVER", response.role());
    }

    @Test
    void login_emailIsNormalizedToLowercase() {
        User user = new User();
        user.setEmail("driver@example.com");
        user.setPassword("hashed");
        user.setUserType(UserType.DRIVER);

        when(authUserRepository.findByEmail("driver@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(), anyString())).thenReturn("token");

        // Passes mixed-case email
        LoginResponse response = authService.login(new LoginRequest("Driver@Example.COM", "Password123!"));

        assertEquals("driver@example.com", response.email());
    }

    @Test
    void login_withWrongCredentials_shouldThrowUnauthorized() {
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThrows(UnauthorizedException.class,
                () -> authService.login(new LoginRequest("bad@example.com", "wrong")));

        verify(jwtService, never()).generateToken(any(), anyString());
    }

    // ── changePassword ────────────────────────────────────────────────────────

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

        ResetPasswordResponse response = authService.changePassword(
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
                () -> authService.changePassword("user@example.com",
                        new ChangePasswordRequest("WrongPass", "NewPass")));

        verify(authUserRepository, never()).save(any());
    }

    @Test
    void changePassword_whenUserNotFound_shouldThrowUnauthorized() {
        when(authUserRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class,
                () -> authService.changePassword("ghost@example.com",
                        new ChangePasswordRequest("any", "any")));
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
        assertNotNull(response.email());
    }
}
