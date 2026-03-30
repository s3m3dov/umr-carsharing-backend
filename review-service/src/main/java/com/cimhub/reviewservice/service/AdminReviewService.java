package com.cimhub.reviewservice.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.service.AuditService;
import com.cimhub.reviewservice.dto.response.ReviewResponse;
import com.cimhub.reviewservice.model.Review;
import com.cimhub.reviewservice.model.ReviewStatus;
import com.cimhub.reviewservice.repository.MongoReviewDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final MongoReviewDataRepository reviewDataRepository;
    private final AuditService auditService;

    public Page<ReviewResponse> listReviews(ReviewStatus status, Pageable pageable) {
        Page<Review> reviews;
        if (status != null) {
            reviews = reviewDataRepository.findByStatus(status, pageable);
        } else {
            reviews = reviewDataRepository.findAll(pageable);
        }
        return reviews.map(this::mapToResponse);
    }

    public ReviewResponse getReview(String reviewId) {
        Review review = reviewDataRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));
        return mapToResponse(review);
    }

    public ReviewResponse flagReview(String reviewId, String adminEmail) {
        Review review = reviewDataRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));
        review.setStatus(ReviewStatus.FLAGGED);
        reviewDataRepository.save(review);
        auditService.logFlag(EntityType.REVIEW, reviewId, adminEmail, "Review flagged by admin");
        log.info("Review {} flagged by admin {}", reviewId, adminEmail);
        return mapToResponse(review);
    }

    public void removeReview(String reviewId, String adminEmail) {
        Review review = reviewDataRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));
        review.setStatus(ReviewStatus.REMOVED);
        reviewDataRepository.save(review);
        auditService.logDelete(EntityType.REVIEW, reviewId, adminEmail, "Review removed by admin");
        log.info("Review {} removed by admin {}", reviewId, adminEmail);
    }

    public ReviewResponse publishReview(String reviewId, String adminEmail) {
        Review review = reviewDataRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));
        review.setStatus(ReviewStatus.PUBLISHED);
        reviewDataRepository.save(review);
        auditService.logPublish(EntityType.REVIEW, reviewId, adminEmail, "Review published by admin");
        log.info("Review {} published by admin {}", reviewId, adminEmail);
        return mapToResponse(review);
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .tripId(review.getTripId())
                .bookingId(review.getBookingId())
                .reviewerId(review.getReviewerId())
                .reviewerType(review.getReviewerType())
                .revieweeId(review.getRevieweeId())
                .revieweeType(review.getRevieweeType())
                .rating(review.getRating())
                .comment(review.getComment())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .build();
    }

}
