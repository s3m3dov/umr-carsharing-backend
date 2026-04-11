package com.cimhub.authservice.service;

import com.cimhub.authservice.dto.request.LoginRequest;
import com.cimhub.authservice.dto.request.ValidateTokenRequest;
import com.cimhub.authservice.dto.response.LoginResponse;
import com.cimhub.authservice.dto.response.ValidateTokenResponse;
import com.cimhub.authservice.model.User;
import com.cimhub.authservice.repository.AuthUserRepository;
import com.cimhub.authservice.security.JwtService;
import com.cimhub.authservice.security.UserDetailsBuilder;
import com.cimhub.authservice.util.EmailNormalizer;
import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.common.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthUserRepository authUserRepository;
    private final AuditService auditService;

    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = EmailNormalizer.normalize(request.email());
        log.info("Login attempt for user: {}", normalizedEmail);

        try {
            authenticationManager.authenticate(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            normalizedEmail, request.password()));
        } catch (Exception e) {
            log.warn("Login failed for user: {}", normalizedEmail);
            auditService.logLogin(EntityType.USER, normalizedEmail, normalizedEmail, "User login failed");
            throw new UnauthorizedException("Invalid credentials");
        }

        User authUserData = authUserRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        UserDetails userDetails = UserDetailsBuilder.fromUserWithType(authUserData);

        String token = jwtService.generateTokenWithRole(
                userDetails, authUserData.getUserType().name());
        auditService.logLogin(EntityType.USER, authUserData.getUserId(), normalizedEmail, "User login successful");

        return new LoginResponse(
                token, authUserData.getEmail(), authUserData.getUserType().name());
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
}
