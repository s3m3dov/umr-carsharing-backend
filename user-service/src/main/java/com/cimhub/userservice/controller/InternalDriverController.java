package com.cimhub.userservice.controller;

import com.cimhub.common.dto.response.ApiSuccess;
import com.cimhub.userservice.dto.request.CreateDriverProfileRequest;
import com.cimhub.userservice.dto.response.DriverResponse;
import com.cimhub.userservice.service.DriverService;
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
@RequestMapping("/api/internal/drivers")
@RequiredArgsConstructor
public class InternalDriverController {

    private final DriverService driverService;

    @PostMapping("/{userId}")
    public ResponseEntity<ApiSuccess<DriverResponse>> createDriverProfile(
            @PathVariable String userId, @Valid @RequestBody CreateDriverProfileRequest request) {
        log.info("Internal: create driver profile for userId: {}", userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccess.success(driverService.createDriverProfile(userId, request)));
    }
}
