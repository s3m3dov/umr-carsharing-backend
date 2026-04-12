package com.cimhub.tripservice.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cimhub.common.constant.HeaderConstants;
import com.cimhub.common.enums.TripStatus;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.tripservice.dto.response.CompleteTripResponse;
import com.cimhub.tripservice.dto.response.TripResponse;
import com.cimhub.tripservice.exception.TripNotInProgressException;
import com.cimhub.tripservice.model.OperationMessage;
import com.cimhub.tripservice.service.booking.CancellationService;
import com.cimhub.tripservice.service.booking.CompletionService;
import com.cimhub.tripservice.service.geo.TripGeospatialService;
import com.cimhub.tripservice.service.trip.ActiveRidesService;
import com.cimhub.tripservice.service.trip.OfferRideService;
import com.cimhub.tripservice.service.trip.TripService;
import com.cimhub.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TripController.class)
@Import(GlobalExceptionHandler.class)
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

    @MockBean
    private MongoTemplate mongoTemplate;

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
        when(completionService.completeTrip("trip-1", "driver-1")).thenReturn(response);

        mockMvc.perform(post("/api/trips/trip-1/complete")
                        .header(HeaderConstants.X_USER_ID, "driver-1")
                        .header(HeaderConstants.X_USER_ROLE, "DRIVER")
                        .param("driverId", "driver-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(OperationMessage.TRIP_COMPLETED_SUCCESS.format(2)))
                .andExpect(jsonPath("$.data.tripStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.autoCompletedRides").value(2));
    }

    @Test
    void completeTrip_whenTripNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Trip not found with id: trip-99"))
                .when(completionService)
                .completeTrip("trip-99", "driver-1");

        mockMvc.perform(post("/api/trips/trip-99/complete")
                        .header(HeaderConstants.X_USER_ID, "driver-1")
                        .header(HeaderConstants.X_USER_ROLE, "DRIVER")
                        .param("driverId", "driver-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Trip not found with id: trip-99"));
    }

    @Test
    void completeTrip_whenDriverIsNotOwner_shouldReturn401() throws Exception {
        doThrow(new UnauthorizedException("You are not authorized to complete this trip"))
                .when(completionService)
                .completeTrip("trip-1", "intruder");

        mockMvc.perform(post("/api/trips/trip-1/complete")
                        .header(HeaderConstants.X_USER_ID, "intruder")
                        .header(HeaderConstants.X_USER_ROLE, "DRIVER")
                        .param("driverId", "intruder"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("You are not authorized to complete this trip"));
    }

    @Test
    void completeTrip_whenTripNotInProgress_shouldReturn409() throws Exception {
        doThrow(new TripNotInProgressException("Trip trip-1 cannot be completed — current status: AVAILABLE"))
                .when(completionService)
                .completeTrip("trip-1", "driver-1");

        mockMvc.perform(post("/api/trips/trip-1/complete")
                        .header(HeaderConstants.X_USER_ID, "driver-1")
                        .header(HeaderConstants.X_USER_ROLE, "DRIVER")
                        .param("driverId", "driver-1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRIP_NOT_IN_PROGRESS"));
    }

    @Test
    void completeTrip_whenUserHeaderMissing_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/trips/trip-1/complete")
                        .header(HeaderConstants.X_USER_ROLE, "DRIVER")
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
        when(completionService.completeTrip("trip-2", "driver-2")).thenReturn(response);

        mockMvc.perform(post("/api/trips/trip-2/complete")
                        .header(HeaderConstants.X_USER_ID, "driver-2")
                        .header(HeaderConstants.X_USER_ROLE, "DRIVER")
                        .param("driverId", "driver-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(OperationMessage.TRIP_COMPLETED_SUCCESS.format(0)))
                .andExpect(jsonPath("$.data.tripStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.autoCompletedRides").value(0));
    }

    @Test
    void completeTrip_whenRoleHeaderMissing_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/trips/trip-1/complete")
                        .header(HeaderConstants.X_USER_ID, "driver-1")
                        .param("driverId", "driver-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completeTrip_whenRoleIsNotDriver_shouldReturn403() throws Exception {
        mockMvc.perform(post("/api/trips/trip-1/complete")
                        .header(HeaderConstants.X_USER_ID, "driver-1")
                        .header(HeaderConstants.X_USER_ROLE, "PASSENGER")
                        .param("driverId", "driver-1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Driver access required"));
    }

}
