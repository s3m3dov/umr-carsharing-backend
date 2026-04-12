package com.cimhub.reviewservice.controller;

import com.cimhub.common.dto.response.ApiSuccess;
import com.cimhub.reviewservice.service.ReviewService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal controller for user management in review-service.
 * Hidden from public API documentation.
 */
@Slf4j
@Hidden
@RestController
@RequestMapping("/api/internal/reviews/users")
@RequiredArgsConstructor
public class InternalReviewController {

    private final ReviewService reviewService;

    @Operation(
            summary = "Internal: Delete user data",
            description = "Deletes all user-related data from review-service")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiSuccess<String>> deleteUserData(@PathVariable String userId) {
        log.info("Internal request to delete all review data for user: {}", userId);
        reviewService.deleteUserData(userId);
        return ResponseEntity.ok(ApiSuccess.success("User data deleted from review-service"));
    }
}
