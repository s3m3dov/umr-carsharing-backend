package com.cimhub.tripservice.service;

import com.cimhub.common.exception.BusinessException;
import com.cimhub.tripservice.dto.common.LocationPoint;
import com.cimhub.tripservice.dto.request.JoinTripRequest;
import com.cimhub.tripservice.model.Booking;
import com.cimhub.tripservice.model.BookingStatus;
import com.cimhub.tripservice.model.Trip;
import com.cimhub.tripservice.repository.BookingRepository;
import com.cimhub.tripservice.repository.TripsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JoinTripServiceTest {

    @Mock
    private TripsRepository tripsRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private JoinTripService joinTripService;

    private JoinTripRequest joinTripRequest;
    private Trip availableTrip;

    @BeforeEach
    void setUp() {
        LocationPoint pickup = new LocationPoint(40.7128, -74.0060, "New York");
        LocationPoint dropoff = new LocationPoint(34.0522, -118.2437, "Los Angeles");

        joinTripRequest = new JoinTripRequest(
                "trip123",
                "passenger456",
                pickup,
                dropoff,
                2);

        availableTrip = new Trip();
        availableTrip.setTripId("trip123");
        availableTrip.setDriverId("driver789");
        availableTrip.setTripStatus("AVAILABLE");
        availableTrip.setOfferedSeat(4);
        availableTrip.setCurrSeats(0);
    }

    @Test
    void joinTrip_whenValidRequest_thenSuccessfullyJoinsTrip() {
        // Given
        when(tripsRepository.findById(anyString())).thenReturn(Optional.of(availableTrip));
        when(bookingRepository.findByTripId(anyString())).thenReturn(Collections.emptyList());

        // When
        String result = joinTripService.joinTrip(joinTripRequest);

        // Then
        assertThat(result).contains("Successfully joined the trip");

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());

        Booking savedBooking = bookingCaptor.getValue();
        assertThat(savedBooking.getTripId()).isEqualTo("trip123");
        assertThat(savedBooking.getPassengerId()).isEqualTo("passenger456");
        assertThat(savedBooking.getDriverId()).isEqualTo("driver789");
        assertThat(savedBooking.getRequestedSeats()).isEqualTo(2);
        assertThat(savedBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        verify(tripsRepository).save(tripCaptor.capture());
        assertThat(tripCaptor.getValue().getCurrSeats()).isEqualTo(2);
    }

    @Test
    void joinTrip_whenTripNotFound_thenThrowsBusinessException() {
        // Given
        when(tripsRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> joinTripService.joinTrip(joinTripRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Trip")
                .hasMessageContaining("trip123");

        verify(bookingRepository, never()).save(any(Booking.class));
        verify(tripsRepository, never()).save(any(Trip.class));
    }

    @Test
    void joinTrip_whenTripNotAvailable_thenThrowsBusinessException() {
        // Given
        availableTrip.setTripStatus("COMPLETED");
        when(tripsRepository.findById(anyString())).thenReturn(Optional.of(availableTrip));

        // When & Then
        assertThatThrownBy(() -> joinTripService.joinTrip(joinTripRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not available for booking");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void joinTrip_whenPassengerAlreadyBooked_thenThrowsBusinessException() {
        // Given
        Booking existingBooking = Booking.builder()
                .tripId("trip123")
                .passengerId("passenger456")
                .status(BookingStatus.CONFIRMED)
                .build();

        when(tripsRepository.findById(anyString())).thenReturn(Optional.of(availableTrip));
        when(bookingRepository.findByTripId(anyString())).thenReturn(Collections.singletonList(existingBooking));

        // When & Then
        assertThatThrownBy(() -> joinTripService.joinTrip(joinTripRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already booked");

        verify(bookingRepository, never()).save(any(Booking.class));
        verify(tripsRepository, times(0)).save(any(Trip.class));
    }

    @Test
    void joinTrip_whenInsufficientSeats_thenThrowsBusinessException() {
        // Given
        availableTrip.setCurrSeats(3); // Only 1 seat available (4 offered - 3 current)
        when(tripsRepository.findById(anyString())).thenReturn(Optional.of(availableTrip));
        when(bookingRepository.findByTripId(anyString())).thenReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> joinTripService.joinTrip(joinTripRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only 1 seats available, but 2 requested");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void joinTrip_whenCancelledBookingExists_thenAllowsNewBooking() {
        // Given
        Booking cancelledBooking = Booking.builder()
                .tripId("trip123")
                .passengerId("passenger456")
                .status(BookingStatus.CANCELLED)
                .build();

        when(tripsRepository.findById(anyString())).thenReturn(Optional.of(availableTrip));
        when(bookingRepository.findByTripId(anyString())).thenReturn(Collections.singletonList(cancelledBooking));

        // When
        String result = joinTripService.joinTrip(joinTripRequest);

        // Then
        assertThat(result).contains("Successfully joined the trip");
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void joinTrip_whenMultipleSeatsRequested_thenUpdatesCurrentSeatsCorrectly() {
        // Given
        availableTrip.setCurrSeats(1);
        when(tripsRepository.findById(anyString())).thenReturn(Optional.of(availableTrip));
        when(bookingRepository.findByTripId(anyString())).thenReturn(Collections.emptyList());

        // When
        joinTripService.joinTrip(joinTripRequest);

        // Then
        ArgumentCaptor<Trip> tripCaptor = ArgumentCaptor.forClass(Trip.class);
        verify(tripsRepository).save(tripCaptor.capture());
        assertThat(tripCaptor.getValue().getCurrSeats()).isEqualTo(3); // 1 + 2 requested
    }

    @Test
    void joinTrip_whenOfferedStatusTrip_thenAllowsJoining() {
        // Given
        availableTrip.setTripStatus("OFFERED");
        when(tripsRepository.findById(anyString())).thenReturn(Optional.of(availableTrip));
        when(bookingRepository.findByTripId(anyString())).thenReturn(Collections.emptyList());

        // When
        String result = joinTripService.joinTrip(joinTripRequest);

        // Then
        assertThat(result).contains("Successfully joined the trip");
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void joinTrip_whenCalled_thenSetsPickupAndDropoffLocations() {
        // Given
        when(tripsRepository.findById(anyString())).thenReturn(Optional.of(availableTrip));
        when(bookingRepository.findByTripId(anyString())).thenReturn(Collections.emptyList());

        // When
        joinTripService.joinTrip(joinTripRequest);

        // Then
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());

        Booking savedBooking = bookingCaptor.getValue();
        assertThat(savedBooking.getPickupLocation()).isNotNull();
        assertThat(savedBooking.getPickupLocation().getLatitude()).isEqualTo(40.7128);
        assertThat(savedBooking.getPickupLocation().getLongitude()).isEqualTo(-74.0060);

        assertThat(savedBooking.getDropoffLocation()).isNotNull();
        assertThat(savedBooking.getDropoffLocation().getLatitude()).isEqualTo(34.0522);
        assertThat(savedBooking.getDropoffLocation().getLongitude()).isEqualTo(-118.2437);
    }
}
