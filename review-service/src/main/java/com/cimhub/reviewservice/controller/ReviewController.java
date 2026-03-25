package com.cimhub.reviewservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.reviewservice.dto.request.SubmitReviewRequest;
import com.cimhub.reviewservice.dto.response.AverageRatingResponse;
import com.cimhub.reviewservice.dto.response.ReviewResponse;
import com.cimhub.reviewservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for review operations.
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Submit a new review for a completed trip.
     *
     * @param userId the ID of the user submitting the review (from X-User-Id header)
     * @param userRole the role of the user (DRIVER or PASSENGER, from X-User-Role header)
     * @param request the review submission request
     * @return the created review
     */
    @PostMapping
    public ResponseEntity<ApiResponseWrapper<ReviewResponse>> submitReview(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole,
            @Valid @RequestBody SubmitReviewRequest request) {
        log.info("Review submission request from user: {}", userId);
        ReviewResponse response = reviewService.submitReview(userId, userRole, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseWrapper.success(response));
    }

    /**
     * Get all reviews about a specific driver.
     *
     * @param driverId the driver's ID
     * @return list of reviews about the driver
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<ApiResponseWrapper<List<ReviewResponse>>> getReviewsByDriver(
            @PathVariable String driverId) {
        log.info("Fetching reviews for driver: {}", driverId);
        List<ReviewResponse> reviews = reviewService.getReviewsByDriver(driverId);
        return ResponseEntity.ok(ApiResponseWrapper.success(reviews));
    }

    /**
     * Get all reviews about a specific user/passenger.
     *
     * @param userId the user's ID
     * @return list of reviews about the user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponseWrapper<List<ReviewResponse>>> getReviewsByUser(
            @PathVariable String userId) {
        log.info("Fetching reviews for user: {}", userId);
        List<ReviewResponse> reviews = reviewService.getReviewsByUser(userId);
        return ResponseEntity.ok(ApiResponseWrapper.success(reviews));
    }

    /**
     * Get all reviews for a specific trip.
     *
     * @param tripId the trip's ID
     * @return list of reviews for the trip
     */
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<ApiResponseWrapper<List<ReviewResponse>>> getReviewsByTrip(
            @PathVariable String tripId) {
        log.info("Fetching reviews for trip: {}", tripId);
        List<ReviewResponse> reviews = reviewService.getReviewsByTrip(tripId);
        return ResponseEntity.ok(ApiResponseWrapper.success(reviews));
    }

    /**
     * Get all reviews submitted by the current user.
     *
     * @param userId the ID of the user (from X-User-Id header)
     * @return list of reviews given by the user
     */
    @GetMapping("/given")
    public ResponseEntity<ApiResponseWrapper<List<ReviewResponse>>> getReviewsGiven(
            @RequestHeader("X-User-Id") String userId) {
        log.info("Fetching reviews given by user: {}", userId);
        List<ReviewResponse> reviews = reviewService.getReviewsGiven(userId);
        return ResponseEntity.ok(ApiResponseWrapper.success(reviews));
    }

    /**
     * Get all reviews received by the current user.
     *
     * @param userId the ID of the user (from X-User-Id header)
     * @return list of reviews received by the user
     */
    @GetMapping("/received")
    public ResponseEntity<ApiResponseWrapper<List<ReviewResponse>>> getReviewsReceived(
            @RequestHeader("X-User-Id") String userId) {
        log.info("Fetching reviews received by user: {}", userId);
        List<ReviewResponse> reviews = reviewService.getReviewsReceived(userId);
        return ResponseEntity.ok(ApiResponseWrapper.success(reviews));
    }

    /**
     * Get the average rating for a driver.
     *
     * @param driverId the driver's ID
     * @return average rating response
     */
    @GetMapping("/driver/{driverId}/average-rating")
    public ResponseEntity<ApiResponseWrapper<AverageRatingResponse>> getAverageRating(
            @PathVariable String driverId) {
        log.info("Fetching average rating for driver: {}", driverId);
        AverageRatingResponse response = reviewService.getAverageRating(driverId);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }
}
