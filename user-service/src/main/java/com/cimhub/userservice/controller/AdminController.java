package com.cimhub.userservice.controller;

import com.cimhub.common.constant.HeaderConstants;
import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.BadRequestException;
import com.cimhub.common.util.AdminGuard;
import com.cimhub.userservice.dto.examples.UserApiDoc;
import com.cimhub.userservice.dto.response.AdminProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin - Profile", description = "Admin self profile endpoints")
@ApiResponses({
    @ApiResponse(
            responseCode = "403",
            description = "Forbidden - User is not an admin",
            content =
                    @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = UserApiDoc.ERROR_ADMIN_UNAUTHORIZED))),
    @ApiResponse(
            responseCode = "400",
            description = "Missing or invalid admin email",
            content =
                    @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = UserApiDoc.ERROR_INVALID_REQUEST)))
})
public class AdminController {

    @Operation(
            summary = "Get admin profile",
            description = "Returns the authenticated admin profile derived from gateway headers")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Admin profile returned",
                content =
                        @Content(
                                mediaType = "application/json",
                                examples = @ExampleObject(value = UserApiDoc.ADMIN_PROFILE_RESPONSE)))
    })
    @GetMapping("/profile")
    public ResponseEntity<ApiResponseWrapper<AdminProfileResponse>> getAdminProfile(
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true)
                    UserType userRole,
            @Parameter(
                            description = "Injected by the gateway from the JWT. Do not set manually.",
                            required = false,
                            hidden = true)
                    @RequestHeader(value = HeaderConstants.X_USER_EMAIL, required = true)
                    String adminEmail) {
        AdminGuard.requireAdmin(userRole);
        if (adminEmail == null || adminEmail.isBlank()) {
            throw new BadRequestException("Admin email is required");
        }

        log.info("Returning admin profile for: {}", adminEmail);
        AdminProfileResponse response = new AdminProfileResponse(adminEmail, UserType.ADMIN);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }
}
