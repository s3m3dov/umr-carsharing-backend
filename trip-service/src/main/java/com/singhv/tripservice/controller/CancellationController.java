package com.singhv.tripservice.controller;

import com.singhv.common.dto.RequestDTO;
import com.singhv.common.dto.ResponseDTO;
import com.singhv.tripservice.dto.CancelRequestDTO;
import com.singhv.tripservice.service.CancellationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for cancellation operations
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class CancellationController {

    private final CancellationService cancellationService;

    /**
     * Passenger cancels their ride
     * Endpoint: POST /api/rides/cancel
     */
    @PostMapping("/rides/cancel")
    public ResponseEntity<ResponseDTO<String>> cancelRide(
            @RequestBody RequestDTO<CancelRequestDTO> request) {
        
        log.info("Received ride cancellation request from passenger: {}", request.getUserId());
        
        ResponseDTO<String> response = cancellationService.cancelRide(request);
        
        if (response.getSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Driver cancels their trip (cancels all associated rides)
     * Endpoint: POST /api/trips/cancel
     */
    @PostMapping("/trips/cancel")
    public ResponseEntity<ResponseDTO<String>> cancelTrip(
            @RequestBody RequestDTO<CancelRequestDTO> request) {
        
        log.info("Received trip cancellation request from driver: {}", request.getUserId());
        
        ResponseDTO<String> response = cancellationService.cancelTrip(request);
        
        if (response.getSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
