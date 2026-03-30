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
import com.cimhub.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPassengerService {

    private final UserRepository userRepository;
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

        Passenger passenger = new Passenger();
        passenger.setUserId(request.email());
        passenger.setEmail(request.email());
        passenger.setFirstName(request.firstName());
        passenger.setLastName(request.lastName());
        passenger.setPhoneNumber(request.phoneNumber());
        passenger.setAge(request.age());
        passenger.setCreatedDate(new Date());
        passenger.setCreatedBy(adminEmail);

        userRepository.save(passenger);
        auditService.logCreate(EntityType.PASSENGER, passenger.getUserId(), adminEmail, "Passenger created by admin");
        log.info("Passenger {} created by admin {}", passenger.getUserId(), adminEmail);
        return mapToResponse(passenger);
    }

    public PassengerResponse updatePassenger(String passengerId, UpdatePassengerRequest request, String adminEmail) {
        User user = findPassengerOrThrow(passengerId);
        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.phoneNumber() != null) user.setPhoneNumber(request.phoneNumber());
        if (request.age() != null) user.setAge(request.age());
        userRepository.save(user);
        auditService.logUpdate(EntityType.PASSENGER, passengerId, adminEmail, "Passenger info updated");
        log.info("Passenger {} updated by {}", passengerId, adminEmail);
        return mapToResponse(user);
    }

    public void deletePassenger(String passengerId, String adminEmail) {
        User user = findPassengerOrThrow(passengerId);
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
        return PassengerResponse.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .userType(user.getUserType())
                .phoneNumber(user.getPhoneNumber())
                .age(user.getAge())
                .createdDate(user.getCreatedDate())
                .build();
    }
}
