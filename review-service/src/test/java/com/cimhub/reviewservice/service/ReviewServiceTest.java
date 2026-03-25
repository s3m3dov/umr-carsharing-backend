package com.cimhub.reviewservice.service;

import com.cimhub.common.enums.UserType;
import com.cimhub.reviewservice.dto.request.SubmitReviewRequest;
import com.cimhub.reviewservice.dto.response.AverageRatingResponse;
import com.cimhub.reviewservice.dto.response.ReviewResponse;
import com.cimhub.reviewservice.exception.ReviewAlreadyExistsException;
import com.cimhub.reviewservice.exception.TripNotCompletedException;
import com.cimhub.reviewservice.model.Review;
import com.cimhub.reviewservice.model.ReviewStatus;
import com.cimhub.reviewservice.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private TripServiceClient tripServiceClient;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository, tripServiceClient);
    }

    @Test
    void testSubmitReviewHappyPathPassengerReviewingDriver() {
        // Arrange
        String userId = "passenger-123";
        String userRole = "PASSENGER";
        String tripId = "trip-123";
        String bookingId = "booking-123";
        SubmitReviewRequest request = SubmitReviewRequest.builder()
                .tripId(tripId)
                .bookingId(bookingId)
                .rating(5)
                .comment("Great driver!")
                .build();

        when(tripServiceClient.isTripCompleted(tripId)).thenReturn(true);
        when(tripServiceClient.isBookingCompleted(bookingId)).thenReturn(true);
        when(reviewRepository.existsByReviewerIdAndTripIdAndBookingId(userId, tripId, bookingId))
                .thenReturn(false);

        Review savedReview = Review.builder()
                .reviewId("review-123")
                .tripId(tripId)
                .bookingId(bookingId)
                .reviewerId(userId)
                .reviewerType(UserType.PASSENGER)
                .revieweeId("driver-456")
                .revieweeType(UserType.DRIVER)
                .rating(5)
                .comment("Great driver!")
                .status(ReviewStatus.PUBLISHED)
                .createdAt(Instant.now())
                .build();

        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        // Act
        ReviewResponse response = reviewService.submitReview(userId, userRole, request);

        // Assert
        assertNotNull(response);
        assertEquals("review-123", response.reviewId());
        assertEquals(tripId, response.tripId());
        assertEquals(5, response.rating());
        assertEquals("Great driver!", response.comment());
        verify(tripServiceClient).isTripCompleted(tripId);
        verify(tripServiceClient).isBookingCompleted(bookingId);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void testSubmitReviewHappyPathDriverReviewingPassenger() {
        // Arrange
        String userId = "driver-456";
        String userRole = "DRIVER";
        String tripId = "trip-123";
        String bookingId = "booking-123";
        SubmitReviewRequest request = SubmitReviewRequest.builder()
                .tripId(tripId)
                .bookingId(bookingId)
                .rating(4)
                .comment("Good passenger")
                .build();

        when(tripServiceClient.isTripCompleted(tripId)).thenReturn(true);
        when(tripServiceClient.isBookingCompleted(bookingId)).thenReturn(true);
        when(reviewRepository.existsByReviewerIdAndTripIdAndBookingId(userId, tripId, bookingId))
                .thenReturn(false);

        Review savedReview = Review.builder()
                .reviewId("review-456")
                .tripId(tripId)
                .bookingId(bookingId)
                .reviewerId(userId)
                .reviewerType(UserType.DRIVER)
                .revieweeId("passenger-123")
                .revieweeType(UserType.PASSENGER)
                .rating(4)
                .comment("Good passenger")
                .status(ReviewStatus.PUBLISHED)
                .createdAt(Instant.now())
                .build();

        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        // Act
        ReviewResponse response = reviewService.submitReview(userId, userRole, request);

        // Assert
        assertNotNull(response);
        assertEquals("review-456", response.reviewId());
        assertEquals(UserType.DRIVER, response.reviewerType());
        assertEquals(4, response.rating());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void testSubmitReviewRejectsDuplicateReview() {
        // Arrange
        String userId = "passenger-123";
        String userRole = "PASSENGER";
        String tripId = "trip-123";
        String bookingId = "booking-123";
        SubmitReviewRequest request = SubmitReviewRequest.builder()
                .tripId(tripId)
                .bookingId(bookingId)
                .rating(5)
                .comment("Great driver!")
                .build();

        when(tripServiceClient.isTripCompleted(tripId)).thenReturn(true);
        when(tripServiceClient.isBookingCompleted(bookingId)).thenReturn(true);
        when(reviewRepository.existsByReviewerIdAndTripIdAndBookingId(userId, tripId, bookingId))
                .thenReturn(true);

        // Act & Assert
        assertThrows(ReviewAlreadyExistsException.class, () ->
                reviewService.submitReview(userId, userRole, request)
        );
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testSubmitReviewRejectsNonCompletedTrip() {
        // Arrange
        String userId = "passenger-123";
        String userRole = "PASSENGER";
        String tripId = "trip-123";
        String bookingId = "booking-123";
        SubmitReviewRequest request = SubmitReviewRequest.builder()
                .tripId(tripId)
                .bookingId(bookingId)
                .rating(5)
                .comment("Great driver!")
                .build();

        when(tripServiceClient.isTripCompleted(tripId)).thenReturn(false);

        // Act & Assert
        assertThrows(TripNotCompletedException.class, () ->
                reviewService.submitReview(userId, userRole, request)
        );
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testSubmitReviewRejectsNonCompletedBooking() {
        // Arrange
        String userId = "passenger-123";
        String userRole = "PASSENGER";
        String tripId = "trip-123";
        String bookingId = "booking-123";
        SubmitReviewRequest request = SubmitReviewRequest.builder()
                .tripId(tripId)
                .bookingId(bookingId)
                .rating(5)
                .comment("Great driver!")
                .build();

        when(tripServiceClient.isTripCompleted(tripId)).thenReturn(true);
        when(tripServiceClient.isBookingCompleted(bookingId)).thenReturn(false);

        // Act & Assert
        assertThrows(TripNotCompletedException.class, () ->
                reviewService.submitReview(userId, userRole, request)
        );
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testGetReviewsByDriver() {
        // Arrange
        String driverId = "driver-456";
        Review review1 = Review.builder()
                .reviewId("review-1")
                .tripId("trip-1")
                .revieweeId(driverId)
                .rating(5)
                .comment("Excellent")
                .build();
        Review review2 = Review.builder()
                .reviewId("review-2")
                .tripId("trip-2")
                .revieweeId(driverId)
                .rating(4)
                .comment("Good")
                .build();

        when(reviewRepository.findByRevieweeId(driverId))
                .thenReturn(Arrays.asList(review1, review2));

        // Act
        List<ReviewResponse> reviews = reviewService.getReviewsByDriver(driverId);

        // Assert
        assertEquals(2, reviews.size());
        assertEquals("review-1", reviews.get(0).reviewId());
        assertEquals("review-2", reviews.get(1).reviewId());
        verify(reviewRepository).findByRevieweeId(driverId);
    }

    @Test
    void testGetReviewsByUser() {
        // Arrange
        String userId = "passenger-123";
        Review review = Review.builder()
                .reviewId("review-1")
                .tripId("trip-1")
                .revieweeId(userId)
                .rating(3)
                .build();

        when(reviewRepository.findByRevieweeId(userId))
                .thenReturn(Arrays.asList(review));

        // Act
        List<ReviewResponse> reviews = reviewService.getReviewsByUser(userId);

        // Assert
        assertEquals(1, reviews.size());
        assertEquals(3, reviews.get(0).rating());
    }

    @Test
    void testGetReviewsByTrip() {
        // Arrange
        String tripId = "trip-123";
        Review review1 = Review.builder()
                .reviewId("review-1")
                .tripId(tripId)
                .rating(5)
                .build();
        Review review2 = Review.builder()
                .reviewId("review-2")
                .tripId(tripId)
                .rating(4)
                .build();

        when(reviewRepository.findByTripId(tripId))
                .thenReturn(Arrays.asList(review1, review2));

        // Act
        List<ReviewResponse> reviews = reviewService.getReviewsByTrip(tripId);

        // Assert
        assertEquals(2, reviews.size());
        verify(reviewRepository).findByTripId(tripId);
    }

    @Test
    void testGetReviewsGiven() {
        // Arrange
        String userId = "passenger-123";
        Review review = Review.builder()
                .reviewId("review-1")
                .reviewerId(userId)
                .rating(5)
                .build();

        when(reviewRepository.findByReviewerId(userId))
                .thenReturn(Arrays.asList(review));

        // Act
        List<ReviewResponse> reviews = reviewService.getReviewsGiven(userId);

        // Assert
        assertEquals(1, reviews.size());
        assertEquals("review-1", reviews.get(0).reviewId());
    }

    @Test
    void testGetReviewsReceived() {
        // Arrange
        String userId = "driver-456";
        Review review = Review.builder()
                .reviewId("review-1")
                .revieweeId(userId)
                .rating(5)
                .build();

        when(reviewRepository.findByRevieweeId(userId))
                .thenReturn(Arrays.asList(review));

        // Act
        List<ReviewResponse> reviews = reviewService.getReviewsReceived(userId);

        // Assert
        assertEquals(1, reviews.size());
        assertEquals("review-1", reviews.get(0).reviewId());
    }

    @Test
    void testGetAverageRatingWithMultipleReviews() {
        // Arrange
        String driverId = "driver-456";
        Review review1 = Review.builder()
                .reviewId("review-1")
                .revieweeId(driverId)
                .rating(5)
                .build();
        Review review2 = Review.builder()
                .reviewId("review-2")
                .revieweeId(driverId)
                .rating(4)
                .build();
        Review review3 = Review.builder()
                .reviewId("review-3")
                .revieweeId(driverId)
                .rating(5)
                .build();

        when(reviewRepository.findByRevieweeId(driverId))
                .thenReturn(Arrays.asList(review1, review2, review3));

        // Act
        AverageRatingResponse response = reviewService.getAverageRating(driverId);

        // Assert
        assertEquals(driverId, response.subjectId());
        assertEquals(4.666666666666667, response.averageRating(), 0.01);
        assertEquals(3, response.totalReviews());
    }

    @Test
    void testGetAverageRatingWithNoReviews() {
        // Arrange
        String driverId = "driver-456";
        when(reviewRepository.findByRevieweeId(driverId))
                .thenReturn(Arrays.asList());

        // Act
        AverageRatingResponse response = reviewService.getAverageRating(driverId);

        // Assert
        assertEquals(driverId, response.subjectId());
        assertEquals(0.0, response.averageRating());
        assertEquals(0, response.totalReviews());
    }

    @Test
    void testGetAverageRatingWithSingleReview() {
        // Arrange
        String driverId = "driver-456";
        Review review = Review.builder()
                .reviewId("review-1")
                .revieweeId(driverId)
                .rating(4)
                .build();

        when(reviewRepository.findByRevieweeId(driverId))
                .thenReturn(Arrays.asList(review));

        // Act
        AverageRatingResponse response = reviewService.getAverageRating(driverId);

        // Assert
        assertEquals(driverId, response.subjectId());
        assertEquals(4.0, response.averageRating());
        assertEquals(1, response.totalReviews());
    }
}
