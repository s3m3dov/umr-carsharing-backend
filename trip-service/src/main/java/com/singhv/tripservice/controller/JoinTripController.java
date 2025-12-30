package com.singhv.tripservice.controller;

import com.singhv.common.dto.RequestDTO;
import com.singhv.common.dto.ResponseDTO;
import com.singhv.tripservice.dto.JoinTripRequestDTO;
import com.singhv.tripservice.service.JoinTripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for passengers to join/book a trip
 */
@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@Slf4j
public class JoinTripController {

    private final JoinTripService joinTripService;

    /**
     * Join a trip (book a ride)
     * 
     * @param request Contains trip details and passenger pickup/dropoff locations
     * @return Success message with ride ID
     */
    @PostMapping("/join-trip")
    public ResponseEntity<ResponseDTO<String>> joinTrip(
            @RequestBody RequestDTO<JoinTripRequestDTO> request) {
        
        log.info("Received join trip request from user: {}", request.getUserId());
        
        ResponseDTO<String> response = joinTripService.joinTrip(request);
        
        if (response.getSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
