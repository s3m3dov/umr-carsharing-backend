package com.cimhub.reviewservice.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.service.AuditService;
import com.cimhub.reviewservice.dto.request.SubmitReviewRequest;
import com.cimhub.reviewservice.dto.response.AverageRatingResponse;
import com.cimhub.reviewservice.dto.response.ReviewResponse;
import com.cimhub.reviewservice.exception.UnauthorizedReviewerException;
import com.cimhub.reviewservice.model.Review;
import com.cimhub.reviewservice.repository.ReviewRepository;
import com.cimhub.reviewservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TripServiceClient tripServiceClient;

    @Mock
    private AuditService auditService;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository, userRepository, tripServiceClient, auditService);
    }

    @Test
    void submitDriverReview_withValidData_shouldPersistAndAudit() {
        SubmitReviewRequest request = SubmitReviewRequest.builder()
                .userId("passenger-1")
                .userRole(UserType.PASSENGER)
                .tripId("trip-1")
                .bookingId("booking-1")
                .rating(5)
                .comment("Great driver")
                .build();

        when(userRepository.existsByUserId("passenger-1")).thenReturn(true);
        when(tripServiceClient.isTripCompleted("trip-1")).thenReturn(true);
        when(tripServiceClient.isBookingCompleted("booking-1")).thenReturn(true);
        when(reviewRepository.existsByReviewerIdAndTripIdAndBookingId("passenger-1", "trip-1", "booking-1")).thenReturn(false);
        when(tripServiceClient.getTripDriverId("trip-1")).thenReturn("driver-1");
        when(tripServiceClient.getBookingPassengerId("booking-1")).thenReturn("passenger-1");
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setReviewId("review-1");
            return review;
        });

        ReviewResponse response = reviewService.submitDriverReview(request);

        assertEquals("review-1", response.reviewId());
        assertEquals("passenger-1", response.reviewerId());
        assertEquals("driver-1", response.revieweeId());
        assertEquals(UserType.PASSENGER, response.reviewerType());
        assertEquals(UserType.DRIVER, response.revieweeType());

        verify(auditService).logCreate(EntityType.REVIEW, "review-1", "passenger-1", "Driver review submitted");
    }

    @Test
    void submitDriverReview_whenBookingBelongsToAnotherPassenger_shouldThrowUnauthorized() {
        SubmitReviewRequest request = SubmitReviewRequest.builder()
                .userId("passenger-1")
                .userRole(UserType.PASSENGER)
                .tripId("trip-1")
                .bookingId("booking-1")
                .rating(4)
                .comment("Good")
                .build();

        when(userRepository.existsByUserId("passenger-1")).thenReturn(true);
        when(tripServiceClient.isTripCompleted("trip-1")).thenReturn(true);
        when(tripServiceClient.isBookingCompleted("booking-1")).thenReturn(true);
        when(reviewRepository.existsByReviewerIdAndTripIdAndBookingId("passenger-1", "trip-1", "booking-1")).thenReturn(false);
        when(tripServiceClient.getTripDriverId("trip-1")).thenReturn("driver-1");
        when(tripServiceClient.getBookingPassengerId("booking-1")).thenReturn("another-passenger");

        assertThrows(UnauthorizedReviewerException.class, () -> reviewService.submitDriverReview(request));
    }

    @Test
    void getDriverAverageRating_whenNoReviews_shouldReturnZeroStats() {
        when(userRepository.existsByUserId("driver-1")).thenReturn(true);
        when(reviewRepository.findByRevieweeIdAndRevieweeType("driver-1", UserType.DRIVER)).thenReturn(List.of());

        AverageRatingResponse response = reviewService.getDriverAverageRating("driver-1");

        assertEquals("driver-1", response.subjectId());
        assertEquals(0.0, response.averageRating());
        assertEquals(0L, response.totalReviews());
    }
}
