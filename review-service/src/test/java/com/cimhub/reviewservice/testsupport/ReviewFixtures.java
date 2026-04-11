package com.cimhub.reviewservice.testsupport;

import com.cimhub.common.enums.UserType;
import com.cimhub.reviewservice.dto.response.ReviewResponse;
import com.cimhub.reviewservice.model.Review;
import com.cimhub.reviewservice.model.ReviewStatus;
import java.time.Instant;

public final class ReviewFixtures {

    private ReviewFixtures() {}

    public static ReviewResponse reviewResponse(String reviewId) {
        return ReviewResponse.builder()
                .reviewId(reviewId)
                .tripId("trip-1")
                .bookingId("booking-1")
                .reviewerId("reviewer")
                .reviewerType(UserType.PASSENGER)
                .revieweeId("reviewee")
                .revieweeType(UserType.DRIVER)
                .rating(5)
                .comment("Great")
                .status(ReviewStatus.PUBLISHED)
                .createdAt(Instant.parse("2030-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2030-01-01T00:00:00Z"))
                .build();
    }

    public static Review review(String reviewId) {
        return Review.builder()
                .reviewId(reviewId)
                .tripId("trip-1")
                .bookingId("booking-1")
                .reviewerId("reviewer")
                .reviewerType(UserType.PASSENGER)
                .revieweeId("reviewee")
                .revieweeType(UserType.DRIVER)
                .rating(5)
                .comment("Great")
                .status(ReviewStatus.PUBLISHED)
                .createdAt(Instant.parse("2030-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2030-01-01T00:00:00Z"))
                .build();
    }
}
