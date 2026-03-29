package com.cimhub.authservice.service;

import com.cimhub.authservice.dto.request.ChangePasswordRequest;
import com.cimhub.authservice.dto.request.ConfirmResetPasswordRequest;
import com.cimhub.authservice.dto.request.CreateProfileRequest;
import com.cimhub.authservice.dto.request.LoginRequest;
import com.cimhub.authservice.dto.request.RegisterUserRequest;
import com.cimhub.authservice.dto.request.ResetPasswordRequest;
import com.cimhub.authservice.dto.request.ValidateTokenRequest;
import com.cimhub.authservice.dto.response.LoginResponse;
import com.cimhub.authservice.dto.response.ResetPasswordResponse;
import com.cimhub.authservice.dto.response.SignUpResponse;
import com.cimhub.authservice.dto.response.ValidateTokenResponse;
import com.cimhub.authservice.model.User;
import com.cimhub.authservice.repository.AuthUserRepository;
import com.cimhub.authservice.security.JwtService;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.common.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

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

        if (authUserRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new UserAlreadyExistsException(
                    "User Already Exists with email: " + normalizedEmail);
        }

        if (userServiceClient.profileExists(normalizedEmail)) {
            log.warn("User profile already exists in user-service for email: {}", normalizedEmail);
            throw new UserAlreadyExistsException(
                    "User profile already exists: " + normalizedEmail);
        }

        User newAuthUser = new User();
        newAuthUser.setUserId(normalizedEmail);
        newAuthUser.setEmail(normalizedEmail);
        newAuthUser.setPassword(passwordEncoder.encode(request.password()));
        newAuthUser.setUserType(request.userType());
        newAuthUser.setCreatedDate(new Date());
        authUserRepository.save(newAuthUser);

        try {
            CreateProfileRequest profileRequest = CreateProfileRequest.builder()
                    .userId(normalizedEmail)
                    .email(normalizedEmail)
                    .firstName(request.firstName())
                    .lastName(request.lastName())
                    .userType(request.userType())
                    .phoneNumber(request.phoneNumber())
                    .age(request.age())
                    .licenseNumber(request.licenseNumber())
                    .build();

            userServiceClient.createProfile(profileRequest);
        } catch (Exception e) {
            log.error("Failed to create profile in user-service, rolling back auth user: {}", normalizedEmail, e);
            authUserRepository.deleteById(normalizedEmail);
            throw new RuntimeException("Registration failed: unable to create user profile", e);
        }

        String token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(newAuthUser.getEmail())
                        .password(newAuthUser.getPassword())
                        .roles("USER")
                        .build(),
                newAuthUser.getUserType().name());

        return SignUpResponse.builder()
                .token(token)
                .email(newAuthUser.getEmail())
                .role(newAuthUser.getUserType().name())
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.email().toLowerCase();
        log.info("Login attempt for user: {}", normalizedEmail);

        try {
            authenticationManager.authenticate(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            normalizedEmail,
                            request.password()));
        } catch (Exception e) {
            log.warn("Login failed for user: {}", normalizedEmail);
            throw new UnauthorizedException("Invalid credentials");
        }

        User authUserData = authUserRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(authUserData.getEmail())
                .password(authUserData.getPassword())
                .roles("USER")
                .build();

        String token = jwtService.generateToken(userDetails, authUserData.getUserType().name());

        return new LoginResponse(token, authUserData.getEmail(), authUserData.getUserType().name());
    }

    public ValidateTokenResponse validateToken(ValidateTokenRequest request) {
        try {
            String email = jwtService.extractUsername(request.token());
            boolean isTokenValid = jwtService.isTokenValid(request.token(), email);
            String role = jwtService.extractRole(request.token());

            return new ValidateTokenResponse(isTokenValid, email, role);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return new ValidateTokenResponse(false, null, null);
        }
    }

    public ResetPasswordResponse initiatePasswordReset(ResetPasswordRequest request) {
        String normalizedEmail = request.email().toLowerCase();
        log.info("Password reset initiated for user: {}", normalizedEmail);

        authUserRepository.findByEmail(normalizedEmail).ifPresentOrElse(
                user -> {
                    String resetToken = UUID.randomUUID().toString();
                    Date expiryDate = new Date(System.currentTimeMillis() + 3600000);

                    user.setResetToken(resetToken);
                    user.setResetTokenExpiry(expiryDate);
                    authUserRepository.save(user);

                    log.info("Reset token generated for user: {}", normalizedEmail);
                },
                () -> log.warn("Password reset requested for non-existent user: {}", normalizedEmail)
        );

        return new ResetPasswordResponse("If the email exists, a reset link has been sent");
    }

    public ResetPasswordResponse confirmPasswordReset(ConfirmResetPasswordRequest request) {
        log.info("Password reset confirmation received");

        User user = authUserRepository.findByResetToken(request.token())
                .orElseThrow(() -> {
                    log.warn("Invalid reset token");
                    return new UnauthorizedException("Invalid or expired reset token");
                });

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().before(new Date())) {
            log.warn("Expired reset token for user: {}", user.getEmail());
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            authUserRepository.save(user);
            throw new UnauthorizedException("Invalid or expired reset token");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        authUserRepository.save(user);

        log.info("Password successfully reset for user: {}", user.getEmail());

        return new ResetPasswordResponse("Password has been reset successfully");
    }

    public ResetPasswordResponse changePassword(String userEmail, ChangePasswordRequest request) {
        User user = authUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        authUserRepository.save(user);

        log.info("Password changed for user: {}", userEmail);

        return new ResetPasswordResponse("Password has been changed successfully");
    }
}
