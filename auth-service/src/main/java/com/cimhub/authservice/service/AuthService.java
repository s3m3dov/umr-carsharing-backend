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
import com.cimhub.common.exception.UserAlreadyExistsException;
import com.cimhub.common.exception.UnauthorizedException;
import feign.FeignException;
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
        String normalizedEmail = request.email().toLowerCase();
        log.info("Registering new user: {}", normalizedEmail);

        if (authUserRepository.findByEmail(normalizedEmail) != null) {
            throw new UserAlreadyExistsException(
                    "User Already Exists with email: " + normalizedEmail);
        }

        User newUser = new User();
        newUser.setUserId(normalizedEmail);
        newUser.setEmail(normalizedEmail);
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setCreatedDate(new Date());
        authUserRepository.save(newUser);

        CreateProfileRequest profileRequest = CreateProfileRequest.builder()
                .userId(newUser.getUserId())
                .email(newUser.getEmail())
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
        } catch (FeignException.Conflict e) {
            log.error("User profile already exists for {}. Rolling back auth user.", normalizedEmail);
            authUserRepository.delete(newUser);
            throw new UserAlreadyExistsException("User profile already exists for " + normalizedEmail);
        } catch (Exception e) {
            log.error("Failed to create user profile for {}. Rolling back auth user.", normalizedEmail, e);
            authUserRepository.delete(newUser);
            throw new RuntimeException("Failed to create user profile: " + e.getMessage());
        }

        String token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(newUser.getEmail())
                        .password(newUser.getPassword())
                        .roles("USER")
                        .build());

        return SignUpResponse.builder()
                .token(token)
                .email(newUser.getEmail())
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.email().toLowerCase();
        log.info("Login check for user: {}", normalizedEmail);

        try {
            authenticationManager.authenticate(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            normalizedEmail,
                            request.password()));
        } catch (Exception e) {
            log.warn("Login failed for user {}: {}", normalizedEmail, e.getMessage());
            throw new UnauthorizedException("Invalid credentials");
        }

        User userData = authUserRepository.findByEmail(normalizedEmail);

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(userData.getEmail())
                .password(userData.getPassword())
                .roles("USER")
                .build();

        String token = jwtService.generateToken(userDetails);

        return new LoginResponse(token, userData.getEmail());
    }

    public ValidateTokenResponse validateToken(ValidateTokenRequest request) {
        try {
            String email = jwtService.extractUsername(request.token());
            boolean isTokenValid = jwtService.isTokenValid(request.token(), email);

            return new ValidateTokenResponse(isTokenValid, email);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return new ValidateTokenResponse(false, null);
        }
    }
}
