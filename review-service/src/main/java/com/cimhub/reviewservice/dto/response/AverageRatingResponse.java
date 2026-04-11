package com.cimhub.reviewservice.dto.response;

import com.cimhub.reviewservice.dto.examples.ReviewApiDoc;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(
        name = "AverageRatingResponse",
        description = "Average rating information for a driver",
        example = ReviewApiDoc.GET_AVERAGE_RATING_RESPONSE)
@Builder
public record AverageRatingResponse(
        @Schema(description = "ID of the subject (driver or user)")
        String subjectId,

        @Schema(description = "Average rating for the subject")
        double averageRating,

        @Schema(description = "Total number of reviews for the subject")
        long totalReviews) {}
