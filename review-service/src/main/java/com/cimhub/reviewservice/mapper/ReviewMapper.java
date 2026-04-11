package com.cimhub.reviewservice.mapper;

import com.cimhub.reviewservice.dto.response.ReviewResponse;
import com.cimhub.reviewservice.model.Review;

public final class ReviewMapper {

    private ReviewMapper() {}

    public static ReviewResponse toResponse(Review review) {
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
