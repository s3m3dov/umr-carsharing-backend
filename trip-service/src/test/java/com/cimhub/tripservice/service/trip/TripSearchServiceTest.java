package com.cimhub.tripservice.service.trip;

import com.cimhub.common.testsupport.Points;
import com.cimhub.tripservice.dto.response.TripResponse;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.common.enums.TripStatus;
import com.cimhub.tripservice.repository.TripsRepository;
import com.cimhub.tripservice.mapper.TripMapper;
import com.cimhub.tripservice.service.util.TimezoneConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripSearchServiceTest {

    @Mock
    private TripsRepository tripsRepository;

    private TripSearchService tripSearchService;

    private TripMapper tripMapper;

    @BeforeEach
    void setUp() {
        tripMapper = new TripMapper(new TimezoneConversionService());
        tripSearchService = new TripSearchService(tripsRepository, tripMapper);
    }

    @Test
    void searchAvailableTrips_shouldReturnOnlyCreatedAndAvailableTrips() {
        Trip created = buildTrip("trip-1", TripStatus.CREATED, 4, 1);
        Trip available = buildTrip("trip-2", TripStatus.AVAILABLE, 3, 2);
        Trip cancelled = buildTrip("trip-3", TripStatus.CANCELLED, 4, 0);

        when(tripsRepository.findAll()).thenReturn(List.of(created, available, cancelled));

        List<TripResponse> responses = tripSearchService.searchAvailableTrips("Europe/Berlin");

        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(r -> r.tripStatus() == TripStatus.CREATED
                || r.tripStatus() == TripStatus.AVAILABLE));
    }

    @Test
    void searchAvailableTrips_shouldMapAvailableSeatsAndTimezoneFields() {
        Trip trip = buildTrip("trip-9", TripStatus.AVAILABLE, 5, 2);
        when(tripsRepository.findAll()).thenReturn(List.of(trip));

        List<TripResponse> responses = tripSearchService.searchAvailableTrips("Europe/Berlin");

        TripResponse response = responses.getFirst();
        assertEquals("trip-9", response.tripId());
        assertEquals(3, response.availableSeats());
        assertEquals("Europe/Berlin", response.userTimezone());
        assertEquals("UTC", response.tripTimezone());
        assertEquals(TripStatus.AVAILABLE, response.tripStatus());
    }

    private Trip buildTrip(String tripId, TripStatus status, int totalSeats, int bookedSeats) {
        Trip trip = new Trip();
        trip.setTripId(tripId);
        trip.setDriverId("driver-1");
        trip.setVehicleNumber("HE-XX-1234");
        trip.setCarType("SEDAN");
        trip.setSourceAddress(Points.of(0, 0, ""));
        trip.setDestinationAddress(Points.of(0, 0, ""));
        trip.setTripStatus(status);
        trip.setTripStartDateTimeUTC(Instant.parse("2026-02-01T12:00:00Z"));
        trip.setTripTimezone("UTC");
        trip.setTotalSeats(totalSeats);
        trip.setBookedSeats(bookedSeats);
        trip.setPricePerSeat(14.0);
        return trip;
    }
}
