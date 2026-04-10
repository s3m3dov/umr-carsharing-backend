package com.cimhub.userservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile read APIs")
public class UserController {

    private final UserService userService;

    @GetMapping("/health")
    public ResponseEntity<ApiResponseWrapper<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponseWrapper.success("User Service is up and running!"));
    }

    @Operation(
            summary = "Get user information",
            description = "Returns aggregated user info from role profiles (driver or passenger)."
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
            @Parameter(description = "User's unique identifier", required = true, example = "john.driver@example.com")
            @PathVariable String userId) {
        log.info("Get user info for userId: {}", userId);
        UserResponse userInfo = userService.getUserInfo(userId);
        return ResponseEntity.ok(ApiResponseWrapper.success(userInfo));
    }
}
