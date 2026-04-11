package com.cimhub.reviewservice.controller;

import com.cimhub.common.enums.UserType;
import com.cimhub.reviewservice.dto.request.SubmitReviewRequest;
import com.cimhub.reviewservice.dto.response.AverageRatingResponse;
import com.cimhub.reviewservice.service.ReviewService;
import com.cimhub.reviewservice.testsupport.ReviewFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    private ReviewController reviewController;

    @BeforeEach
    void setUp() {
        reviewController = new ReviewController(reviewService);
    }

    @Test
    void submitDriverReview_shouldReturnCreatedResponse() {
        SubmitReviewRequest request = SubmitReviewRequest.builder()
                .userId("passenger@example.com")
                .userRole(UserType.PASSENGER)
                .tripId("trip-1")
                .bookingId("booking-1")
                .rating(5)
                .comment("Great")
                .build();
        var responseBody = ReviewFixtures.reviewResponse("review-1");
        when(reviewService.submitDriverReview(request)).thenReturn(responseBody);

        ResponseEntity<?> response = reviewController.submitDriverReview(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(reviewService).submitDriverReview(request);
    }

    @Test
    void submitPassengerReview_shouldReturnCreatedResponse() {
        SubmitReviewRequest request = SubmitReviewRequest.builder()
                .userId("driver@example.com")
                .userRole(UserType.DRIVER)
                .tripId("trip-1")
                .bookingId("booking-1")
                .rating(5)
                .comment("Great")
                .build();
        var responseBody = ReviewFixtures.reviewResponse("review-2");
        when(reviewService.submitPassengerReview(request)).thenReturn(responseBody);

        ResponseEntity<?> response = reviewController.submitPassengerReview(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(reviewService).submitPassengerReview(request);
    }

    @Test
    void getDriverReviews_shouldReturnOkResponse() {
        when(reviewService.getDriverReviews("driver-1")).thenReturn(List.of(ReviewFixtures.reviewResponse("review-1")));

        ResponseEntity<?> response = reviewController.getDriverReviews("driver-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reviewService).getDriverReviews("driver-1");
    }

    @Test
    void getPassengerReviews_shouldReturnOkResponse() {
        when(reviewService.getPassengerReviews("passenger-1")).thenReturn(List.of(ReviewFixtures.reviewResponse("review-3")));

        ResponseEntity<?> response = reviewController.getPassengerReviews("passenger-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reviewService).getPassengerReviews("passenger-1");
    }

    @Test
    void getDriverAverageRating_shouldReturnOkResponse() {
        AverageRatingResponse average = AverageRatingResponse.builder()
                .subjectId("driver-1")
                .averageRating(4.5)
                .totalReviews(2)
                .build();
        when(reviewService.getDriverAverageRating("driver-1")).thenReturn(average);

        ResponseEntity<?> response = reviewController.getDriverAverageRating("driver-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reviewService).getDriverAverageRating("driver-1");
    }

    @Test
    void getPassengerAverageRating_shouldReturnOkResponse() {
        AverageRatingResponse average = AverageRatingResponse.builder()
                .subjectId("passenger-1")
                .averageRating(3.0)
                .totalReviews(1)
                .build();
        when(reviewService.getPassengerAverageRating("passenger-1")).thenReturn(average);

        ResponseEntity<?> response = reviewController.getPassengerAverageRating("passenger-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reviewService).getPassengerAverageRating("passenger-1");
    }
}
