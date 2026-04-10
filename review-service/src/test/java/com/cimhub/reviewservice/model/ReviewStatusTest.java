package com.cimhub.reviewservice.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReviewStatusTest {

    @Test
    void enumValues_shouldRemainStable() {
        ReviewStatus[] values = ReviewStatus.values();

        assertEquals(4, values.length);
        assertEquals(ReviewStatus.PENDING, values[0]);
        assertEquals(ReviewStatus.PUBLISHED, values[1]);
        assertEquals(ReviewStatus.FLAGGED, values[2]);
        assertEquals(ReviewStatus.REMOVED, values[3]);
    }
}
