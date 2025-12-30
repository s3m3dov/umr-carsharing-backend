package com.singhv.tripservice.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Service to handle timezone conversions for trip times.
 * Ensures users see trip times in their preferred timezone.
 */
@Service
public class TimezoneConversionService {

    /**
     * Convert a UTC Instant to a ZonedDateTime in the specified timezone
     *
     * @param utcInstant The UTC time from database
     * @param targetTimezone The target timezone (e.g., "Asia/Kolkata", "Europe/Berlin")
     * @return ZonedDateTime in the target timezone
     */
    public ZonedDateTime convertToTimezone(Instant utcInstant, String targetTimezone) {
        ZoneId zoneId = ZoneId.of(targetTimezone);
        return utcInstant.atZone(zoneId);
    }

    /**
     * Convert a UTC Instant to the original trip timezone
     * Useful for displaying trip time in the location where it will occur
     *
     * @param utcInstant The UTC time from database
     * @param tripTimezone The timezone where the trip takes place
     * @return ZonedDateTime in the trip's original timezone
     */
    public ZonedDateTime convertToTripTimezone(Instant utcInstant, String tripTimezone) {
        return convertToTimezone(utcInstant, tripTimezone);
    }

    /**
     * Convert a UTC Instant to user's local timezone
     * Frontend should pass user's timezone preference
     *
     * @param utcInstant The UTC time from database
     * @param userTimezone User's preferred timezone
     * @return ZonedDateTime in user's timezone
     */
    public ZonedDateTime convertToUserTimezone(Instant utcInstant, String userTimezone) {
        return convertToTimezone(utcInstant, userTimezone);
    }

    /**
     * Get formatted time string for display
     *
     * @param utcInstant The UTC time from database
     * @param timezone Target timezone
     * @param format Format pattern (e.g., "yyyy-MM-dd HH:mm")
     * @return Formatted time string
     */
    public String formatInTimezone(Instant utcInstant, String timezone, String format) {
        ZonedDateTime zonedTime = convertToTimezone(utcInstant, timezone);
        java.time.format.DateTimeFormatter formatter =
            java.time.format.DateTimeFormatter.ofPattern(format);
        return zonedTime.format(formatter);
    }
}

