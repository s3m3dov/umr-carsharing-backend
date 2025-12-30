package com.singhv.apigateway.controller;

import com.singhv.common.dto.ResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseDTO<String> healthCheck() {
        ResponseDTO<String> dto = new ResponseDTO<>();
        dto.setSuccess(true);
        dto.setErrorMessage(null);
        dto.setResponseContent("API Gateway Service is alive");
        return dto;
    }
}
