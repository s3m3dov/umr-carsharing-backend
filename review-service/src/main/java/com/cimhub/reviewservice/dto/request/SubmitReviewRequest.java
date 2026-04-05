package com.cimhub.reviewservice.dto.request;

import com.cimhub.common.enums.UserType;
import com.cimhub.reviewservice.controller.ReviewApiExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Schema(
        name = "SubmitReviewRequest",
        description = "Request to submit a review for a completed trip",
        example = ReviewApiExamples.SUBMIT_REVIEW_REQUEST
)

@Builder
public record SubmitReviewRequest(
        @NotBlank(message = "userId is required")
        @Schema(description = "The ID of the user submitting the review", example = "jane.passenger@example.com")
        String userId,

        @NotNull(message = "userRole is required")
        @Schema(description = "The role of the reviewer", example = "PASSENGER")
        UserType userRole,

        @NotBlank(message = "tripId is required")
        @Schema(description = "The ID of the trip being reviewed")
        String tripId,

        @NotBlank(message = "bookingId is required")
        @Schema(description = "The ID of the booking associated with the trip")
        String bookingId,

        @Min(value = 1, message = "rating must be at least 1")
        @Max(value = 5, message = "rating must be at most 5")
        @Schema(description = "Rating from 1 to 5", example = "5")
        int rating,

        @Schema(description = "Optional review comment")
        String comment) {
}
