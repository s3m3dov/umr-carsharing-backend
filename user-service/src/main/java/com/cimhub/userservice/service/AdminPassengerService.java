package com.cimhub.userservice.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UserAlreadyExistsException;
import com.cimhub.common.service.AuditService;
import com.cimhub.userservice.dto.request.CreatePassengerRequest;
import com.cimhub.userservice.dto.request.UpdatePassengerRequest;
import com.cimhub.userservice.dto.response.PassengerResponse;
import com.cimhub.userservice.model.Passenger;
import com.cimhub.userservice.model.User;
import com.cimhub.userservice.repository.PassengerRepository;
import com.cimhub.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPassengerService {

    private final UserRepository userRepository;
    private final PassengerRepository passengerRepository;
    private final AuditService auditService;

    public Page<PassengerResponse> listPassengers(Pageable pageable) {
        Page<User> passengers = userRepository.findByUserType(UserType.PASSENGER, pageable);
        return passengers.map(this::mapToResponse);
    }

    public PassengerResponse getPassenger(String passengerId) {
        User user = findPassengerOrThrow(passengerId);
        return mapToResponse(user);
    }

    public PassengerResponse createPassenger(CreatePassengerRequest request, String adminEmail) {
        if (userRepository.findByEmail(request.email()) != null) {
            throw new UserAlreadyExistsException("User with email " + request.email() + " already exists");
        }

        User user = new User();
        user.setUserId(request.email());
        user.setEmail(request.email());
        user.setUserType(UserType.PASSENGER);
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());
        user.setAge(request.age());
        Instant now = Instant.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setCreatedBy(adminEmail);
        userRepository.save(user);

        Passenger passenger = new Passenger();
        passenger.setUserId(user.getUserId());
        passenger.setCreatedAt(user.getCreatedAt());
        passenger.setUpdatedAt(user.getUpdatedAt());
        passenger.setCreatedBy(adminEmail);
        passengerRepository.save(passenger);

        auditService.logCreate(EntityType.PASSENGER, user.getUserId(), adminEmail, "Passenger created by admin");
        log.info("Passenger {} created by admin {}", user.getUserId(), adminEmail);
        return mapToResponse(user);
    }

    public PassengerResponse updatePassenger(String passengerId, UpdatePassengerRequest request, String adminEmail) {
        User user = findPassengerOrThrow(passengerId);
        Passenger passenger = passengerRepository.findByUserId(passengerId).orElse(null);
        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.phoneNumber() != null) user.setPhoneNumber(request.phoneNumber());
        if (request.age() != null) user.setAge(request.age());
        Instant now = Instant.now();
        user.setUpdatedAt(now);
        userRepository.save(user);
        if (passenger != null) {
            passenger.setUpdatedAt(now);
            passengerRepository.save(passenger);
        }
        auditService.logUpdate(EntityType.PASSENGER, passengerId, adminEmail, "Passenger info updated");
        log.info("Passenger {} updated by {}", passengerId, adminEmail);
        return mapToResponse(user);
    }

    public void deletePassenger(String passengerId, String adminEmail) {
        User user = findPassengerOrThrow(passengerId);
        passengerRepository.findByUserId(passengerId).ifPresent(passengerRepository::delete);
        userRepository.delete(user);
        auditService.logDelete(EntityType.PASSENGER, passengerId, adminEmail, "Passenger deleted");
        log.info("Passenger {} deleted by {}", passengerId, adminEmail);
    }

    private User findPassengerOrThrow(String passengerId) {
        User user = userRepository.findByUserId(passengerId);
        if (user == null || user.getUserType() != UserType.PASSENGER) {
            throw new ResourceNotFoundException("Passenger not found: " + passengerId);
        }
        return user;
    }

    private PassengerResponse mapToResponse(User user) {
        Passenger passenger = passengerRepository.findByUserId(user.getUserId()).orElse(null);
        Instant createdAt = passenger != null && passenger.getCreatedAt() != null
                ? passenger.getCreatedAt()
                : user.getCreatedAt();
        Instant updatedAt = passenger != null && passenger.getUpdatedAt() != null
                ? passenger.getUpdatedAt()
                : user.getUpdatedAt();
        return PassengerResponse.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .userType(user.getUserType())
                .phoneNumber(user.getPhoneNumber())
                .age(user.getAge())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
