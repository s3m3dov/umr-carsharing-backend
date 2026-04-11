package com.cimhub.authservice.service;

import com.cimhub.authservice.dto.request.RegisterUserRequest;
import com.cimhub.authservice.dto.response.SignUpResponse;
import com.cimhub.authservice.model.User;
import com.cimhub.authservice.repository.AuthUserRepository;
import com.cimhub.authservice.security.JwtService;
import com.cimhub.authservice.security.UserDetailsBuilder;
import com.cimhub.authservice.util.EmailNormalizer;
import com.cimhub.authservice.util.PasswordValidator;
import com.cimhub.common.enums.EntityType;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.BadRequestException;
import com.cimhub.common.exception.UserAlreadyExistsException;
import com.cimhub.common.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final AuthUserRepository authUserRepository;
    private final UserServiceClient userServiceClient;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public SignUpResponse registerUser(RegisterUserRequest request) {
        String normalizedEmail = EmailNormalizer.normalize(request.email());
        log.info("Registering new user: {}", normalizedEmail);

        validatePasswordStrength(request.password());
        validateLicenseNumber(request);

        if (authUserRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new UserAlreadyExistsException("User Already Exists with email: " + normalizedEmail);
        }

        User newAuthUser = createUser(normalizedEmail, request);
        authUserRepository.save(newAuthUser);
        auditService.logCreate(EntityType.USER, normalizedEmail, normalizedEmail, "User registered");

        createSubProfile(request, normalizedEmail);

        return generateSignupResponse(newAuthUser);
    }

    private void validatePasswordStrength(String password) {
        PasswordValidator.ValidationResult result = PasswordValidator.validate(password);
        if (!result.valid()) {
            throw new BadRequestException(result.message());
        }
    }

    private void validateLicenseNumber(RegisterUserRequest request) {
        if (request.userType() == UserType.DRIVER && 
                (request.licenseNumber() == null || request.licenseNumber().isBlank())) {
            throw new BadRequestException("License number is required for DRIVER users");
        }
    }

    private User createUser(String normalizedEmail, RegisterUserRequest request) {
        User user = new User();
        user.setUserId(normalizedEmail);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setUserType(request.userType());
        Instant now = Instant.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return user;
    }

    private void createSubProfile(RegisterUserRequest request, String normalizedEmail) {
        try {
            if (request.userType() == UserType.PASSENGER) {
                userServiceClient.createPassengerSubProfile(
                        normalizedEmail,
                        request.firstName(),
                        request.lastName(),
                        request.phoneNumber(),
                        request.age() != null ? request.age() : 0);
            } else if (request.userType() == UserType.DRIVER) {
                userServiceClient.createDriverSubProfile(
                        normalizedEmail,
                        request.firstName(),
                        request.lastName(),
                        request.phoneNumber(),
                        request.age() != null ? request.age() : 0,
                        request.licenseNumber());
            }
        } catch (Exception e) {
            log.error("Failed to create sub-profile in user-service, rolling back user: {}", normalizedEmail, e);
            authUserRepository.deleteById(normalizedEmail);
            auditService.logDelete(EntityType.USER, normalizedEmail, normalizedEmail, 
                    "User rollback after sub-profile creation failure");
            throw new RuntimeException("Registration failed: unable to create user sub-profile", e);
        }
    }

    private SignUpResponse generateSignupResponse(User user) {
        String token = jwtService.generateTokenWithRole(
                UserDetailsBuilder.fromUser(user),
                user.getUserType().name());

        return SignUpResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getUserType().name())
                .build();
    }
}