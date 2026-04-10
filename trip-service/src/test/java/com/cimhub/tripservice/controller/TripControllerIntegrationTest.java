package com.cimhub.tripservice.controller;

import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.tripservice.dto.response.CompleteTripResponse;
import com.cimhub.tripservice.dto.response.TripResponse;
import com.cimhub.tripservice.exception.TripNotInProgressException;
import com.cimhub.tripservice.model.OperationMessage;
import com.cimhub.tripservice.model.TripStatus;
import com.cimhub.tripservice.service.ActiveRidesService;
import com.cimhub.tripservice.service.CancellationService;
import com.cimhub.tripservice.service.CompletionService;
import com.cimhub.tripservice.service.OfferRideService;
import com.cimhub.tripservice.service.TripGeospatialService;
import com.cimhub.tripservice.service.TripService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @MockBean
    private TripService tripService;

    @Test
    void getTrip_shouldReturn200WithData() throws Exception {
        TripResponse response = TripResponse.builder()
                .tripId("trip-1")
                .tripStatus(TripStatus.COMPLETED)
                .build();
        when(tripService.getTrip("trip-1")).thenReturn(response);

        mockMvc.perform(get("/api/trips/trip-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tripId").value("trip-1"))
                .andExpect(jsonPath("$.data.tripStatus").value("COMPLETED"));
    }

    @Test
    void completeTrip_shouldReturn200WithMessage() throws Exception {
        CompleteTripResponse response = CompleteTripResponse.builder()
                .tripId("trip-1")
                .driverId("driver-1")
                .tripStatus(TripStatus.COMPLETED)
                .autoCompletedRides(2)
                .build();
        when(completionService.completeTrip("trip-1", "driver-1"))
                .thenReturn(response);

        mockMvc.perform(post("/api/trips/trip-1/complete")
                        .header("X-User-Id", "driver-1")
                        .header("X-User-Role", "DRIVER")
                        .param("driverId", "driver-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(OperationMessage.TRIP_COMPLETED_SUCCESS.format(2)))
                .andExpect(jsonPath("$.data.tripStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.autoCompletedRides").value(2));
    }

    @Test
    void completeTrip_whenTripNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Trip not found with id: trip-99"))
                .when(completionService).completeTrip("trip-99", "driver-1");

        mockMvc.perform(post("/api/trips/trip-99/complete")
                        .header("X-User-Id", "driver-1")
                        .header("X-User-Role", "DRIVER")
                        .param("driverId", "driver-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Trip not found with id: trip-99"));
    }

    @Test
    void completeTrip_whenDriverIsNotOwner_shouldReturn401() throws Exception {
        doThrow(new UnauthorizedException("You are not authorized to complete this trip"))
                .when(completionService).completeTrip("trip-1", "intruder");

        mockMvc.perform(post("/api/trips/trip-1/complete")
                        .header("X-User-Id", "intruder")
                        .header("X-User-Role", "DRIVER")
                        .param("driverId", "intruder"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("You are not authorized to complete this trip"));
    }

    @Test
    void completeTrip_whenTripNotInProgress_shouldReturn409() throws Exception {
        doThrow(new TripNotInProgressException("Trip trip-1 cannot be completed — current status: AVAILABLE"))
                .when(completionService).completeTrip("trip-1", "driver-1");

        mockMvc.perform(post("/api/trips/trip-1/complete")
                        .header("X-User-Id", "driver-1")
                        .header("X-User-Role", "DRIVER")
                        .param("driverId", "driver-1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRIP_NOT_IN_PROGRESS"));
    }

    @Test
    void completeTrip_whenUserHeaderMissing_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/trips/trip-1/complete")
                        .header("X-User-Role", "DRIVER")
                        .param("driverId", "driver-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completeTrip_withZeroAutoCompletedRides_shouldReturn200() throws Exception {
        CompleteTripResponse response = CompleteTripResponse.builder()
                .tripId("trip-2")
                .driverId("driver-2")
                .tripStatus(TripStatus.COMPLETED)
                .autoCompletedRides(0)
                .build();
        when(completionService.completeTrip("trip-2", "driver-2"))
                .thenReturn(response);

        mockMvc.perform(post("/api/trips/trip-2/complete")
                        .header("X-User-Id", "driver-2")
                        .header("X-User-Role", "DRIVER")
                        .param("driverId", "driver-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(OperationMessage.TRIP_COMPLETED_SUCCESS.format(0)))
                .andExpect(jsonPath("$.data.tripStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.autoCompletedRides").value(0));
    }

    @Test
    void completeTrip_whenDriverIdParamMismatchesHeader_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/trips/trip-1/complete")
                        .header("X-User-Id", "driver-1")
                        .header("X-User-Role", "DRIVER")
                        .param("driverId", "other-driver"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Driver identity mismatch"));
    }

    @Test
    void completeTrip_whenRoleHeaderMissing_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/trips/trip-1/complete")
                        .header("X-User-Id", "driver-1")
                        .param("driverId", "driver-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completeTrip_whenRoleIsNotDriver_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/trips/trip-1/complete")
                        .header("X-User-Id", "driver-1")
                        .header("X-User-Role", "PASSENGER")
                        .param("driverId", "driver-1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Driver access required"));
    }

    @Test
    void searchMatchingRoute_whenEffectiveUserIdMismatchesHeader_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/trips/search/matching-route")
                        .header("X-User-Id", "header-user")
                        .header("X-User-Role", "PASSENGER")
                        .param("sourceLatitude", "50.8154")
                        .param("sourceLongitude", "8.6747")
                        .param("destinationLatitude", "50.7265")
                        .param("destinationLongitude", "7.0895")
                        .param("effectiveUserId", "payload-user"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User identity mismatch"));
    }
}
