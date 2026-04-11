package com.cimhub.reviewservice.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.service.AuditService;
import com.cimhub.reviewservice.dto.request.SubmitReviewRequest;
import com.cimhub.reviewservice.dto.response.AverageRatingResponse;
import com.cimhub.reviewservice.dto.response.ReviewResponse;
import com.cimhub.reviewservice.exception.DriverNotFoundException;
import com.cimhub.reviewservice.exception.InvalidRatingException;
import com.cimhub.reviewservice.exception.InvalidReviewerException;
import com.cimhub.reviewservice.exception.PassengerNotFoundException;
import com.cimhub.reviewservice.exception.ReviewAlreadyExistsException;
import com.cimhub.reviewservice.exception.ReviewsNotFoundException;
import com.cimhub.reviewservice.exception.TripNotCompletedException;
import com.cimhub.reviewservice.exception.UnauthorizedReviewerException;
import com.cimhub.reviewservice.mapper.ReviewMapper;
import com.cimhub.reviewservice.model.Review;
import com.cimhub.reviewservice.model.ReviewStatus;
import com.cimhub.reviewservice.repository.MongoReviewDataRepository;
import com.cimhub.reviewservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final MongoReviewDataRepository reviewRepository;
    private final UserRepository userRepository;
    private final TripServiceClient tripServiceClient;
    private final AuditService auditService;

    @Transactional
    public ReviewResponse submitDriverReview(String userId, UserType userRole, SubmitReviewRequest request) {
        return submitReview(
                userId, userRole, request,
                UserType.PASSENGER,
                UserType.DRIVER,
                "Only passengers can review drivers",
                reviewerId -> new PassengerNotFoundException("Reviewer user not found: " + reviewerId),
                this::resolveDriverRevieweeId,
                "Driver review submitted"
        );
    }

    @Transactional
    public ReviewResponse submitPassengerReview(String userId, UserType userRole, SubmitReviewRequest request) {
        return submitReview(
                userId, userRole, request,
                UserType.DRIVER,
                UserType.PASSENGER,
                "Only drivers can review passengers",
                reviewerId -> new DriverNotFoundException("Reviewer user not found: " + reviewerId),
                this::resolvePassengerRevieweeId,
                "Passenger review submitted"
        );
    }

    public List<ReviewResponse> getDriverReviews(String driverId) {
        log.info("Fetching reviews for driver: {}", driverId);
        ensureUserExists(driverId, reviewerId -> new DriverNotFoundException("Driver not found: " + reviewerId));
        return getReviewsForReviewee(driverId, UserType.DRIVER, "No reviews found for driver: " + driverId);
    }

    public List<ReviewResponse> getPassengerReviews(String passengerId) {
        log.info("Fetching reviews for passenger: {}", passengerId);
        ensureUserExists(passengerId, reviewerId -> new PassengerNotFoundException("Passenger not found: " + reviewerId));
        return getReviewsForReviewee(passengerId, UserType.PASSENGER, "No reviews found for passenger: " + passengerId);
    }

    public AverageRatingResponse getDriverAverageRating(String driverId) {
        log.info("Calculating average rating for driver: {}", driverId);
        ensureUserExists(driverId, reviewerId -> new DriverNotFoundException("Driver not found: " + reviewerId));
        return calculateAverageRating(driverId, UserType.DRIVER);
    }

    public AverageRatingResponse getPassengerAverageRating(String passengerId) {
        log.info("Calculating average rating for passenger: {}", passengerId);
        ensureUserExists(passengerId, reviewerId -> new PassengerNotFoundException("Passenger not found: " + reviewerId));
        return calculateAverageRating(passengerId, UserType.PASSENGER);
    }

    private ReviewResponse submitReview(
            String userId,
            UserType userRole,
            SubmitReviewRequest request,
            UserType expectedReviewerRole,
            UserType revieweeType,
            String roleValidationError,
            Function<String, RuntimeException> reviewerNotFoundFactory,
            BiFunction<String, SubmitReviewRequest, String> revieweeIdResolver,
            String auditMessage) {
        log.info("Submitting {} review from user: {} for trip: {}", revieweeType.name().toLowerCase(), userId, request.tripId());

        validateRatingRange(request.rating());
        validateUserRole(userRole, expectedReviewerRole, roleValidationError);
        ensureUserExists(userId, reviewerNotFoundFactory);
        validateTripAndBookingCompleted(request.tripId(), request.bookingId());
        ensureReviewDoesNotExist(userId, request.tripId(), request.bookingId());

        String revieweeId = revieweeIdResolver.apply(userId, request);
        Review review = buildReview(userId, expectedReviewerRole, request, revieweeId, revieweeType);

        Review savedReview = reviewRepository.save(review);
        auditService.logCreate(EntityType.REVIEW, savedReview.getReviewId(), userId, auditMessage);
        log.info("Review saved with ID: {}", savedReview.getReviewId());

        return ReviewMapper.toResponse(savedReview);
    }

    private String resolveDriverRevieweeId(String userId, SubmitReviewRequest request) {
        String driverId = tripServiceClient.getTripDriverId(request.tripId());
        if (driverId == null) {
            throw new DriverNotFoundException("Driver not found for trip: " + request.tripId());
        }

        String bookingPassengerId = tripServiceClient.getBookingPassengerId(request.bookingId());
        if (!userId.equals(bookingPassengerId)) {
            throw new UnauthorizedReviewerException("You are not the passenger of this booking");
        }

        return driverId;
    }

    private String resolvePassengerRevieweeId(String userId, SubmitReviewRequest request) {
        String driverId = tripServiceClient.getTripDriverId(request.tripId());
        if (driverId == null) {
            throw new DriverNotFoundException("Driver not found for trip: " + request.tripId());
        }
        if (!userId.equals(driverId)) {
            throw new UnauthorizedReviewerException("Only the driver of this trip can review passengers");
        }

        String passengerId = tripServiceClient.getBookingPassengerId(request.bookingId());
        if (passengerId == null) {
            throw new PassengerNotFoundException("Passenger not found for booking: " + request.bookingId());
        }

        return passengerId;
    }

    private Review buildReview(String userId, UserType reviewerType, SubmitReviewRequest request, String revieweeId, UserType revieweeType) {
        Instant now = Instant.now();
        return Review.builder()
                .tripId(request.tripId())
                .bookingId(request.bookingId())
                .reviewerId(userId)
                .reviewerType(reviewerType)
                .revieweeId(revieweeId)
                .revieweeType(revieweeType)
                .rating(request.rating())
                .comment(request.comment())
                .status(ReviewStatus.PUBLISHED)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private void ensureUserExists(String userId, Function<String, RuntimeException> notFoundFactory) {
        if (!userRepository.existsByUserId(userId)) {
            throw notFoundFactory.apply(userId);
        }
    }

    private void ensureReviewDoesNotExist(String reviewerId, String tripId, String bookingId) {
        if (reviewRepository.existsByReviewerIdAndTripIdAndBookingId(reviewerId, tripId, bookingId)) {
            throw new ReviewAlreadyExistsException("You have already reviewed this trip");
        }
    }

    private void validateRatingRange(int rating) {
        if (rating < 1 || rating > 5) {
            throw new InvalidRatingException("Rating must be between 1 and 5");
        }
    }

    private List<ReviewResponse> getReviewsForReviewee(String revieweeId, UserType revieweeType, String noReviewsMessage) {
        List<Review> reviews = reviewRepository.findByRevieweeIdAndRevieweeType(revieweeId, revieweeType);
        if (reviews.isEmpty()) {
            throw new ReviewsNotFoundException(noReviewsMessage);
        }
        return reviews.stream()
                .map(ReviewMapper::toResponse)
                .toList();
    }

    private AverageRatingResponse calculateAverageRating(String subjectId, UserType revieweeType) {
        List<Review> reviews = reviewRepository.findByRevieweeIdAndRevieweeType(subjectId, revieweeType);
        if (reviews.isEmpty()) {
            return AverageRatingResponse.builder()
                    .subjectId(subjectId)
                    .averageRating(0.0)
                    .totalReviews(0)
                    .build();
        }

        return AverageRatingResponse.builder()
                .subjectId(subjectId)
                .averageRating(reviews.stream().mapToInt(Review::getRating).average().orElse(0.0))
                .totalReviews(reviews.size())
                .build();
    }

    private void validateUserRole(UserType userRole, UserType expectedRole, String errorMessage) {
        if (userRole != expectedRole) {
            throw new InvalidReviewerException(errorMessage);
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
}
