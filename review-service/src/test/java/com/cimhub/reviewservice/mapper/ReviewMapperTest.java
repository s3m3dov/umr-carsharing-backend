package com.cimhub.reviewservice.mapper;

import com.cimhub.common.enums.UserType;
import com.cimhub.reviewservice.dto.response.ReviewResponse;
import com.cimhub.reviewservice.model.Review;
import com.cimhub.reviewservice.model.ReviewStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReviewMapperTest {

    @Test
    void toResponse_shouldMapAllFields() {
        Review review = Review.builder()
                .reviewId("review-1")
                .tripId("trip-1")
                .bookingId("booking-1")
                .reviewerId("reviewer-1")
                .reviewerType(UserType.PASSENGER)
                .revieweeId("reviewee-1")
                .revieweeType(UserType.DRIVER)
                .rating(5)
                .comment("Great trip")
                .status(ReviewStatus.PUBLISHED)
                .createdAt(Instant.parse("2030-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2030-01-01T00:01:00Z"))
                .build();

        ReviewResponse response = ReviewMapper.toResponse(review);

        assertEquals("review-1", response.reviewId());
        assertEquals("trip-1", response.tripId());
        assertEquals("booking-1", response.bookingId());
        assertEquals("reviewer-1", response.reviewerId());
        assertEquals(UserType.PASSENGER, response.reviewerType());
        assertEquals("reviewee-1", response.revieweeId());
        assertEquals(UserType.DRIVER, response.revieweeType());
        assertEquals(5, response.rating());
        assertEquals("Great trip", response.comment());
        assertEquals(ReviewStatus.PUBLISHED, response.status());
        assertEquals(Instant.parse("2030-01-01T00:00:00Z"), response.createdAt());
        assertEquals(Instant.parse("2030-01-01T00:01:00Z"), response.updatedAt());
    }
}
