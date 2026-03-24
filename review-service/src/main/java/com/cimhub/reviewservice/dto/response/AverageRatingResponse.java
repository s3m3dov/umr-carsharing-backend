package com.cimhub.reviewservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record AverageRatingResponse(
        @Schema(description = "ID of the driver")
        String driverId,

        @Schema(description = "Average rating for the driver")
        double averageRating,

        @Schema(description = "Total number of reviews for the driver")
        long totalReviews) {
}
