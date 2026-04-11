package com.cimhub.apigateway.controller;

import com.cimhub.common.dto.response.ApiSuccess;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiSuccess<String>> healthCheck() {
        return ResponseEntity.ok(ApiSuccess.success("API Gateway Service is alive"));
    }
}
