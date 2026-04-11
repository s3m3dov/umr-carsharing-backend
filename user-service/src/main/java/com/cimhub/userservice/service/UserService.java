package com.cimhub.userservice.service;

import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.userservice.dto.response.UserResponse;
import com.cimhub.userservice.model.Driver;
import com.cimhub.userservice.model.Passenger;
import com.cimhub.userservice.repository.DriverRepository;
import com.cimhub.userservice.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final DriverRepository driverRepository;
    private final PassengerRepository passengerRepository;

    public UserResponse getUserInfo(String userId) {
        Driver driver = driverRepository.findByUserId(userId).orElse(null);
        if (driver != null) {
            return new UserResponse(
                    driver.getUserId(),
                    driver.getFirstName(),
                    driver.getLastName(),
                    driver.getEmail(),
                    UserType.DRIVER,
                    driver.getPhoneNumber(),
                    driver.getAge(),
                    driver.getLicenseNumber(),
                    driver.getCreatedAt(),
                    driver.getUpdatedAt());
        }

        Passenger passenger = passengerRepository.findByUserId(userId).orElse(null);
        if (passenger != null) {
            return new UserResponse(
                    passenger.getUserId(),
                    passenger.getFirstName(),
                    passenger.getLastName(),
                    passenger.getEmail(),
                    UserType.PASSENGER,
                    passenger.getPhoneNumber(),
                    passenger.getAge(),
                    null,
                    passenger.getCreatedAt(),
                    passenger.getUpdatedAt());
        }

        throw new ResourceNotFoundException("User with id " + userId + " not found");
    }
}
