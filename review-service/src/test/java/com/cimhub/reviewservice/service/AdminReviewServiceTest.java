package com.cimhub.reviewservice.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.service.AuditService;
import com.cimhub.reviewservice.dto.response.ReviewResponse;
import com.cimhub.reviewservice.model.Review;
import com.cimhub.reviewservice.model.ReviewStatus;
import com.cimhub.reviewservice.repository.MongoReviewDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}
