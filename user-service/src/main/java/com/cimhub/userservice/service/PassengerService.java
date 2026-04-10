package com.cimhub.userservice.service;

import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UserAlreadyExistsException;
import com.cimhub.userservice.dto.request.CreatePassengerProfileRequest;
import com.cimhub.userservice.dto.request.UpdatePassengerRequest;
import com.cimhub.userservice.dto.response.PassengerResponse;
import com.cimhub.userservice.model.Passenger;
import com.cimhub.userservice.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassengerService {

    private final PassengerRepository passengerRepository;

    public PassengerResponse getPassengerProfile(String userId) {
        return buildResponse(findPassengerOrThrow(userId));
    }

    public PassengerResponse createPassengerProfile(String userId, CreatePassengerProfileRequest request) {
        if (passengerRepository.findByUserId(userId).isPresent()) {
            throw new UserAlreadyExistsException("Passenger profile already exists for: " + userId);
        }

        Instant now = Instant.now();
        Passenger passenger = new Passenger();
        passenger.setUserId(userId);
        passenger.setEmail(userId);
        ProfileUpdater.applyCommonProfileFields(
                request,
                passenger::setFirstName,
                passenger::setLastName,
                passenger::setPhoneNumber,
                passenger::setAge);
        passenger.setCreatedAt(now);
        passenger.setUpdatedAt(now);
        passenger.setCreatedBy(userId);
        passengerRepository.save(passenger);
        log.info("Passenger profile created for userId: {}", userId);
        return buildResponse(passenger);
    }

    public PassengerResponse updatePassengerProfile(String userId, UpdatePassengerRequest request) {
        Passenger passenger = findPassengerOrThrow(userId);

        ProfileUpdater.applyCommonProfileUpdates(
                request,
                passenger::setFirstName,
                passenger::setLastName,
                passenger::setPhoneNumber,
                passenger::setAge);

        Instant now = Instant.now();
        passenger.setUpdatedAt(now);
        passengerRepository.save(passenger);
        log.info("Passenger profile updated for userId: {}", userId);
        return buildResponse(passenger);
    }

    private Passenger findPassengerOrThrow(String userId) {
        Passenger passenger = passengerRepository.findByUserId(userId).orElse(null);
        if (passenger == null) {
            throw new ResourceNotFoundException("Passenger not found: " + userId);
        }
        return passenger;
    }

    private PassengerResponse buildResponse(Passenger passenger) {
        return PassengerResponse.builder()
                .userId(passenger.getUserId())
                .firstName(passenger.getFirstName())
                .lastName(passenger.getLastName())
                .email(passenger.getEmail())
                .userType(passenger.getUserType())
                .phoneNumber(passenger.getPhoneNumber())
                .age(passenger.getAge())
                .createdAt(passenger.getCreatedAt())
                .updatedAt(passenger.getUpdatedAt())
                .build();
    }
}
