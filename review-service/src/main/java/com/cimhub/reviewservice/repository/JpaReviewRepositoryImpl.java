package com.cimhub.reviewservice.repository;

import com.cimhub.reviewservice.model.Review;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * JPA (relational database) implementation of the ReviewRepository interface.
 * Delegates to Spring Data JPA for database operations.
 * Active when profile "relational" is enabled.
 */
@Component
@Profile("relational")
@RequiredArgsConstructor
@Slf4j
public class JpaReviewRepositoryImpl implements ReviewRepository {

    private final JpaReviewDataRepository jpaReviewDataRepository;

    @Override
    public Review save(Review review) {
        log.debug("Saving review using JPA: {}", review.getReviewId());
        return jpaReviewDataRepository.save(review);
    }

    @Override
    public Optional<Review> findById(String id) {
        log.debug("Finding review by ID using JPA: {}", id);
        return jpaReviewDataRepository.findById(id);
    }

    @Override
    public List<Review> findByRevieweeId(String revieweeId) {
        log.debug("Finding reviews by reviewee ID using JPA: {}", revieweeId);
        return jpaReviewDataRepository.findByRevieweeId(revieweeId);
    }

    @Override
    public List<Review> findByTripId(String tripId) {
        log.debug("Finding reviews by trip ID using JPA: {}", tripId);
        return jpaReviewDataRepository.findByTripId(tripId);
    }

    @Override
    public List<Review> findByReviewerId(String reviewerId) {
        log.debug("Finding reviews by reviewer ID using JPA: {}", reviewerId);
        return jpaReviewDataRepository.findByReviewerId(reviewerId);
    }

    @Override
    public boolean existsByReviewerIdAndTripIdAndBookingId(String reviewerId, String tripId, String bookingId) {
        log.debug("Checking existence of review using JPA - reviewerId: {}, tripId: {}, bookingId: {}",
                reviewerId, tripId, bookingId);
        return jpaReviewDataRepository.existsByReviewerIdAndTripIdAndBookingId(reviewerId, tripId, bookingId);
    }

    @Override
    public Optional<Review> findByReviewerIdAndTripIdAndBookingId(String reviewerId, String tripId, String bookingId) {
        log.debug("Finding review using JPA - reviewerId: {}, tripId: {}, bookingId: {}",
                reviewerId, tripId, bookingId);
        return jpaReviewDataRepository.findByReviewerIdAndTripIdAndBookingId(reviewerId, tripId, bookingId);
    }

    @Override
    public long countByRevieweeId(String revieweeId) {
        log.debug("Counting reviews by reviewee ID using JPA: {}", revieweeId);
        return jpaReviewDataRepository.countByRevieweeId(revieweeId);
    }
}
