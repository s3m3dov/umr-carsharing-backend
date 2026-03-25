package com.cimhub.apigateway.controller;

import com.cimhub.common.dto.ApiResponseWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponseWrapper<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponseWrapper.success("API Gateway Service is alive"));
    }
}
