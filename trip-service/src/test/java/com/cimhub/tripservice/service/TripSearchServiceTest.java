package com.cimhub.tripservice.service;

import com.cimhub.tripservice.dto.response.TripResponse;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.repository.TripsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripSearchServiceTest {

    @Mock
    private TripsRepository tripsRepository;

    @Mock
    private TimezoneConversionService timezoneService;

    @InjectMocks
    private TripSearchService tripSearchService;

    private Trip availableTrip;
    private Trip offeredTrip;
    private Trip completedTrip;

    @BeforeEach
    void setUp() {
        availableTrip = new Trip();
        availableTrip.setTripId("trip1");
        availableTrip.setDriverId("driver1");
        availableTrip.setVehicleNumber("ABC123");
        availableTrip.setTripStatus("AVAILABLE");
        availableTrip.setOfferedSeat(4);
        availableTrip.setCurrSeats(1);
        availableTrip.setTripStartDateTimeUTC(Instant.now().plusSeconds(86400));
        availableTrip.setTripTimezone("America/New_York");
        availableTrip.setPricePerKm(5.0);

        offeredTrip = new Trip();
        offeredTrip.setTripId("trip2");
        offeredTrip.setDriverId("driver2");
        offeredTrip.setVehicleNumber("XYZ789");
        offeredTrip.setTripStatus("OFFERED");
        offeredTrip.setOfferedSeat(3);
        offeredTrip.setCurrSeats(0);
        offeredTrip.setTripStartDateTimeUTC(Instant.now().plusSeconds(172800));
        offeredTrip.setTripTimezone("Europe/London");
        offeredTrip.setPricePerKm(4.5);

        completedTrip = new Trip();
        completedTrip.setTripId("trip3");
        completedTrip.setDriverId("driver3");
        completedTrip.setVehicleNumber("DEF456");
        completedTrip.setTripStatus("COMPLETED");
        completedTrip.setOfferedSeat(5);
        completedTrip.setCurrSeats(5);
        completedTrip.setTripStartDateTimeUTC(Instant.now().minusSeconds(86400));
        completedTrip.setTripTimezone("Asia/Tokyo");
    }

    @Test
    void searchAvailableTrips_whenTripsExist_thenReturnsOnlyAvailableAndOfferedTrips() {
        // Given
        when(tripsRepository.findAll()).thenReturn(Arrays.asList(availableTrip, offeredTrip, completedTrip));
        when(timezoneService.convertToTripTimezone(any(Instant.class), anyString()))
                .thenReturn(ZonedDateTime.now());
        when(timezoneService.convertToUserTimezone(any(Instant.class), anyString()))
                .thenReturn(ZonedDateTime.now());

        // When
        List<TripResponse> responses = tripSearchService.searchAvailableTrips("America/Los_Angeles");

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(TripResponse::tripId)
                .containsExactlyInAnyOrder("trip1", "trip2");
        assertThat(responses).extracting(TripResponse::tripStatus)
                .containsOnly("AVAILABLE", "OFFERED");

        verify(tripsRepository).findAll();
    }

    @Test
    void searchAvailableTrips_whenNoTripsExist_thenReturnsEmptyList() {
        // Given
        when(tripsRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<TripResponse> responses = tripSearchService.searchAvailableTrips("America/New_York");

        // Then
        assertThat(responses).isEmpty();
        verify(tripsRepository).findAll();
    }

    @Test
    void searchAvailableTrips_whenOnlyCompletedTrips_thenReturnsEmptyList() {
        // Given
        when(tripsRepository.findAll()).thenReturn(Collections.singletonList(completedTrip));

        // When
        List<TripResponse> responses = tripSearchService.searchAvailableTrips("Europe/Paris");

        // Then
        assertThat(responses).isEmpty();
    }

    @Test
    void searchAvailableTrips_whenCalled_thenCalculatesAvailableSeatsCorrectly() {
        // Given
        when(tripsRepository.findAll()).thenReturn(Collections.singletonList(availableTrip));
        when(timezoneService.convertToTripTimezone(any(Instant.class), anyString()))
                .thenReturn(ZonedDateTime.now());
        when(timezoneService.convertToUserTimezone(any(Instant.class), anyString()))
                .thenReturn(ZonedDateTime.now());

        // When
        List<TripResponse> responses = tripSearchService.searchAvailableTrips("UTC");

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).availableSeats()).isEqualTo(3); // 4 offered - 1 current
        assertThat(responses.get(0).offeredSeat()).isEqualTo(4);
    }

    @Test
    void searchAvailableTrips_whenCalled_thenConvertsTimezonesToUserTimezone() {
        // Given
        when(tripsRepository.findAll()).thenReturn(Collections.singletonList(availableTrip));

        ZonedDateTime tripTime = ZonedDateTime.now().plusDays(1);
        ZonedDateTime userTime = ZonedDateTime.now().plusDays(1).plusHours(3);

        when(timezoneService.convertToTripTimezone(any(Instant.class), eq("America/New_York")))
                .thenReturn(tripTime);
        when(timezoneService.convertToUserTimezone(any(Instant.class), eq("Europe/London")))
                .thenReturn(userTime);

        // When
        List<TripResponse> responses = tripSearchService.searchAvailableTrips("Europe/London");

        // Then
        assertThat(responses).hasSize(1);
        verify(timezoneService).convertToTripTimezone(availableTrip.getTripStartDateTimeUTC(), "America/New_York");
        verify(timezoneService).convertToUserTimezone(availableTrip.getTripStartDateTimeUTC(), "Europe/London");
    }

    @Test
    void searchAvailableTrips_whenCalled_thenIncludesAllTripDetails() {
        // Given
        when(tripsRepository.findAll()).thenReturn(Collections.singletonList(availableTrip));
        when(timezoneService.convertToTripTimezone(any(Instant.class), anyString()))
                .thenReturn(ZonedDateTime.now());
        when(timezoneService.convertToUserTimezone(any(Instant.class), anyString()))
                .thenReturn(ZonedDateTime.now());

        // When
        List<TripResponse> responses = tripSearchService.searchAvailableTrips("UTC");

        // Then
        assertThat(responses).hasSize(1);
        TripResponse response = responses.get(0);

        assertThat(response.tripId()).isEqualTo("trip1");
        assertThat(response.driverId()).isEqualTo("driver1");
        assertThat(response.vehicleNumber()).isEqualTo("ABC123");
        assertThat(response.tripStatus()).isEqualTo("AVAILABLE");
        assertThat(response.pricePerKm()).isEqualTo(5.0);
        assertThat(response.tripTimezone()).isEqualTo("America/New_York");
        assertThat(response.userTimezone()).isEqualTo("UTC");
    }
}
