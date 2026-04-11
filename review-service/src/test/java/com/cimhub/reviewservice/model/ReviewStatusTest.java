package com.cimhub.reviewservice.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import org.junit.jupiter.api.Test;

class ReviewStatusTest {

    @Test
    void enumValues_shouldContainExpectedStatuses() {
        EnumSet<ReviewStatus> allStatuses = EnumSet.allOf(ReviewStatus.class);

        assertEquals(4, allStatuses.size());
        assertTrue(allStatuses.contains(ReviewStatus.PENDING));
        assertTrue(allStatuses.contains(ReviewStatus.PUBLISHED));
        assertTrue(allStatuses.contains(ReviewStatus.FLAGGED));
        assertTrue(allStatuses.contains(ReviewStatus.REMOVED));
    }

    @Test
    void valueOf_shouldRoundTripKnownValue() {
        assertEquals(ReviewStatus.PENDING, ReviewStatus.valueOf("PENDING"));
    }

    @Test
    void valueOf_shouldThrowForUnknownValue() {
        assertThrows(IllegalArgumentException.class, () -> ReviewStatus.valueOf("nope"));
    }
}
