package com.cimhub.tripservice.controller;

import com.cimhub.common.dto.RequestDTO;
import com.cimhub.common.dto.ResponseDTO;
import com.cimhub.tripservice.dto.OfferRideRequestDTO;
import com.cimhub.tripservice.dto.OfferRideResponseDTO;
import com.cimhub.tripservice.service.OfferRideService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips")
public class OfferRideController {

    private final OfferRideService offerRideService;

    @PostMapping("/offer")
    public ResponseEntity<ResponseDTO<OfferRideResponseDTO>> offerRide(
            @RequestBody RequestDTO<OfferRideRequestDTO> request) {

        ResponseDTO<OfferRideResponseDTO> response = offerRideService.offerRide(request);
        OfferRideResponseDTO responseContent = response.getResponseContent();

        if (responseContent != null && responseContent.getTripCreated() != null && responseContent.getTripCreated()) {
            response.setSuccess(true);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            response.setSuccess(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
