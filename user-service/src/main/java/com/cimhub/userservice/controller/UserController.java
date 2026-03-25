package com.cimhub.userservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.userservice.dto.request.CreateProfileRequest;
import com.cimhub.userservice.dto.response.UserResponse;
import com.cimhub.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile management APIs. Handles user profile creation, retrieval, and updates. Users are created initially during authentication (signup).")
public class UserController {

        private final UserService userService;

        @GetMapping("/health")
        public ResponseEntity<ApiResponseWrapper<String>> healthCheck() {
                return ResponseEntity.ok(ApiResponseWrapper.success("User Service is up and running!"));
        }

        @Operation(
                summary = "Get user information",
                description = "Retrieves user profile including personal info and role."
        )
        @ApiResponses(value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "User found and profile returned",
                        content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UserApiExamples.GET_USER_RESPONSE))
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "User not found",
                        content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UserApiExamples.ERROR_USER_NOT_FOUND))
                )
        })
        @GetMapping(value = "/{userId}")
        public ResponseEntity<ApiResponseWrapper<UserResponse>> getUserInfo(
                @Parameter(description = "User's unique identifier", required = true, example = "user_123")
                @PathVariable String userId) {
                log.info("Get user info for userId: {}", userId);
                UserResponse userInfo = userService.getUserInfo(userId);
                return ResponseEntity.ok(ApiResponseWrapper.success(userInfo));
        }

        @Operation(
                summary = "Create user profile",
                description = "Creates or updates user profile with additional information. Called during signup to complete setup. NO NEED TO SEND SEPARATE REQUEST."
        )
        @ApiResponses(value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Profile created successfully",
                        content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UserApiExamples.CREATE_PROFILE_RESPONSE))
                ),
                @ApiResponse(
                        responseCode = "400",
                        description = "Invalid profile data",
                        content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UserApiExamples.ERROR_INVALID_DATA))
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "User not found",
                        content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UserApiExamples.ERROR_USER_NOT_FOUND))
                )
        })
        @PostMapping(value = "/profile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ApiResponseWrapper<UserResponse>> createProfile(
                @Valid @RequestBody CreateProfileRequest request) {
                log.info("Create user profile for userId: {}", request.userId());
                UserResponse userProfile = userService.createProfile(request);
                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(ApiResponseWrapper.success(userProfile));
        }
}
