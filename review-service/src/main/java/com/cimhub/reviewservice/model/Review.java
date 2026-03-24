package com.cimhub.reviewservice.model;

import com.cimhub.common.enums.UserType;
import jakarta.persistence.*;
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
 * Review entity supporting both MongoDB and relational databases.
 * A review is given by one user (reviewer) about another user (reviewee) after a completed trip.
 */
@Document(collection = "reviews")
@Entity
@Table(name = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String reviewId;

    @Field("tripId")
    @Column(name = "trip_id")
    private String tripId;

    @Field("bookingId")
    @Column(name = "booking_id")
    private String bookingId;

    @Field("reviewerId")
    @Column(name = "reviewer_id")
    private String reviewerId;

    @Enumerated(EnumType.STRING)
    @Field("reviewerType")
    @Column(name = "reviewer_type")
    private UserType reviewerType;

    @Field("revieweeId")
    @Column(name = "reviewee_id")
    private String revieweeId;

    @Enumerated(EnumType.STRING)
    @Field("revieweeType")
    @Column(name = "reviewee_type")
    private UserType revieweeType;

    @Min(1)
    @Max(5)
    @Field("rating")
    @Column(name = "rating")
    private int rating;

    @Field("comment")
    @Column(name = "comment", length = 500)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Field("status")
    @Column(name = "status")
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PUBLISHED;

    @Field("createdAt")
    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();
}
