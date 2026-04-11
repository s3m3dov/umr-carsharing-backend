package com.cimhub.tripservice.service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TimezoneConversionServiceTest {

    private TimezoneConversionService timezoneConversionService;

    @BeforeEach
    void setUp() {
        timezoneConversionService = new TimezoneConversionService();
    }

    @Test
    void getTimeZoneId_shouldReturnUtcPlaceholder() {
        String timezone = TimezoneConversionService.getTimeZoneId(52.5, 13.4);

        assertEquals("UTC", timezone);
    }

    @Test
    void convertToTimezone_shouldConvertUtcInstantToTargetZone() {
        Instant utcInstant = Instant.parse("2026-06-01T12:00:00Z");

        ZonedDateTime converted = timezoneConversionService.convertToTimezone(utcInstant, "Europe/Berlin");

        assertEquals("Europe/Berlin", converted.getZone().getId());
        assertEquals(14, converted.getHour());
        assertEquals(0, converted.getMinute());
    }

    @Test
    void convertToTripTimezone_shouldDelegateToGenericConversion() {
        Instant utcInstant = Instant.parse("2026-01-10T18:30:00Z");

        ZonedDateTime converted = timezoneConversionService.convertToTripTimezone(utcInstant, "Asia/Tbilisi");

        assertEquals("Asia/Tbilisi", converted.getZone().getId());
        assertEquals(22, converted.getHour());
        assertEquals(30, converted.getMinute());
    }

    @Test
    void convertToUserTimezone_shouldDelegateToGenericConversion() {
        Instant utcInstant = Instant.parse("2026-01-10T18:30:00Z");

        ZonedDateTime converted = timezoneConversionService.convertToUserTimezone(utcInstant, "America/New_York");

        assertEquals("America/New_York", converted.getZone().getId());
        assertEquals(13, converted.getHour());
        assertEquals(30, converted.getMinute());
    }

    @Test
    void formatInTimezone_shouldUseProvidedPattern() {
        Instant utcInstant = Instant.parse("2026-12-24T20:15:00Z");

        String formatted =
                timezoneConversionService.formatInTimezone(utcInstant, "Europe/Berlin", "yyyy-MM-dd HH:mm z");

        assertEquals("2026-12-24 21:15 CET", formatted);
    }
}
