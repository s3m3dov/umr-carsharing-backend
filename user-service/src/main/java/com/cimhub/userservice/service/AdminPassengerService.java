package com.cimhub.userservice.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.BadRequestException;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.service.AuditService;
import com.cimhub.userservice.client.AuthServiceClient;
import com.cimhub.userservice.dto.request.CreatePassengerRequest;
import com.cimhub.userservice.dto.request.UpdatePassengerRequest;
import com.cimhub.userservice.dto.response.PassengerResponse;
import com.cimhub.userservice.model.Passenger;
import com.cimhub.userservice.repository.PassengerRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPassengerService {

    private final PassengerRepository passengerRepository;
    private final AuditService auditService;
    private final AuthServiceClient authServiceClient;

    public Page<PassengerResponse> listPassengers(Pageable pageable) {
        return passengerRepository.findAll(pageable).map(this::mapToResponse);
    }

    public PassengerResponse getPassenger(String passengerId) {
        Passenger passenger = findPassengerOrThrow(passengerId);
        return mapToResponse(passenger);
    }

    public PassengerResponse createPassenger(CreatePassengerRequest request, String adminEmail) {
        throw new BadRequestException("Passenger creation must be done through auth-service signup");
    }

    public PassengerResponse updatePassenger(String passengerId, UpdatePassengerRequest request, String adminEmail) {
        Passenger passenger = findPassengerOrThrow(passengerId);
        if (request.firstName() != null) passenger.setFirstName(request.firstName());
        if (request.lastName() != null) passenger.setLastName(request.lastName());
        if (request.phoneNumber() != null) passenger.setPhoneNumber(request.phoneNumber());
        if (request.age() != null) passenger.setAge(request.age());
        Instant now = Instant.now();
        passenger.setUpdatedAt(now);
        passengerRepository.save(passenger);
        auditService.logUpdate(EntityType.PASSENGER, passengerId, adminEmail, "Passenger info updated");
        log.info("Passenger {} updated by {}", passengerId, adminEmail);
        return mapToResponse(passenger);
    }

    public void deletePassenger(String passengerId, String adminEmail) {
        Passenger passenger = findPassengerOrThrow(passengerId);
        passengerRepository.delete(passenger);
        authServiceClient.deleteUser(passengerId);
        auditService.logDelete(EntityType.PASSENGER, passengerId, adminEmail, "Passenger deleted");
        log.info("Passenger {} deleted by {}", passengerId, adminEmail);
    }

    private Passenger findPassengerOrThrow(String passengerId) {
        Passenger passenger = passengerRepository.findByUserId(passengerId).orElse(null);
        if (passenger == null) {
            throw new ResourceNotFoundException("Passenger not found: " + passengerId);
        }
        if (passenger.getEmail() == null || passenger.getEmail().isBlank()) {
            passenger.setEmail(passenger.getUserId());
            passengerRepository.save(passenger);
        }
        return passenger;
    }

    private PassengerResponse mapToResponse(Passenger passenger) {
        String resolvedEmail =
                (passenger.getEmail() == null || passenger.getEmail().isBlank())
                        ? passenger.getUserId()
                        : passenger.getEmail();

        return PassengerResponse.builder()
                .userId(passenger.getUserId())
                .firstName(passenger.getFirstName())
                .lastName(passenger.getLastName())
                .email(resolvedEmail)
                .userType(passenger.getUserType())
                .phoneNumber(passenger.getPhoneNumber())
                .age(passenger.getAge())
                .createdAt(passenger.getCreatedAt())
                .updatedAt(passenger.getUpdatedAt())
                .build();
    }
}
