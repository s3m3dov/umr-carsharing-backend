package com.cimhub.reviewservice.repository;

import com.cimhub.reviewservice.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository interface for Review documents.
 * This is used internally by MongoReviewRepositoryImpl.
 */
@Repository
public interface MongoReviewDataRepository extends MongoRepository<Review, String> {

    /**
     * Find all reviews for a specific reviewee (person being reviewed).
     */
    List<Review> findByRevieweeId(String revieweeId);

    /**
     * Find all reviews for a specific trip.
     */
    List<Review> findByTripId(String tripId);

    /**
     * Find all reviews submitted by a specific reviewer.
     */
    List<Review> findByReviewerId(String reviewerId);

    /**
     * Check if a reviewer has already reviewed for a specific trip/booking.
     */
    boolean existsByReviewerIdAndTripIdAndBookingId(String reviewerId, String tripId, String bookingId);

    /**
     * Find a specific review by reviewer, trip, and booking.
     */
    Optional<Review> findByReviewerIdAndTripIdAndBookingId(String reviewerId, String tripId, String bookingId);

    /**
     * Count all reviews for a reviewee.
     */
    long countByRevieweeId(String revieweeId);
}
