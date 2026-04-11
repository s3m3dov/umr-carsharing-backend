package com.cimhub.reviewservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.service.AuditService;
import com.cimhub.reviewservice.dto.response.ReviewResponse;
import com.cimhub.reviewservice.model.Review;
import com.cimhub.reviewservice.model.ReviewStatus;
import com.cimhub.reviewservice.repository.MongoReviewDataRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class AdminReviewServiceTest {

    @Mock
    private MongoReviewDataRepository reviewDataRepository;

    @Mock
    private AuditService auditService;

    private AdminReviewService adminReviewService;

    @BeforeEach
    void setUp() {
        adminReviewService = new AdminReviewService(reviewDataRepository, auditService);
    }

    @Test
    void flagReview_shouldUpdateStatusAndWriteAuditLog() {
        Review review = Review.builder()
                .reviewId("review-1")
                .tripId("trip-1")
                .status(ReviewStatus.PUBLISHED)
                .build();

        when(reviewDataRepository.findById("review-1")).thenReturn(Optional.of(review));
        when(reviewDataRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewResponse response = adminReviewService.flagReview("review-1", "admin@example.com");

        assertEquals(ReviewStatus.FLAGGED, response.status());

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewDataRepository).save(reviewCaptor.capture());
        assertEquals(ReviewStatus.FLAGGED, reviewCaptor.getValue().getStatus());

        verify(auditService).logFlag(EntityType.REVIEW, "review-1", "admin@example.com", "Review flagged by admin");
    }

    @Test
    void getReview_whenNotFound_shouldThrowResourceNotFound() {
        when(reviewDataRepository.findById("missing-review")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminReviewService.getReview("missing-review"));
    }

    @Test
    void listReviews_whenStatusProvided_shouldCallFilteredRepository() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Review> page = new PageImpl<>(List.of(
                Review.builder().reviewId("r-1").status(ReviewStatus.FLAGGED).build()));
        when(reviewDataRepository.findByStatus(ReviewStatus.FLAGGED, pageable)).thenReturn(page);

        Page<ReviewResponse> response = adminReviewService.listReviews(ReviewStatus.FLAGGED, pageable);

        assertEquals(1, response.getContent().size());
        verify(reviewDataRepository).findByStatus(ReviewStatus.FLAGGED, pageable);
    }

    @Test
    void listReviews_whenStatusIsNull_shouldCallFindAll() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Review> page = new PageImpl<>(List.of(
                Review.builder().reviewId("r-2").status(ReviewStatus.PUBLISHED).build()));
        when(reviewDataRepository.findAll(pageable)).thenReturn(page);

        Page<ReviewResponse> response = adminReviewService.listReviews(null, pageable);

        assertEquals(1, response.getContent().size());
        verify(reviewDataRepository).findAll(pageable);
    }

    @Test
    void removeReview_shouldSetRemovedStatusAndLogDelete() {
        Review review = Review.builder()
                .reviewId("review-2")
                .status(ReviewStatus.PUBLISHED)
                .build();
        when(reviewDataRepository.findById("review-2")).thenReturn(Optional.of(review));

        adminReviewService.removeReview("review-2", "admin@example.com");

        verify(reviewDataRepository).save(review);
        assertEquals(ReviewStatus.REMOVED, review.getStatus());
        verify(auditService).logDelete(EntityType.REVIEW, "review-2", "admin@example.com", "Review removed by admin");
    }

    @Test
    void publishReview_shouldSetPublishedStatusAndLogPublish() {
        Review review = Review.builder()
                .reviewId("review-3")
                .status(ReviewStatus.FLAGGED)
                .build();
        when(reviewDataRepository.findById("review-3")).thenReturn(Optional.of(review));

        ReviewResponse response = adminReviewService.publishReview("review-3", "admin@example.com");

        verify(reviewDataRepository).save(review);
        assertEquals(ReviewStatus.PUBLISHED, review.getStatus());
        assertEquals(ReviewStatus.PUBLISHED, response.status());
        verify(auditService)
                .logPublish(EntityType.REVIEW, "review-3", "admin@example.com", "Review published by admin");
    }

    @Test
    void removeReview_whenNotFound_shouldThrowResourceNotFound() {
        when(reviewDataRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class, () -> adminReviewService.removeReview("missing", "admin@example.com"));
    }

    @Test
    void publishReview_whenNotFound_shouldThrowResourceNotFound() {
        when(reviewDataRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminReviewService.publishReview("missing", "admin@example.com"));
    }
}
