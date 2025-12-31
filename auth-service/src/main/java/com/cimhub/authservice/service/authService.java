package com.cimhub.authservice.service;

import com.cimhub.authservice.client.UserServiceClient;
import com.cimhub.authservice.dto.request.CreateProfileRequest;
import com.cimhub.authservice.dto.request.LoginRequest;
import com.cimhub.authservice.dto.request.RegisterUserRequest;
import com.cimhub.authservice.dto.request.ValidateTokenRequest;
import com.cimhub.authservice.dto.response.LoginResponse;
import com.cimhub.authservice.dto.response.SignUpResponse;
import com.cimhub.authservice.dto.response.ValidateTokenResponse;
import com.cimhub.authservice.model.User;
import com.cimhub.authservice.repository.AuthUserRepository;
import com.cimhub.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserServiceClient userServiceClient;

    public SignUpResponse registerUser(RegisterUserRequest request) {
        log.info("Registering new user: {}", request.emailId());

        if (authUserRepository.findByEmailId(request.emailId()) != null) {
            throw new com.cimhub.authservice.exception.UserAlreadyExistsException(
                    "User Already Exists with emailId: " + request.emailId());
        }

        User newUser = new User();
        newUser.setUserId(request.emailId().toLowerCase());
        newUser.setEmailId(request.emailId());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setCreatedDate(new Date());
        authUserRepository.save(newUser);

        CreateProfileRequest profileRequest = CreateProfileRequest.builder()
                .userId(newUser.getUserId())
                .emailId(newUser.getEmailId())
                .fullName(request.fullName())
                .phoneNumber(request.phoneNumber())
                .age(request.age())
                .dob(request.dob())
                .empId(request.empId())
                .organisationName(request.organisationName())
                .profilePicUrl(request.profilePicUrl())
                .build();

        try {
            userServiceClient.createUserProfile(profileRequest);
        } catch (Exception e) {
            log.error("Failed to create user profile for {}. Rolling back auth user.", request.emailId(), e);
            authUserRepository.delete(newUser);
            throw new RuntimeException("Failed to create user profile: " + e.getMessage());
        }

        String token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(newUser.getEmailId())
                        .password(newUser.getPassword())
                        .roles("USER")
                        .build());

        return SignUpResponse.builder()
                .token(token)
                .emailId(newUser.getEmailId())
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        log.info("Login check for user: {}", request.emailId());

        try {
            authenticationManager.authenticate(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            request.emailId(),
                            request.password()));
        } catch (Exception e) {
            log.warn("Login failed for user {}: {}", request.emailId(), e.getMessage());
            throw new RuntimeException("Invalid Credentials");
        }

        User userData = authUserRepository.findByEmailId(request.emailId().toLowerCase());

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(userData.getEmailId())
                .password(userData.getPassword())
                .roles("USER")
                .build();

        String token = jwtService.generateToken(userDetails);

        return new LoginResponse(token, userData.getEmailId());
    }

    public ValidateTokenResponse validateToken(ValidateTokenRequest request) {
        try {
            String emailId = jwtService.extractUsername(request.token());
            boolean isTokenValid = jwtService.isTokenValid(request.token(), emailId);

            return new ValidateTokenResponse(isTokenValid, emailId);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return new ValidateTokenResponse(false, null);
        }
    }
}
