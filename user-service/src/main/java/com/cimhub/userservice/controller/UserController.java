package com.cimhub.userservice.controller;

import com.cimhub.common.constant.HeaderConstants;
import com.cimhub.common.dto.response.ApiSuccess;
import com.cimhub.common.util.OwnershipGuard;
import com.cimhub.userservice.dto.examples.UserApiDoc;
import com.cimhub.userservice.dto.response.UserResponse;
import com.cimhub.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile read APIs")
public class UserController {

    private final UserService userService;

    @GetMapping("/health")
    public ResponseEntity<ApiSuccess<String>> healthCheck() {
        return ResponseEntity.ok(ApiSuccess.success("User Service is up and running!"));
    }

    @Operation(
            summary = "Get user information",
            description = "Returns aggregated user info from role profiles (driver or passenger).")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "User found and profile returned",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples = @ExampleObject(value = UserApiDoc.GET_USER_RESPONSE))),
                @ApiResponse(
                        responseCode = "404",
                        description = "User not found",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        examples = @ExampleObject(value = UserApiDoc.ERROR_USER_NOT_FOUND)))
            })
    @GetMapping(value = "/{userId}")
    public ResponseEntity<ApiSuccess<UserResponse>> getUserInfo(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ID, required = true)
                    String authenticatedUserId,
            @Parameter(description = "User's unique identifier", required = true, example = "john.driver@example.com")
                    @PathVariable
                    String userId) {
        OwnershipGuard.requireOwner(authenticatedUserId, userId);
        log.info("Get user info for userId: {}", userId);
        UserResponse userInfo = userService.getUserInfo(userId);
        return ResponseEntity.ok(ApiSuccess.success(userInfo));
    }
}
