package com.cimhub.tripservice.controller;

import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.tripservice.dto.response.CompleteRideResponse;
import com.cimhub.tripservice.exception.RideNotCompletableException;
import com.cimhub.tripservice.model.BookingStatus;
import com.cimhub.tripservice.model.OperationMessage;
import com.cimhub.tripservice.model.TripStatus;
import com.cimhub.tripservice.service.CancellationService;
import com.cimhub.tripservice.service.CompletionService;
import com.cimhub.tripservice.service.JoinTripService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JoinTripService joinTripService;

    @MockBean
    private CancellationService cancellationService;

    @MockBean
    private CompletionService completionService;

    @Test
    void completeRide_shouldReturn200WithMessage() throws Exception {
        CompleteRideResponse response = CompleteRideResponse.builder()
                .bookingId("b-1")
                .tripId("trip-1")
                .driverId("driver-1")
                .passengerId("passenger-1")
                .bookingStatus(BookingStatus.COMPLETED)
                .tripStatus(TripStatus.IN_PROGRESS)
                .build();
        when(completionService.completeRide("b-1", "driver-1"))
                .thenReturn(response);

        mockMvc.perform(post("/api/bookings/b-1/complete")
                        .param("driverId", "driver-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(OperationMessage.RIDE_COMPLETED_SUCCESS.text()))
                .andExpect(jsonPath("$.data.bookingStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.tripStatus").value("IN_PROGRESS"));
    }

    @Test
    void completeRide_whenBookingNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Booking not found with id: b-99"))
                .when(completionService).completeRide("b-99", "driver-1");

        mockMvc.perform(post("/api/bookings/b-99/complete")
                        .param("driverId", "driver-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Booking not found with id: b-99"));
    }

    @Test
    void completeRide_whenDriverIsNotOwner_shouldReturn401() throws Exception {
        doThrow(new UnauthorizedException("You are not authorized to complete this ride"))
                .when(completionService).completeRide("b-1", "intruder");

        mockMvc.perform(post("/api/bookings/b-1/complete")
                        .param("driverId", "intruder"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("You are not authorized to complete this ride"));
    }

    @Test
    void completeRide_whenRideNotCompletable_shouldReturn409() throws Exception {
        doThrow(new RideNotCompletableException("Ride b-1 cannot be completed — current status: COMPLETED"))
                .when(completionService).completeRide("b-1", "driver-1");

        mockMvc.perform(post("/api/bookings/b-1/complete")
                        .param("driverId", "driver-1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RIDE_NOT_COMPLETABLE"));
    }

    @Test
    void completeRide_whenDriverIdParamMissing_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/bookings/b-1/complete"))
                .andExpect(status().isBadRequest());
    }
}
