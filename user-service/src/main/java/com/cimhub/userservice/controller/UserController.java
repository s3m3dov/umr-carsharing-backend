package com.cimhub.userservice.controller;

import com.cimhub.common.dto.ApiResponse;
import com.cimhub.userservice.dto.LoginRequestDTO;
import com.cimhub.userservice.dto.LoginResponseDTO;
import com.cimhub.userservice.dto.SignUpResponseDTO;
import com.cimhub.userservice.dto.UserInfoDTO;
import com.cimhub.userservice.dto.UserProfileDTO;
import com.cimhub.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "User registration, authentication, and profile management APIs")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("User Service is up and running!"));
    }

    @Operation(summary = "Get user information", description = "Retrieves user profile information by user ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping(value = "/{userId}")
    public ResponseEntity<ApiResponse<UserInfoDTO>> getUserInfo(
            @Parameter(description = "User's unique identifier", required = true) @PathVariable String userId) {
        log.info("Get user info for userId: {}", userId);
        UserInfoDTO userInfo = userService.getUserInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }

    @Operation(summary = "Register new user", description = "Creates a new user account in the system")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
    })
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<SignUpResponseDTO>> addNewUser(
            @Valid @RequestBody UserInfoDTO userInfoDTO) {
        log.info("Signup request for email: {}", userInfoDTO.getEmailId());
        SignUpResponseDTO signUpResponse = userService.registerNewUser(userInfoDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(signUpResponse));
    }

    @Operation(summary = "User login", description = "Authenticates a user and returns login details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<LoginResponseDTO>> loginUser(
            @Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Login request for email: {}", loginRequest.getEmailId());
        LoginResponseDTO loginResponse = userService.checkUserLogin(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }

    @Operation(summary = "Create user profile", description = "Creates a detailed user profile")
    @PostMapping(value = "/profile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<String>> createUserProfile(@Valid @RequestBody UserProfileDTO profileDTO) {
        log.info("Create user profile for userId: {}", profileDTO.getUserId());
        userService.createUserProfile(profileDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Profile created successfully"));
    }
}