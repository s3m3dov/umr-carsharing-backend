package com.cimhub.reviewservice.dto.request;

import com.cimhub.reviewservice.dto.examples.ReviewApiDoc;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Schema(
        name = "SubmitReviewRequest",
        description = "Request to submit a review for a completed trip",
        example = ReviewApiDoc.SUBMIT_REVIEW_REQUEST)
@Builder
public record SubmitReviewRequest(
        @NotBlank(message = "tripId is required") @Schema(description = "The ID of the trip being reviewed")
        String tripId,

        @NotBlank(message = "bookingId is required")
        @Schema(description = "The ID of the booking associated with the trip")
        String bookingId,

        @Min(value = 1, message = "rating must be at least 1")
        @Max(value = 5, message = "rating must be at most 5")
        @Schema(description = "Rating from 1 to 5", example = "5")
        int rating,

        @Schema(description = "Optional review comment") String comment) {}
