package com.cimhub.authservice.service;

import com.cimhub.authservice.client.UserServiceClient;
import com.cimhub.authservice.dto.request.CreateProfileRequest;
import com.cimhub.authservice.dto.request.LoginRequest;
import com.cimhub.authservice.dto.request.RegisterUserRequest;
import com.cimhub.authservice.dto.request.ValidateTokenRequest;
import com.cimhub.authservice.dto.response.LoginResponse;
import com.cimhub.authservice.dto.response.SignUpResponse;
import com.cimhub.authservice.dto.response.ValidateTokenResponse;
import com.cimhub.authservice.exception.UserAlreadyExistsException;
import com.cimhub.authservice.model.User;
import com.cimhub.authservice.repository.AuthUserRepository;
import com.cimhub.authservice.security.JwtService;
import com.cimhub.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private AuthService authService;

    private RegisterUserRequest registerRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterUserRequest(
                "test@example.com",
                "password123",
                "Test User",
                "+1234567890",
                25,
                new Date(),
                "EMP001",
                "Test Org",
                "https://example.com/pic.jpg");

        testUser = new User();
        testUser.setUserId("test@example.com");
        testUser.setEmailId("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setCreatedDate(new Date());
    }

    @Test
    void registerUser_whenValidRequest_thenSuccessfullyRegistersUser() {
        // Given
        when(authUserRepository.findByEmailId(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");
        doNothing().when(userServiceClient).createUserProfile(any(CreateProfileRequest.class));

        // When
        SignUpResponse response = authService.registerUser(registerRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.emailId()).isEqualTo("test@example.com");

        verify(authUserRepository).save(any(User.class));
        verify(userServiceClient).createUserProfile(any(CreateProfileRequest.class));
        verify(jwtService).generateToken(any(UserDetails.class));
    }

    @Test
    void registerUser_whenUserAlreadyExists_thenThrowsException() {
        // Given
        when(authUserRepository.findByEmailId(anyString())).thenReturn(testUser);

        // When & Then
        assertThatThrownBy(() -> authService.registerUser(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User Already Exists");

        verify(authUserRepository, never()).save(any(User.class));
        verify(userServiceClient, never()).createUserProfile(any(CreateProfileRequest.class));
    }

    @Test
    void registerUser_whenProfileCreationFails_thenRollbackAndThrowException() {
        // Given
        when(authUserRepository.findByEmailId(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        doThrow(new RuntimeException("Profile service unavailable"))
                .when(userServiceClient).createUserProfile(any(CreateProfileRequest.class));

        // When & Then
        assertThatThrownBy(() -> authService.registerUser(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create user profile");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(authUserRepository).save(userCaptor.capture());
        verify(authUserRepository).delete(userCaptor.getValue());
    }

    @Test
    void registerUser_whenCalled_thenCreatesProfileWithCorrectData() {
        // Given
        when(authUserRepository.findByEmailId(anyString())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        ArgumentCaptor<CreateProfileRequest> profileCaptor = ArgumentCaptor.forClass(CreateProfileRequest.class);

        // When
        authService.registerUser(registerRequest);

        // Then
        verify(userServiceClient).createUserProfile(profileCaptor.capture());
        CreateProfileRequest capturedRequest = profileCaptor.getValue();

        assertThat(capturedRequest.emailId()).isEqualTo("test@example.com");
        assertThat(capturedRequest.fullName()).isEqualTo("Test User");
        assertThat(capturedRequest.phoneNumber()).isEqualTo("+1234567890");
        assertThat(capturedRequest.age()).isEqualTo(25);
    }

    @Test
    void login_whenValidCredentials_thenReturnsTokenAndEmail() {
        // Given
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(authUserRepository.findByEmailId(anyString())).thenReturn(testUser);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.emailId()).isEqualTo("test@example.com");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(any(UserDetails.class));
    }

    @Test
    void login_whenInvalidCredentials_thenThrowsBusinessException() {
        // Given
        LoginRequest loginRequest = new LoginRequest("test@example.com", "wrongPassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid credentials");

        verify(authUserRepository, never()).findByEmailId(anyString());
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }

    @Test
    void validateToken_whenValidToken_thenReturnsValidResponse() {
        // Given
        ValidateTokenRequest request = new ValidateTokenRequest("valid-jwt-token");
        when(jwtService.extractUsername(anyString())).thenReturn("test@example.com");
        when(jwtService.isTokenValid(anyString(), anyString())).thenReturn(true);

        // When
        ValidateTokenResponse response = authService.validateToken(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isValid()).isTrue();
        assertThat(response.emailId()).isEqualTo("test@example.com");

        verify(jwtService).extractUsername("valid-jwt-token");
        verify(jwtService).isTokenValid("valid-jwt-token", "test@example.com");
    }

    @Test
    void validateToken_whenInvalidToken_thenReturnsInvalidResponse() {
        // Given
        ValidateTokenRequest request = new ValidateTokenRequest("invalid-jwt-token");
        when(jwtService.extractUsername(anyString())).thenThrow(new RuntimeException("Invalid token"));

        // When
        ValidateTokenResponse response = authService.validateToken(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isValid()).isFalse();
        assertThat(response.emailId()).isNull();
    }

    @Test
    void validateToken_whenTokenExpired_thenReturnsInvalidResponse() {
        // Given
        ValidateTokenRequest request = new ValidateTokenRequest("expired-jwt-token");
        when(jwtService.extractUsername(anyString())).thenReturn("test@example.com");
        when(jwtService.isTokenValid(anyString(), anyString())).thenReturn(false);

        // When
        ValidateTokenResponse response = authService.validateToken(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isValid()).isFalse();
        assertThat(response.emailId()).isEqualTo("test@example.com");
    }
}
