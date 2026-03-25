package com.cimhub.reviewservice.service;

import com.cimhub.common.enums.UserType;
import com.cimhub.reviewservice.dto.request.SubmitReviewRequest;
import com.cimhub.reviewservice.dto.response.AverageRatingResponse;
import com.cimhub.reviewservice.dto.response.ReviewResponse;
import com.cimhub.reviewservice.exception.ReviewAlreadyExistsException;
import com.cimhub.reviewservice.exception.TripNotCompletedException;
import com.cimhub.reviewservice.exception.UnauthorizedReviewerException;
import com.cimhub.reviewservice.model.Review;
import com.cimhub.reviewservice.model.ReviewStatus;
import com.cimhub.reviewservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Service for managing reviews in the system.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TripServiceClient tripServiceClient;

    /**
     * Submit a new review for a trip.
     * Validates that:
     * 1. The trip/booking is completed
     * 2. The reviewer has not already reviewed this trip
     * 3. The reviewer was part of the trip
     *
     * @param userId the ID of the user submitting the review (from X-User-Id header)
     * @param userRole the role of the user (DRIVER or PASSENGER, from X-User-Role header)
     * @param request the review submission request
     * @return the created review response
     */
    @Transactional
    public ReviewResponse submitReview(String userId, String userRole, SubmitReviewRequest request) {
        log.info("Submitting review from user: {} for trip: {}", userId, request.tripId());

        // Validate trip is completed
        if (!tripServiceClient.isTripCompleted(request.tripId())) {
            throw new TripNotCompletedException("Trip must be completed before review can be submitted");
        }

        // Validate booking is completed
        if (!tripServiceClient.isBookingCompleted(request.bookingId())) {
            throw new TripNotCompletedException("Booking must be completed before review can be submitted");
        }

        // Check if review already exists
        if (reviewRepository.existsByReviewerIdAndTripIdAndBookingId(userId, request.tripId(), request.bookingId())) {
            throw new ReviewAlreadyExistsException("You have already reviewed this trip");
        }

        UserType reviewerType = UserType.valueOf(userRole);

        // Create and save the review
        Review review = Review.builder()
                .tripId(request.tripId())
                .bookingId(request.bookingId())
                .reviewerId(userId)
                .reviewerType(reviewerType)
                .rating(request.rating())
                .comment(request.comment())
                .status(ReviewStatus.PUBLISHED)
                .createdAt(Instant.now())
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Review saved with ID: {}", savedReview.getReviewId());

        return mapToResponse(savedReview);
    }

    /**
     * Get all reviews for a specific driver.
     *
     * @param driverId the driver's ID
     * @return list of reviews about the driver
     */
    public List<ReviewResponse> getReviewsByDriver(String driverId) {
        log.info("Fetching reviews for driver: {}", driverId);
        List<Review> reviews = reviewRepository.findByRevieweeId(driverId);
        return reviews.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get all reviews for a specific passenger.
     *
     * @param userId the passenger's ID
     * @return list of reviews about the passenger
     */
    public List<ReviewResponse> getReviewsByUser(String userId) {
        log.info("Fetching reviews for user: {}", userId);
        List<Review> reviews = reviewRepository.findByRevieweeId(userId);
        return reviews.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get all reviews for a specific trip.
     *
     * @param tripId the trip's ID
     * @return list of reviews for the trip
     */
    public List<ReviewResponse> getReviewsByTrip(String tripId) {
        log.info("Fetching reviews for trip: {}", tripId);
        List<Review> reviews = reviewRepository.findByTripId(tripId);
        return reviews.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get all reviews submitted by the current user.
     *
     * @param userId the user's ID
     * @return list of reviews given by the user
     */
    public List<ReviewResponse> getReviewsGiven(String userId) {
        log.info("Fetching reviews given by user: {}", userId);
        List<Review> reviews = reviewRepository.findByReviewerId(userId);
        return reviews.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get all reviews received by the current user.
     *
     * @param userId the user's ID
     * @return list of reviews received by the user
     */
    public List<ReviewResponse> getReviewsReceived(String userId) {
        log.info("Fetching reviews received by user: {}", userId);
        List<Review> reviews = reviewRepository.findByRevieweeId(userId);
        return reviews.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Calculate the average rating for a driver.
     *
     * @param driverId the driver's ID
     * @return average rating response
     */
    public AverageRatingResponse getAverageRating(String driverId) {
        log.info("Calculating average rating for driver: {}", driverId);
        List<Review> reviews = reviewRepository.findByRevieweeId(driverId);

        if (reviews.isEmpty()) {
            return AverageRatingResponse.builder()
                    .subjectId(driverId)
                    .averageRating(0.0)
                    .totalReviews(0)
                    .build();
        }

        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        return AverageRatingResponse.builder()
                .subjectId(driverId)
                .averageRating(averageRating)
                .totalReviews(reviews.size())
                .build();
    }

    /**
     * Map a Review entity to a ReviewResponse DTO.
     */
    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .tripId(review.getTripId())
                .bookingId(review.getBookingId())
                .reviewerId(review.getReviewerId())
                .reviewerType(review.getReviewerType())
                .revieweeId(review.getRevieweeId())
                .revieweeType(review.getRevieweeType())
                .rating(review.getRating())
                .comment(review.getComment())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
