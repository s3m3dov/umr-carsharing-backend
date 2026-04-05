package com.cimhub.reviewservice.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.service.AuditService;
import com.cimhub.reviewservice.dto.request.SubmitReviewRequest;
import com.cimhub.reviewservice.dto.response.AverageRatingResponse;
import com.cimhub.reviewservice.dto.response.ReviewResponse;
import com.cimhub.reviewservice.exception.ReviewAlreadyExistsException;
import com.cimhub.reviewservice.exception.TripNotCompletedException;
import com.cimhub.reviewservice.exception.UnauthorizedReviewerException;
import com.cimhub.reviewservice.model.Review;
import com.cimhub.reviewservice.repository.MongoReviewDataRepository;
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
    private MongoReviewDataRepository reviewRepository;

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
    void submitPassengerReview_withValidData_shouldPersistAndAudit() {
        SubmitReviewRequest request = SubmitReviewRequest.builder()
                .userId("driver-1")
                .userRole(UserType.DRIVER)
                .tripId("trip-1")
                .bookingId("booking-1")
                .rating(5)
                .comment("Great passenger")
                .build();

        when(userRepository.existsByUserId("driver-1")).thenReturn(true);
        when(tripServiceClient.isTripCompleted("trip-1")).thenReturn(true);
        when(tripServiceClient.isBookingCompleted("booking-1")).thenReturn(true);
        when(reviewRepository.existsByReviewerIdAndTripIdAndBookingId("driver-1", "trip-1", "booking-1")).thenReturn(false);
        when(tripServiceClient.getTripDriverId("trip-1")).thenReturn("driver-1");
        when(tripServiceClient.getBookingPassengerId("booking-1")).thenReturn("passenger-1");
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setReviewId("review-2");
            return review;
        });

        ReviewResponse response = reviewService.submitPassengerReview(request);

        assertEquals("review-2", response.reviewId());
        assertEquals("driver-1", response.reviewerId());
        assertEquals("passenger-1", response.revieweeId());
        assertEquals(UserType.DRIVER, response.reviewerType());
        assertEquals(UserType.PASSENGER, response.revieweeType());

        verify(auditService).logCreate(EntityType.REVIEW, "review-2", "driver-1", "Passenger review submitted");
    }

    @Test
    void submitPassengerReview_whenTripOrBookingNotCompleted_shouldThrow() {
        SubmitReviewRequest request = SubmitReviewRequest.builder()
                .userId("driver-1")
                .userRole(UserType.DRIVER)
                .tripId("trip-1")
                .bookingId("booking-1")
                .rating(4)
                .comment("Ok")
                .build();

        when(userRepository.existsByUserId("driver-1")).thenReturn(true);
        when(tripServiceClient.isTripCompleted("trip-1")).thenReturn(false);

        assertThrows(TripNotCompletedException.class, () -> reviewService.submitPassengerReview(request));
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

    @Test
    void getDriverAverageRating_withMultipleReviews_shouldCalculateCorrectAverage() {
        Review r1 = Review.builder().rating(4).revieweeId("driver-1").revieweeType(UserType.DRIVER).build();
        Review r2 = Review.builder().rating(5).revieweeId("driver-1").revieweeType(UserType.DRIVER).build();
        Review r3 = Review.builder().rating(3).revieweeId("driver-1").revieweeType(UserType.DRIVER).build();

        when(userRepository.existsByUserId("driver-1")).thenReturn(true);
        when(reviewRepository.findByRevieweeIdAndRevieweeType("driver-1", UserType.DRIVER))
                .thenReturn(List.of(r1, r2, r3));

        AverageRatingResponse response = reviewService.getDriverAverageRating("driver-1");

        assertEquals(3L, response.totalReviews());
        assertEquals(4.0, response.averageRating(), 0.001); // (4+5+3)/3
    }

    @Test
    void getPassengerAverageRating_withReviews_shouldCalculateCorrectAverage() {
        Review r1 = Review.builder().rating(5).revieweeId("passenger-1").revieweeType(UserType.PASSENGER).build();
        Review r2 = Review.builder().rating(3).revieweeId("passenger-1").revieweeType(UserType.PASSENGER).build();

        when(userRepository.existsByUserId("passenger-1")).thenReturn(true);
        when(reviewRepository.findByRevieweeIdAndRevieweeType("passenger-1", UserType.PASSENGER))
                .thenReturn(List.of(r1, r2));

        AverageRatingResponse response = reviewService.getPassengerAverageRating("passenger-1");

        assertEquals("passenger-1", response.subjectId());
        assertEquals(2L, response.totalReviews());
        assertEquals(4.0, response.averageRating(), 0.001); // (5+3)/2
    }

    @Test
    void submitDriverReview_whenAlreadyReviewedSameTrip_shouldThrowReviewAlreadyExists() {
        SubmitReviewRequest request = SubmitReviewRequest.builder()
                .userId("passenger-1")
                .userRole(UserType.PASSENGER)
                .tripId("trip-1")
                .bookingId("booking-1")
                .rating(3)
                .comment("Ok")
                .build();

        when(userRepository.existsByUserId("passenger-1")).thenReturn(true);
        when(tripServiceClient.isTripCompleted("trip-1")).thenReturn(true);
        when(tripServiceClient.isBookingCompleted("booking-1")).thenReturn(true);
        when(reviewRepository.existsByReviewerIdAndTripIdAndBookingId("passenger-1", "trip-1", "booking-1"))
                .thenReturn(true); // already reviewed

        assertThrows(ReviewAlreadyExistsException.class, () -> reviewService.submitDriverReview(request));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void submitPassengerReview_whenBookingBelongsToAnotherDriver_shouldThrowUnauthorized() {
        SubmitReviewRequest request = SubmitReviewRequest.builder()
                .userId("driver-1")
                .userRole(UserType.DRIVER)
                .tripId("trip-1")
                .bookingId("booking-1")
                .rating(4)
                .comment("Fine")
                .build();

        when(userRepository.existsByUserId("driver-1")).thenReturn(true);
        when(tripServiceClient.isTripCompleted("trip-1")).thenReturn(true);
        when(tripServiceClient.isBookingCompleted("booking-1")).thenReturn(true);
        when(reviewRepository.existsByReviewerIdAndTripIdAndBookingId("driver-1", "trip-1", "booking-1"))
                .thenReturn(false);
        when(tripServiceClient.getTripDriverId("trip-1")).thenReturn("another-driver");

        assertThrows(UnauthorizedReviewerException.class, () -> reviewService.submitPassengerReview(request));
    }

    @Test
    void submitDriverReview_whenBookingNotCompleted_shouldThrowTripNotCompleted() {
        SubmitReviewRequest request = SubmitReviewRequest.builder()
                .userId("passenger-1")
                .userRole(UserType.PASSENGER)
                .tripId("trip-1")
                .bookingId("booking-1")
                .rating(5)
                .comment("Good")
                .build();

        when(userRepository.existsByUserId("passenger-1")).thenReturn(true);
        when(tripServiceClient.isTripCompleted("trip-1")).thenReturn(true);
        when(tripServiceClient.isBookingCompleted("booking-1")).thenReturn(false); // booking not done

        assertThrows(TripNotCompletedException.class, () -> reviewService.submitDriverReview(request));
        verify(reviewRepository, never()).save(any());
    }
}
