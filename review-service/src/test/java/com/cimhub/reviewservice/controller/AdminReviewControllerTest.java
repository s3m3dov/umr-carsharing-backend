package com.cimhub.reviewservice.controller;

import com.cimhub.common.exception.ForbiddenException;
import com.cimhub.common.enums.UserType;
import com.cimhub.reviewservice.dto.response.ReviewResponse;
import com.cimhub.reviewservice.model.ReviewStatus;
import com.cimhub.reviewservice.service.AdminReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminReviewControllerTest {

    @Mock
    private AdminReviewService adminReviewService;

    private AdminReviewController adminReviewController;

    @BeforeEach
    void setUp() {
        adminReviewController = new AdminReviewController(adminReviewService);
    }

    @Test
    void listReviews_shouldAllowAdminAndReturnData() {
        Page<ReviewResponse> page = new PageImpl<>(List.of(ReviewResponse.builder().reviewId("r-1").build()));
        when(adminReviewService.listReviews(ReviewStatus.FLAGGED, PageRequest.of(0, 10))).thenReturn(page);

        ResponseEntity<?> response = adminReviewController.listReviews(
                UserType.ADMIN.name(),
                ReviewStatus.FLAGGED,
                PageRequest.of(0, 10));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(adminReviewService).listReviews(ReviewStatus.FLAGGED, PageRequest.of(0, 10));
    }

    @Test
    void listReviews_whenNotAdmin_shouldThrowForbidden() {
        assertThrows(ForbiddenException.class,
                () -> adminReviewController.listReviews(UserType.DRIVER.name(), null, PageRequest.of(0, 10)));
    }

    @Test
    void getReview_shouldDelegateForAdmin() {
        when(adminReviewService.getReview("r-1")).thenReturn(ReviewResponse.builder().reviewId("r-1").build());

        ResponseEntity<?> response = adminReviewController.getReview(UserType.ADMIN.name(), "r-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(adminReviewService).getReview("r-1");
    }

    @Test
    void flagReview_shouldDelegateForAdmin() {
        when(adminReviewService.flagReview("r-1", "admin@example.com"))
                .thenReturn(ReviewResponse.builder().reviewId("r-1").status(ReviewStatus.FLAGGED).build());

        ResponseEntity<?> response = adminReviewController.flagReview(UserType.ADMIN.name(), "admin@example.com", "r-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(adminReviewService).flagReview("r-1", "admin@example.com");
    }

    @Test
    void removeReview_shouldDelegateForAdmin() {
        ResponseEntity<?> response = adminReviewController.removeReview(UserType.ADMIN.name(), "admin@example.com", "r-2");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(adminReviewService).removeReview("r-2", "admin@example.com");
    }

    @Test
    void publishReview_shouldDelegateForAdmin() {
        when(adminReviewService.publishReview("r-3", "admin@example.com"))
                .thenReturn(ReviewResponse.builder().reviewId("r-3").status(ReviewStatus.PUBLISHED).build());

        ResponseEntity<?> response = adminReviewController.publishReview(UserType.ADMIN.name(), "admin@example.com", "r-3");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(adminReviewService).publishReview("r-3", "admin@example.com");
    }
}
