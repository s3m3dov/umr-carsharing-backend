package com.cimhub.reviewservice.dto.response;

import com.cimhub.common.enums.UserType;
import com.cimhub.reviewservice.model.ReviewStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;

@Builder
public record ReviewResponse(
        @Schema(description = "Unique review ID")
        String reviewId,

        @Schema(description = "ID of the trip being reviewed")
        String tripId,

        @Schema(description = "ID of the booking associated with the trip")
        String bookingId,

        @Schema(description = "ID of the user who submitted the review")
        String reviewerId,

        @Schema(description = "Type of reviewer (DRIVER or PASSENGER)")
        UserType reviewerType,

        @Schema(description = "ID of the user being reviewed")
        String revieweeId,

        @Schema(description = "Type of reviewee (DRIVER or PASSENGER)")
        UserType revieweeType,

        @Schema(description = "Rating from 1 to 5")
        int rating,

        @Schema(description = "Review comment text")
        String comment,

        @Schema(description = "Status of the review (PENDING or PUBLISHED)")
        ReviewStatus status,

        @Schema(description = "Timestamp when the review was created", example = "2026-03-24T12:34:56.789Z")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        Instant createdAt) {
}
