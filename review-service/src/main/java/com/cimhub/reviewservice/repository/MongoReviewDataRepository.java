package com.cimhub.reviewservice.repository;

import com.cimhub.common.enums.UserType;
import com.cimhub.reviewservice.model.Review;
import com.cimhub.reviewservice.model.ReviewStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoReviewDataRepository extends MongoRepository<Review, String> {

    List<Review> findByRevieweeId(String revieweeId);

    List<Review> findByRevieweeIdAndRevieweeType(String revieweeId, UserType revieweeType);

    List<Review> findByTripId(String tripId);

    List<Review> findByReviewerId(String reviewerId);

    boolean existsByReviewerIdAndTripIdAndBookingId(String reviewerId, String tripId, String bookingId);

    Optional<Review> findByReviewerIdAndTripIdAndBookingId(String reviewerId, String tripId, String bookingId);

    long countByRevieweeId(String revieweeId);

    // Admin queries
    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);

    long countByStatus(ReviewStatus status);
}
