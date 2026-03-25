package com.cimhub.reviewservice.repository;

import com.cimhub.reviewservice.model.Review;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB implementation of the ReviewRepository interface.
 * Delegates to Spring Data MongoDB for database operations.
 */
@Component
@Profile("mongodb")
@RequiredArgsConstructor
@Slf4j
public class MongoReviewRepositoryImpl implements ReviewRepository {

    private final MongoReviewDataRepository mongoReviewDataRepository;

    @Override
    public Review save(Review review) {
        log.debug("Saving review using MongoDB: {}", review.getReviewId());
        return mongoReviewDataRepository.save(review);
    }

    @Override
    public Optional<Review> findById(String id) {
        log.debug("Finding review by ID using MongoDB: {}", id);
        return mongoReviewDataRepository.findById(id);
    }

    @Override
    public List<Review> findByRevieweeId(String revieweeId) {
        log.debug("Finding reviews by reviewee ID using MongoDB: {}", revieweeId);
        return mongoReviewDataRepository.findByRevieweeId(revieweeId);
    }

    @Override
    public List<Review> findByTripId(String tripId) {
        log.debug("Finding reviews by trip ID using MongoDB: {}", tripId);
        return mongoReviewDataRepository.findByTripId(tripId);
    }

    @Override
    public List<Review> findByReviewerId(String reviewerId) {
        log.debug("Finding reviews by reviewer ID using MongoDB: {}", reviewerId);
        return mongoReviewDataRepository.findByReviewerId(reviewerId);
    }

    @Override
    public boolean existsByReviewerIdAndTripIdAndBookingId(String reviewerId, String tripId, String bookingId) {
        log.debug("Checking existence of review using MongoDB - reviewerId: {}, tripId: {}, bookingId: {}",
                reviewerId, tripId, bookingId);
        return mongoReviewDataRepository.existsByReviewerIdAndTripIdAndBookingId(reviewerId, tripId, bookingId);
    }

    @Override
    public Optional<Review> findByReviewerIdAndTripIdAndBookingId(String reviewerId, String tripId, String bookingId) {
        log.debug("Finding review using MongoDB - reviewerId: {}, tripId: {}, bookingId: {}",
                reviewerId, tripId, bookingId);
        return mongoReviewDataRepository.findByReviewerIdAndTripIdAndBookingId(reviewerId, tripId, bookingId);
    }

    @Override
    public long countByRevieweeId(String revieweeId) {
        log.debug("Counting reviews by reviewee ID using MongoDB: {}", revieweeId);
        return mongoReviewDataRepository.countByRevieweeId(revieweeId);
    }
}
