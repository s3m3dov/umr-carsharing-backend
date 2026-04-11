package com.cimhub.reviewservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.common.constant.HeaderConstants;
import com.cimhub.common.util.AdminGuard;
import com.cimhub.reviewservice.dto.examples.ReviewApiDoc;
import com.cimhub.reviewservice.dto.response.ReviewResponse;
import com.cimhub.reviewservice.model.ReviewStatus;
import com.cimhub.reviewservice.service.AdminReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@Tag(name = "Admin - Review Management", description = "Admin endpoints for managing reviews")
@ApiResponses({
    @ApiResponse(responseCode = "403", description = "Forbidden - User is not an admin",
        content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiDoc.ERROR_ADMIN_UNAUTHORIZED))),
    @ApiResponse(responseCode = "404", description = "Resource not found",
        content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiDoc.ERROR_REVIEW_NOT_FOUND)))
})
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @Operation(summary = "List all reviews", description = "Paginated list of reviews with optional status filter")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiDoc.ADMIN_LIST_REVIEWS_RESPONSE)))
    })
    @GetMapping
    public ResponseEntity<ApiResponseWrapper<Page<ReviewResponse>>> listReviews(
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true) String userRole,
            @Parameter(description = "Filter by review status") @RequestParam(required = false) ReviewStatus status,
            Pageable pageable) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminReviewService.listReviews(status, pageable)));
    }

    @Operation(summary = "Get review detail", description = "Retrieve detailed information about a specific review")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Review retrieved successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiDoc.ADMIN_GET_REVIEW_RESPONSE)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<ReviewResponse>> getReview(
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true) String userRole,
            @Parameter(description = "Review ID", example = "review_001") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminReviewService.getReview(id)));
    }

    @Operation(summary = "Flag a review", description = "Flag a review for review by admin")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Review flagged successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiDoc.ADMIN_FLAG_REVIEW_RESPONSE)))
    })
    @PostMapping("/{id}/flag")
    public ResponseEntity<ApiResponseWrapper<ReviewResponse>> flagReview(
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true) String userRole,
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = "X-User-Email", required = true) String adminEmail,
            @Parameter(description = "Review ID", example = "review_001") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminReviewService.flagReview(id, adminEmail)));
    }

    @Operation(summary = "Remove a review", description = "Remove/hide a review from public view")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Review removed successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiDoc.ADMIN_REMOVE_REVIEW_RESPONSE)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseWrapper<String>> removeReview(
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true) String userRole,
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = "X-User-Email", required = true) String adminEmail,
            @Parameter(description = "Review ID", example = "review_001") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        adminReviewService.removeReview(id, adminEmail);
        return ResponseEntity.ok(ApiResponseWrapper.success("Review removed"));
    }

    @Operation(summary = "Publish/restore a review", description = "Publish or restore a flagged review")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Review published successfully",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiDoc.ADMIN_PUBLISH_REVIEW_RESPONSE)))
    })
    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponseWrapper<ReviewResponse>> publishReview(
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = HeaderConstants.X_USER_ROLE, required = true) String userRole,
            @Parameter(description = "Injected by the gateway from the JWT. Do not set manually.", required = false) @RequestHeader(value = "X-User-Email", required = true) String adminEmail,
            @Parameter(description = "Review ID", example = "review_001") @PathVariable String id) {
        AdminGuard.requireAdmin(userRole);
        return ResponseEntity.ok(ApiResponseWrapper.success(adminReviewService.publishReview(id, adminEmail)));
    }
}
