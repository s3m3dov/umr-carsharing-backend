package com.cimhub.reviewservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.reviewservice.dto.request.SubmitReviewRequest;
import com.cimhub.reviewservice.dto.response.AverageRatingResponse;
import com.cimhub.reviewservice.dto.response.ReviewResponse;
import com.cimhub.reviewservice.service.ReviewService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reviews", description = "APIs for submitting and retrieving driver and passenger reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
            summary = "Submit a review for a driver",
            description = "Passenger submits a review for a driver after completing a trip. User role must be PASSENGER."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Review submitted successfully",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiExamples.SUBMIT_DRIVER_REVIEW_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid review data or trip not completed",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiExamples.ERROR_TRIP_NOT_COMPLETED))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Review already exists for this trip",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiExamples.ERROR_REVIEW_ALREADY_EXISTS))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Driver or user not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiExamples.ERROR_DRIVER_NOT_FOUND))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized to review this driver",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiExamples.ERROR_UNAUTHORIZED_REVIEWER))
            )
    })
    @PostMapping("/driver")
    public ResponseEntity<ApiResponseWrapper<ReviewResponse>> submitDriverReview(
            @Valid @RequestBody SubmitReviewRequest request) {
        log.info("Submitting driver review from user: {}", request.userId());
        ReviewResponse response = reviewService.submitDriverReview(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseWrapper.success(response));
    }

    @Operation(
            summary = "Submit a review for a passenger",
            description = "Driver submits a review for a passenger after completing a trip. User role must be DRIVER."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Review submitted successfully",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiExamples.SUBMIT_PASSENGER_REVIEW_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid review data or trip not completed",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiExamples.ERROR_TRIP_NOT_COMPLETED))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Review already exists for this trip",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiExamples.ERROR_REVIEW_ALREADY_EXISTS))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Passenger not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiExamples.ERROR_PASSENGER_NOT_FOUND))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized to review this passenger",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiExamples.ERROR_UNAUTHORIZED_REVIEWER))
            )
    })
    @PostMapping("/passenger")
    public ResponseEntity<ApiResponseWrapper<ReviewResponse>> submitPassengerReview(
            @Valid @RequestBody SubmitReviewRequest request) {
        log.info("Submitting passenger review from user: {}", request.userId());
        ReviewResponse response = reviewService.submitPassengerReview(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseWrapper.success(response));
    }

    @Operation(
            summary = "Get all reviews for a driver",
            description = "Retrieves all reviews submitted about a specific driver"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reviews retrieved successfully",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiExamples.GET_DRIVER_REVIEWS_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Driver not found or no reviews exist",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiExamples.ERROR_DRIVER_NOT_FOUND))
            )
    })
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<ApiResponseWrapper<List<ReviewResponse>>> getDriverReviews(
            @Parameter(description = "Driver's unique identifier", required = true, example = "john.driver@example.com")
            @PathVariable String driverId) {
        log.info("Fetching reviews for driver: {}", driverId);
        List<ReviewResponse> reviews = reviewService.getDriverReviews(driverId);
        return ResponseEntity.ok(ApiResponseWrapper.success(reviews));
    }

    @Operation(
            summary = "Get all reviews for a passenger",
            description = "Retrieves all reviews submitted about a specific passenger"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reviews retrieved successfully",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiExamples.GET_PASSENGER_REVIEWS_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Passenger not found or no reviews exist",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiExamples.ERROR_PASSENGER_NOT_FOUND))
            )
    })
    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<ApiResponseWrapper<List<ReviewResponse>>> getPassengerReviews(
            @Parameter(description = "Passenger's unique identifier", required = true, example = "jane.passenger@example.com")
            @PathVariable String passengerId) {
        log.info("Fetching reviews for passenger: {}", passengerId);
        List<ReviewResponse> reviews = reviewService.getPassengerReviews(passengerId);
        return ResponseEntity.ok(ApiResponseWrapper.success(reviews));
    }

    @Operation(
            summary = "Get driver's average rating",
            description = "Calculates and returns the average rating for a driver based on all reviews"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Average rating retrieved successfully",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiExamples.GET_AVERAGE_RATING_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Driver not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiExamples.ERROR_DRIVER_NOT_FOUND))
            )
    })
    @GetMapping("/driver/{driverId}/average-rating")
    public ResponseEntity<ApiResponseWrapper<AverageRatingResponse>> getDriverAverageRating(
            @Parameter(description = "Driver's unique identifier", required = true, example = "john.driver@example.com")
            @PathVariable String driverId) {
        log.info("Fetching average rating for driver: {}", driverId);
        AverageRatingResponse response = reviewService.getDriverAverageRating(driverId);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }

    @Operation(
            summary = "Get passenger's average rating",
            description = "Calculates and returns the average rating for a passenger based on all reviews"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Average rating retrieved successfully",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiExamples.GET_AVERAGE_RATING_RESPONSE))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Passenger not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ReviewApiExamples.ERROR_PASSENGER_NOT_FOUND))
            )
    })
    @GetMapping("/passenger/{passengerId}/average-rating")
    public ResponseEntity<ApiResponseWrapper<AverageRatingResponse>> getPassengerAverageRating(
            @Parameter(description = "Passenger's unique identifier", required = true, example = "jane.passenger@example.com")
            @PathVariable String passengerId) {
        log.info("Fetching average rating for passenger: {}", passengerId);
        AverageRatingResponse response = reviewService.getPassengerAverageRating(passengerId);
        return ResponseEntity.ok(ApiResponseWrapper.success(response));
    }
}