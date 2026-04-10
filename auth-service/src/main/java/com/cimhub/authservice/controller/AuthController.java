package com.cimhub.authservice.controller;

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
import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.exception.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Management", description = "User registration, login, and token validation APIs. Handles all authentication flows including signup, login, and JWT token validation.")
public class AuthController {

    private final AuthService authService;
    private final PasswordManagementService passwordManagementService;

    @Operation(
            summary = "Register new user",
            description = "Creates a new user account and returns JWT token. User type (DRIVER or PASSENGER) determines role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User successfully registered",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = AuthApiExamples.SIGNUP_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input - missing fields or invalid email",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = AuthApiExamples.ERROR_VALIDATION))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User with this email already exists",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = AuthApiExamples.ERROR_USER_EXISTS))
            )
    })
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseWrapper<SignUpResponse>> registerUser(
            @Valid @RequestBody RegisterUserRequest request) {
        log.info("Signup request for user: {}", request.email());
        SignUpResponse response = authService.registerUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseWrapper.success(response));
    }

    @Operation(
            summary = "User login",
            description = "Authenticates a user with email and password. Returns JWT token for subsequent requests."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = AuthApiExamples.LOGIN_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid email or password",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = AuthApiExamples.ERROR_INVALID_CREDENTIALS))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Missing required fields",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = AuthApiExamples.ERROR_VALIDATION))
            )
    })
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseWrapper<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Login request for user: {}", request.email());
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }

    @Operation(
            summary = "Validate token",
            description = "Validates a JWT token and returns user information. Used by API Gateway and services."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token is valid",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = AuthApiExamples.VALIDATE_TOKEN_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token is invalid or expired",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = AuthApiExamples.ERROR_INVALID_TOKEN))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Token field missing",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = AuthApiExamples.ERROR_VALIDATION))
            )
    })
    @PostMapping(value = "/validate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseWrapper<ValidateTokenResponse>> validateToken(
            @Valid @RequestBody ValidateTokenRequest request) {
        log.info("Token validation request");
        ValidateTokenResponse response = authService.validateToken(request);
        if (!response.isValid()) {
            throw new UnauthorizedException("Invalid or expired token");
        }
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }

    @Operation(
            summary = "Initiate password reset",
            description = "Sends a password reset token to the user's email"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reset token sent if email exists"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid email format"
            )
    })
    @PostMapping(value = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseWrapper<ResetPasswordResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        log.info("Password reset request for user: {}", request.email());
        ResetPasswordResponse response = passwordManagementService.initiatePasswordReset(request);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }

    @Operation(
            summary = "Confirm password reset",
            description = "Resets the user's password using the reset token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password reset successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input - missing fields"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired reset token"
            )
    })
    @PostMapping(value = "/reset-password/confirm", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseWrapper<ResetPasswordResponse>> confirmResetPassword(
            @Valid @RequestBody ConfirmResetPasswordRequest request) {
        log.info("Password reset confirmation request");
        ResetPasswordResponse response = passwordManagementService.confirmPasswordReset(request);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }

    @Operation(
            summary = "Change password",
            description = "Changes the password for the currently authenticated user. Requires the current password for verification."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password changed successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Current password is incorrect or user not authenticated"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input - missing fields or password too short"
            )
    })
    @PutMapping(value = "/change-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseWrapper<ResetPasswordResponse>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Password change request for user: {}", userDetails.getUsername());
        ResetPasswordResponse response = passwordManagementService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }
}
