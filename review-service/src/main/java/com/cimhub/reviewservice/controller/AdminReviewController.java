package com.cimhub.reviewservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.util.AdminGuard;
import com.cimhub.reviewservice.dto.response.ReviewResponse;
import com.cimhub.reviewservice.model.ReviewStatus;
import com.cimhub.reviewservice.service.AdminReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/reviews/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Review Management", description = "Admin endpoints for managing reviews")
@ApiResponse(responseCode = "403", description = "Forbidden - User is not an admin", content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
@ApiResponse(responseCode = "404", description = "Resource not found", content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @Operation(summary = "List all reviews", description = "Paginated list of reviews with optional status filter")
    @GetMapping
    public ResponseEntity<ApiResponseWrapper<Page<ReviewResponse>>> listReviews(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Filter by review status") @RequestParam(required = false) ReviewStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminReviewService.listReviews(status, pageable)));
    }

    @Operation(summary = "Get review detail", description = "Retrieve detailed information about a specific review")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<ReviewResponse>> getReview(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Review ID") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminReviewService.getReview(id)));
    }

    @Operation(summary = "Flag a review", description = "Flag a review for review by admin")
    @PostMapping("/{id}/flag")
    public ResponseEntity<ApiResponseWrapper<ReviewResponse>> flagReview(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Admin email performing the action") @RequestHeader("X-User-Email") String adminEmail,
            @Parameter(description = "Review ID") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminReviewService.flagReview(id, adminEmail)));
    }

    @Operation(summary = "Remove a review", description = "Remove/hide a review from public view")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<String>> removeReview(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Admin email performing the action") @RequestHeader("X-User-Email") String adminEmail,
            @Parameter(description = "Review ID") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        adminReviewService.removeReview(id, adminEmail);
        return ResponseEntity.ok(ApiResponseWrapper.success("Review removed"));
    }

    @Operation(summary = "Publish/restore a review", description = "Publish or restore a flagged review")
    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponseWrapper<ReviewResponse>> publishReview(
            @Parameter(description = "User role from JWT token", required = true) @RequestHeader("X-User-Role") String userRole,
            @Parameter(description = "Admin email performing the action") @RequestHeader("X-User-Email") String adminEmail,
            @Parameter(description = "Review ID") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminReviewService.publishReview(id, adminEmail)));
    }
}
