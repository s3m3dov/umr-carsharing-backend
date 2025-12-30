package com.cimhub.vehicleservice.controller;

import com.cimhub.common.dto.RequestDTO;
import com.cimhub.common.dto.ResponseDTO;
import com.cimhub.common.dto.ResponseListDTO;
import com.cimhub.vehicleservice.dto.VehicleRegisterRequestDTO;
import com.cimhub.vehicleservice.dto.VehicleResponseDTO;
import com.cimhub.vehicleservice.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicle Management", description = "Vehicle registration and management APIs")
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

    @Operation(summary = "Get user vehicles", description = "Retrieves all vehicles registered by a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicles found", content = @Content(schema = @Schema(implementation = ResponseListDTO.class))),
            @ApiResponse(responseCode = "404", description = "No vehicles found")
    })
    @GetMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseListDTO<VehicleResponseDTO> getUserVehicles(
            @Parameter(description = "User's unique identifier", required = true) @PathVariable String userId) {
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

    @Operation(summary = "Register vehicle", description = "Registers a new vehicle for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle registered successfully", content = @Content(schema = @Schema(implementation = ResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or vehicle already registered")
    })
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDTO<Void> registerVehicle(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Vehicle registration details", required = true) @RequestBody RequestDTO<VehicleRegisterRequestDTO> requestInfo) {
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