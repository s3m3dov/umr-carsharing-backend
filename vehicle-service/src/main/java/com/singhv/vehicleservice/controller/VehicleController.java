package com.singhv.vehicleservice.controller;

import com.singhv.common.dto.RequestDTO;
import com.singhv.common.dto.ResponseDTO;
import com.singhv.common.dto.ResponseListDTO;
import com.singhv.vehicleservice.dto.VehicleRegisterRequestDTO;
import com.singhv.vehicleservice.dto.VehicleResponseDTO;
import com.singhv.vehicleservice.service.VehicleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @GetMapping("/health")
    public ResponseDTO<String> healthCheck() {
        ResponseDTO<String> dto = new ResponseDTO<>();
        dto.setSuccess(true);
        dto.setErrorMessage(null);
        dto.setResponseContent("Vehicle Service is alive");
        return dto;
    }
    @GetMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseListDTO<VehicleResponseDTO> getUserVehicles(@PathVariable String userId) {
        log.info("Getting vehicles for userId: {}", userId);
        ResponseListDTO<VehicleResponseDTO> response = new ResponseListDTO<>();
        try {
            List<VehicleResponseDTO> resultList = vehicleService.getUserVehicles(userId);
            if (!resultList.isEmpty()) {
                response.setResponseContent(resultList);
                response.setSuccess(true);
                response.setErrorMessage(null);
            } else {
                response.setResponseContent(null);
                response.setSuccess(true);
                response.setErrorMessage("No vehicles found");
            }
        } catch (Exception exp) {
            response.setResponseContent(null);
            response.setSuccess(false);
            response.setErrorMessage(exp.getMessage());
            exp.printStackTrace();
        }
        return response;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDTO<Void> registerVehicle(@RequestBody RequestDTO<VehicleRegisterRequestDTO> requestInfo) {
        log.info("Registering vehicle for userId: {}", requestInfo.getUserId());
        ResponseDTO<Void> responseDTO = new ResponseDTO<>();
        try {
            if (requestInfo.getRequestContent() == null) {
                throw new Exception("Vehicle Details Required!!");
            }
            if (vehicleService.addNewVehicle(requestInfo.getUserId(), requestInfo.getRequestContent())) {
                responseDTO.setSuccess(true);
                responseDTO.setErrorMessage(null);
                responseDTO.setResponseContent(null);
            } else {
                responseDTO.setSuccess(false);
                responseDTO.setErrorMessage("Vehicle already registered");
                responseDTO.setResponseContent(null);
            }
        } catch (Exception exp) {
            responseDTO.setSuccess(false);
            responseDTO.setResponseContent(null);
            responseDTO.setErrorMessage(exp.getMessage());
            exp.printStackTrace();
        }
        return responseDTO;
    }
}