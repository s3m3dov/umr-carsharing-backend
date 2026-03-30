package com.cimhub.reviewservice.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.service.AuditService;
import com.cimhub.reviewservice.dto.request.SubmitReviewRequest;
import com.cimhub.reviewservice.dto.response.AverageRatingResponse;
import com.cimhub.reviewservice.dto.response.ReviewResponse;
import com.cimhub.reviewservice.exception.DriverNotFoundException;
import com.cimhub.reviewservice.exception.PassengerNotFoundException;
import com.cimhub.reviewservice.exception.ReviewAlreadyExistsException;
import com.cimhub.reviewservice.exception.ReviewsNotFoundException;
import com.cimhub.reviewservice.exception.TripNotCompletedException;
import com.cimhub.reviewservice.exception.UnauthorizedReviewerException;
import com.cimhub.reviewservice.model.Review;
import com.cimhub.reviewservice.model.ReviewStatus;
import com.cimhub.reviewservice.repository.ReviewRepository;
import com.cimhub.reviewservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final TripServiceClient tripServiceClient;
    private final AuditService auditService;

    @Transactional
    public ReviewResponse submitDriverReview(SubmitReviewRequest request) {
        log.info("Submitting driver review from user: {} for trip: {}", request.userId(), request.tripId());

        validateUserRole(request.userRole(), "PASSENGER", "Only passengers can review drivers");

        if (!userRepository.existsByUserId(request.userId())) {
            throw new PassengerNotFoundException("Reviewer user not found: " + request.userId());
        }

        validateTripAndBookingCompleted(request.tripId(), request.bookingId());

        if (reviewRepository.existsByReviewerIdAndTripIdAndBookingId(request.userId(), request.tripId(), request.bookingId())) {
            throw new ReviewAlreadyExistsException("You have already reviewed this trip");
        }

        String driverId = tripServiceClient.getTripDriverId(request.tripId());
        if (driverId == null) {
            throw new DriverNotFoundException("Driver not found for trip: " + request.tripId());
        }

        String bookingPassengerId = tripServiceClient.getBookingPassengerId(request.bookingId());
        if (!request.userId().equals(bookingPassengerId)) {
            throw new UnauthorizedReviewerException("You are not the passenger of this booking");
        }

        Review review = Review.builder()
                .updatedAt(Instant.now())
                .tripId(request.tripId())
                .bookingId(request.bookingId())
                .reviewerId(request.userId())
                .reviewerType(UserType.PASSENGER)
                .revieweeId(driverId)
                .revieweeType(UserType.DRIVER)
                .rating(request.rating())
                .comment(request.comment())
                .status(ReviewStatus.PUBLISHED)
                .createdAt(Instant.now())
                .build();

        Review savedReview = reviewRepository.save(review);
        auditService.logCreate(EntityType.REVIEW, savedReview.getReviewId(), request.userId(), "Driver review submitted");
        log.info("Driver review saved with ID: {}", savedReview.getReviewId());

        return mapToResponse(savedReview);
    }

    @Transactional
    public ReviewResponse submitPassengerReview(SubmitReviewRequest request) {
        log.info("Submitting passenger review from user: {} for trip: {}", request.userId(), request.tripId());

        validateUserRole(request.userRole(), "DRIVER", "Only drivers can review passengers");

        if (!userRepository.existsByUserId(request.userId())) {
            throw new DriverNotFoundException("Reviewer user not found: " + request.userId());
        }

        validateTripAndBookingCompleted(request.tripId(), request.bookingId());

        if (reviewRepository.existsByReviewerIdAndTripIdAndBookingId(request.userId(), request.tripId(), request.bookingId())) {
            throw new ReviewAlreadyExistsException("You have already reviewed this trip");
        }

        String driverId = tripServiceClient.getTripDriverId(request.tripId());
        if (!request.userId().equals(driverId)) {
            throw new UnauthorizedReviewerException("Only the driver of this trip can review passengers");
        }

        String passengerId = tripServiceClient.getBookingPassengerId(request.bookingId());
        if (passengerId == null) {
            throw new PassengerNotFoundException("Passenger not found for booking: " + request.bookingId());
        }

        Review review = Review.builder()
                .updatedAt(Instant.now())
                .tripId(request.tripId())
                .bookingId(request.bookingId())
                .reviewerId(request.userId())
                .reviewerType(UserType.DRIVER)
                .revieweeId(passengerId)
                .revieweeType(UserType.PASSENGER)
                .rating(request.rating())
                .comment(request.comment())
                .status(ReviewStatus.PUBLISHED)
                .createdAt(Instant.now())
                .build();

        Review savedReview = reviewRepository.save(review);
        auditService.logCreate(EntityType.REVIEW, savedReview.getReviewId(), request.userId(), "Passenger review submitted");
        log.info("Passenger review saved with ID: {}", savedReview.getReviewId());

        return mapToResponse(savedReview);
    }

    public List<ReviewResponse> getDriverReviews(String driverId) {
        log.info("Fetching reviews for driver: {}", driverId);
        
        if (!userRepository.existsByUserId(driverId)) {
            throw new DriverNotFoundException("Driver not found: " + driverId);
        }

        List<Review> reviews = reviewRepository.findByRevieweeIdAndRevieweeType(driverId, UserType.DRIVER);
        
        if (reviews.isEmpty()) {
            throw new ReviewsNotFoundException("No reviews found for driver: " + driverId);
        }

        return reviews.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<ReviewResponse> getPassengerReviews(String passengerId) {
        log.info("Fetching reviews for passenger: {}", passengerId);
        
        if (!userRepository.existsByUserId(passengerId)) {
            throw new PassengerNotFoundException("Passenger not found: " + passengerId);
        }

        List<Review> reviews = reviewRepository.findByRevieweeIdAndRevieweeType(passengerId, UserType.PASSENGER);
        
        if (reviews.isEmpty()) {
            throw new ReviewsNotFoundException("No reviews found for passenger: " + passengerId);
        }

        return reviews.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AverageRatingResponse getDriverAverageRating(String driverId) {
        log.info("Calculating average rating for driver: {}", driverId);

        if (!userRepository.existsByUserId(driverId)) {
            throw new DriverNotFoundException("Driver not found: " + driverId);
        }

        List<Review> reviews = reviewRepository.findByRevieweeIdAndRevieweeType(driverId, UserType.DRIVER);

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

    public AverageRatingResponse getPassengerAverageRating(String passengerId) {
        log.info("Calculating average rating for passenger: {}", passengerId);

        if (!userRepository.existsByUserId(passengerId)) {
            throw new PassengerNotFoundException("Passenger not found: " + passengerId);
        }

        List<Review> reviews = reviewRepository.findByRevieweeIdAndRevieweeType(passengerId, UserType.PASSENGER);

        if (reviews.isEmpty()) {
            return AverageRatingResponse.builder()
                    .subjectId(passengerId)
                    .averageRating(0.0)
                    .totalReviews(0)
                    .build();
        }

        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        return AverageRatingResponse.builder()
                .subjectId(passengerId)
                .averageRating(averageRating)
                .totalReviews(reviews.size())
                .build();
    }

    private void validateUserRole(String userRole, String expectedRole, String errorMessage) {
        if (!expectedRole.equalsIgnoreCase(userRole)) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void validateTripAndBookingCompleted(String tripId, String bookingId) {
        if (!tripServiceClient.isTripCompleted(tripId)) {
            throw new TripNotCompletedException("Trip must be completed before review can be submitted");
        }

        if (!tripServiceClient.isBookingCompleted(bookingId)) {
            throw new TripNotCompletedException("Booking must be completed before review can be submitted");
        }
    }

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
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
