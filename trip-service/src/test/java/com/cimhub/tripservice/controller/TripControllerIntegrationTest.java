package com.cimhub.tripservice.controller;

import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.tripservice.exception.TripNotInProgressException;
import com.cimhub.tripservice.service.ActiveRidesService;
import com.cimhub.tripservice.service.CancellationService;
import com.cimhub.tripservice.service.CompletionService;
import com.cimhub.tripservice.service.OfferRideService;
import com.cimhub.tripservice.service.TripGeospatialService;
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

@WebMvcTest(TripController.class)
class TripControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OfferRideService offerRideService;

    @MockBean
    private CancellationService cancellationService;

    @MockBean
    private CompletionService completionService;

    @MockBean
    private TripGeospatialService geoService;

    @MockBean
    private ActiveRidesService activeRidesService;

    @Test
    void completeTrip_shouldReturn200WithMessage() throws Exception {
        when(completionService.completeTrip("trip-1", "driver-1"))
                .thenReturn("Trip completed successfully. 2 remaining ride(s) were auto-completed");

        mockMvc.perform(post("/api/trips/trip-1/complete")
                        .param("driverId", "driver-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Trip completed successfully. 2 remaining ride(s) were auto-completed"));
    }

    @Test
    void completeTrip_whenTripNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Trip not found with id: trip-99"))
                .when(completionService).completeTrip("trip-99", "driver-1");

        mockMvc.perform(post("/api/trips/trip-99/complete")
                        .param("driverId", "driver-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Trip not found with id: trip-99"));
    }

    @Test
    void completeTrip_whenDriverIsNotOwner_shouldReturn401() throws Exception {
        doThrow(new UnauthorizedException("You are not authorized to complete this trip"))
                .when(completionService).completeTrip("trip-1", "intruder");

        mockMvc.perform(post("/api/trips/trip-1/complete")
                        .param("driverId", "intruder"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("You are not authorized to complete this trip"));
    }

    @Test
    void completeTrip_whenTripNotInProgress_shouldReturn409() throws Exception {
        doThrow(new TripNotInProgressException("Trip trip-1 cannot be completed — current status: AVAILABLE"))
                .when(completionService).completeTrip("trip-1", "driver-1");

        mockMvc.perform(post("/api/trips/trip-1/complete")
                        .param("driverId", "driver-1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRIP_NOT_IN_PROGRESS"));
    }

    @Test
    void completeTrip_whenDriverIdParamMissing_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/trips/trip-1/complete"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completeTrip_withZeroAutoCompletedRides_shouldReturn200() throws Exception {
        when(completionService.completeTrip("trip-2", "driver-2"))
                .thenReturn("Trip completed successfully. 0 remaining ride(s) were auto-completed");

        mockMvc.perform(post("/api/trips/trip-2/complete")
                        .param("driverId", "driver-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Trip completed successfully. 0 remaining ride(s) were auto-completed"));
    }
}
