package com.cimhub.userservice.service;

import com.cimhub.common.enums.DriverStatus;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UserAlreadyExistsException;
import com.cimhub.userservice.dto.request.CreateDriverProfileRequest;
import com.cimhub.userservice.dto.request.UpdateDriverRequest;
import com.cimhub.userservice.dto.response.DriverResponse;
import com.cimhub.userservice.model.Driver;
import com.cimhub.userservice.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverResponse getDriverProfile(String userId) {
        return buildResponse(findDriverOrThrow(userId));
    }

    public DriverResponse createDriverProfile(String userId, CreateDriverProfileRequest request) {
        if (driverRepository.findByUserId(userId).isPresent()) {
            throw new UserAlreadyExistsException("Driver profile already exists for: " + userId);
        }

        Instant now = Instant.now();
        Driver driver = new Driver();
        driver.setUserId(userId);
        driver.setEmail(userId);
        ProfileUpdater.applyCommonProfileFields(
                request,
                driver::setFirstName,
                driver::setLastName,
                driver::setPhoneNumber,
                driver::setAge);
        driver.setLicenseNumber(request.licenseNumber());
        driver.setCreatedAt(now);
        driver.setUpdatedAt(now);
        driver.setCreatedBy(userId);
        driverRepository.save(driver);
        log.info("Driver profile created for userId: {}", userId);
        return buildResponse(driver);
    }

    public DriverResponse updateDriverProfile(String userId, UpdateDriverRequest request) {
        Driver driver = findDriverOrThrow(userId);

        ProfileUpdater.applyCommonProfileUpdates(
                request,
                driver::setFirstName,
                driver::setLastName,
                driver::setPhoneNumber,
                driver::setAge);
        if (request.licenseNumber() != null) driver.setLicenseNumber(request.licenseNumber());

        Instant now = Instant.now();
        driver.setUpdatedAt(now);
        driverRepository.save(driver);
        log.info("Driver profile updated for userId: {}", userId);
        return buildResponse(driver);
    }

    private Driver findDriverOrThrow(String userId) {
        Driver driver = driverRepository.findByUserId(userId).orElse(null);
        if (driver == null) {
            throw new ResourceNotFoundException("Driver not found: " + userId);
        }
        if (driver.getEmail() == null || driver.getEmail().isBlank()) {
            driver.setEmail(driver.getUserId());
            driverRepository.save(driver);
        }
        return driver;
    }

    private DriverResponse buildResponse(Driver driver) {
        String resolvedEmail = (driver.getEmail() == null || driver.getEmail().isBlank())
                ? driver.getUserId()
                : driver.getEmail();

        return DriverResponse.builder()
                .userId(driver.getUserId())
                .firstName(driver.getFirstName())
                .lastName(driver.getLastName())
                .email(resolvedEmail)
                .userType(driver.getUserType())
                .phoneNumber(driver.getPhoneNumber())
                .age(driver.getAge())
                .licenseNumber(driver.getLicenseNumber())
                .driverStatus(driver.getDriverStatus() != null ? driver.getDriverStatus() : DriverStatus.PENDING)
                .createdAt(driver.getCreatedAt())
                .updatedAt(driver.getUpdatedAt())
                .build();
    }
}
