package com.cimhub.authservice.service;

import com.cimhub.authservice.config.TokenProperties;
import com.cimhub.authservice.dto.request.ChangePasswordRequest;
import com.cimhub.authservice.dto.request.ConfirmResetPasswordRequest;
import com.cimhub.authservice.dto.request.ResetPasswordRequest;
import com.cimhub.authservice.dto.response.ResetPasswordResponse;
import com.cimhub.authservice.model.User;
import com.cimhub.authservice.repository.AuthUserRepository;
import com.cimhub.authservice.security.TokenHasher;
import com.cimhub.authservice.util.EmailNormalizer;
import com.cimhub.authservice.util.PasswordValidator;
import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.BadRequestException;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.common.service.AuditService;
import java.time.Instant;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordManagementService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final TokenProperties tokenProperties;

    public ResetPasswordResponse initiatePasswordReset(ResetPasswordRequest request) {
        String normalizedEmail = EmailNormalizer.normalize(request.email());
        log.info("Password reset initiated for user: {}", normalizedEmail);

        authUserRepository
                .findByEmail(normalizedEmail)
                .ifPresentOrElse(
                        user -> {
                            String rawToken = TokenHasher.generateSecureToken();
                            String hashedToken = TokenHasher.hashToken(rawToken);
                            Date expiryDate =
                                    new Date(System.currentTimeMillis() + tokenProperties.getResetTokenExpiryMs());

                            user.setResetToken(rawToken);
                            user.setResetTokenHash(hashedToken);
                            user.setResetTokenExpiry(expiryDate);
                            user.setUpdatedAt(Instant.now());
                            authUserRepository.save(user);

                            auditService.logUpdate(
                                    EntityType.USER,
                                    user.getUserId(),
                                    normalizedEmail,
                                    "Password reset token generated");

                            log.info("Reset token generated for user: {}", normalizedEmail);
                        },
                        () -> log.warn("Password reset requested for non-existent user: {}", normalizedEmail));

        return new ResetPasswordResponse("If the email exists, a reset link has been sent");
    }

    public ResetPasswordResponse confirmPasswordReset(ConfirmResetPasswordRequest request) {
        log.info("Password reset confirmation received");

        User user = authUserRepository
                .findByResetTokenHash(TokenHasher.hashToken(request.token()))
                .orElseThrow(() -> {
                    log.warn("Invalid reset token");
                    return new UnauthorizedException("Invalid or expired reset token");
                });

        if (!TokenHasher.verifyToken(request.token(), user.getResetTokenHash())) {
            log.warn("Expired reset token for user: {}", user.getEmail());
            clearResetToken(user);
            throw new UnauthorizedException("Invalid or expired reset token");
        }

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().before(new Date())) {
            log.warn("Expired reset token for user: {}", user.getEmail());
            clearResetToken(user);
            throw new UnauthorizedException("Invalid or expired reset token");
        }

        validatePasswordStrength(request.newPassword());

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        clearResetToken(user);
        user.setUpdatedAt(Instant.now());
        authUserRepository.save(user);

        auditService.logUpdate(EntityType.USER, user.getUserId(), user.getEmail(), "Password reset completed");

        log.info("Password successfully reset for user: {}", user.getEmail());

        return new ResetPasswordResponse("Password has been reset successfully");
    }

    public ResetPasswordResponse changePassword(String userEmail, ChangePasswordRequest request) {
        String normalizedEmail = EmailNormalizer.normalize(userEmail);

        User user = authUserRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        validatePasswordStrength(request.newPassword());

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(Instant.now());
        authUserRepository.save(user);

        auditService.logUpdate(EntityType.USER, user.getUserId(), normalizedEmail, "Password changed by user");

        log.info("Password changed for user: {}", normalizedEmail);

        return new ResetPasswordResponse("Password has been changed successfully");
    }

    private void validatePasswordStrength(String password) {
        PasswordValidator.ValidationResult result = PasswordValidator.validate(password);
        if (!result.valid()) {
            throw new BadRequestException(result.message());
        }
    }

    private void clearResetToken(User user) {
        user.setResetToken(null);
        user.setResetTokenHash(null);
        user.setResetTokenExpiry(null);
    }
}
