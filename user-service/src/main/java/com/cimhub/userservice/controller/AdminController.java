package com.cimhub.userservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.BadRequestException;
import com.cimhub.common.util.AdminGuard;
import com.cimhub.userservice.dto.response.AdminProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@ApiResponse(responseCode = "403", description = "Forbidden - User is not an admin", content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
public class AdminController {

    @Operation(summary = "Get admin profile", description = "Returns the authenticated admin profile derived from gateway headers")
    @ApiResponse(responseCode = "200", description = "Admin profile returned")
    @ApiResponse(responseCode = "400", description = "Missing or invalid admin email", content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
    @GetMapping("/profile")
    public ResponseEntity<ApiResponseWrapper<AdminProfileResponse>> getAdminProfile(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Admin email from JWT token", required = true) @RequestHeader("X-User-Email") String adminEmail) {
        AdminGuard.requireAdmin(userRole);
        if (adminEmail == null || adminEmail.isBlank()) {
            throw new BadRequestException("Admin email is required");
        }

        log.info("Returning admin profile for: {}", adminEmail);
        AdminProfileResponse response = new AdminProfileResponse(adminEmail, UserType.ADMIN);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }
}
