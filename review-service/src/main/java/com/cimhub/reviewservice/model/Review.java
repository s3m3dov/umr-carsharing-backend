package com.cimhub.reviewservice.model;

import com.cimhub.common.enums.UserType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

/**
 * Review entity for storing user reviews.
 * Uses MongoDB for persistence.
 * Stored in "reviews" collection.
 */
@Document(collection = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    private String reviewId;

    @Field("tripId")
    private String tripId;

    @Field("bookingId")
    private String bookingId;

    @Field("reviewerId")
    private String reviewerId;

    @Field("reviewerType")
    private UserType reviewerType;

    @Field("revieweeId")
    private String revieweeId;

    @Field("revieweeType")
    private UserType revieweeType;

    @Min(1)
    @Max(5)
    @Field("rating")
    private int rating;

    @Field("comment")
    private String comment;

    @Field("status")
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PUBLISHED;

    @Field("createdAt")
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Field("updatedAt")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}
