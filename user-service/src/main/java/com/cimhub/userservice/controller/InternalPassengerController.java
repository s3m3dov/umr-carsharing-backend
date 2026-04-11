package com.cimhub.userservice.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import com.cimhub.userservice.dto.request.CreatePassengerProfileRequest;
import com.cimhub.userservice.dto.response.PassengerResponse;
import com.cimhub.userservice.service.PassengerService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Hidden
@RestController
@RequestMapping("/api/internal/passengers")
@RequiredArgsConstructor
public class InternalPassengerController {

    private final PassengerService passengerService;

    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponseWrapper<PassengerResponse>> createPassengerProfile(
            @PathVariable String userId,
            @Valid @RequestBody CreatePassengerProfileRequest request) {
        log.info("Internal: create passenger profile for userId: {}", userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseWrapper.success(passengerService.createPassengerProfile(userId, request)));
    }
}
