package com.cimhub.userservice.service;

import com.cimhub.common.enums.DriverStatus;
import com.cimhub.common.enums.EntityType;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.service.AuditService;
import com.cimhub.userservice.dto.request.BulkDriverActionRequest;
import com.cimhub.userservice.dto.request.UpdateDriverRequest;
import com.cimhub.userservice.dto.response.DriverDocumentResponse;
import com.cimhub.userservice.dto.response.DriverResponse;
import com.cimhub.userservice.model.Driver;
import com.cimhub.userservice.model.DriverDocument;
import com.cimhub.userservice.model.User;
import com.cimhub.userservice.repository.DriverDocumentRepository;
import com.cimhub.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDriverService {

    private final UserRepository userRepository;
    private final DriverDocumentRepository driverDocumentRepository;
    private final AuditService auditService;

    public Page<DriverResponse> listDrivers(DriverStatus status, Pageable pageable) {
        Page<User> drivers;
        if (status != null) {
            drivers = userRepository.findByUserTypeAndDriverStatus(UserType.DRIVER, status, pageable);
        } else {
            drivers = userRepository.findByUserType(UserType.DRIVER, pageable);
        }
        return drivers.map(this::mapToDriverResponse);
    }

    public DriverResponse getDriver(String driverId) {
        User user = findDriverOrThrow(driverId);
        return mapToDriverResponse(user);
    }

    public DriverResponse approveDriver(String driverId, String adminEmail) {
        Driver driver = findDriverEntityOrThrow(driverId);
        driver.setDriverStatus(DriverStatus.ACTIVE);
        userRepository.save(driver);
        auditService.logApprove(EntityType.DRIVER, driverId, adminEmail, "Driver approved");
        log.info("Driver {} approved by {}", driverId, adminEmail);
        return mapToDriverResponse(driver);
    }

    public DriverResponse rejectDriver(String driverId, String adminEmail) {
        Driver driver = findDriverEntityOrThrow(driverId);
        driver.setDriverStatus(DriverStatus.REJECTED);
        userRepository.save(driver);
        auditService.logReject(EntityType.DRIVER, driverId, adminEmail, "Driver rejected");
        log.info("Driver {} rejected by {}", driverId, adminEmail);
        return mapToDriverResponse(driver);
    }

    public int bulkApprove(BulkDriverActionRequest request, String adminEmail) {
        int count = 0;
        for (String driverId : request.driverIds()) {
            try {
                approveDriver(driverId, adminEmail);
                count++;
            } catch (ResourceNotFoundException e) {
                log.warn("Driver {} not found during bulk approve", driverId);
            }
        }
        return count;
    }

    public int bulkReject(BulkDriverActionRequest request, String adminEmail) {
        int count = 0;
        for (String driverId : request.driverIds()) {
            try {
                rejectDriver(driverId, adminEmail);
                count++;
            } catch (ResourceNotFoundException e) {
                log.warn("Driver {} not found during bulk reject", driverId);
            }
        }
        return count;
    }

    public DriverResponse updateDriver(String driverId, UpdateDriverRequest request, String adminEmail) {
        Driver driver = findDriverEntityOrThrow(driverId);
        if (request.firstName() != null) driver.setFirstName(request.firstName());
        if (request.lastName() != null) driver.setLastName(request.lastName());
        if (request.phoneNumber() != null) driver.setPhoneNumber(request.phoneNumber());
        if (request.age() != null) driver.setAge(request.age());
        if (request.licenseNumber() != null) driver.setLicenseNumber(request.licenseNumber());
        userRepository.save(driver);
        auditService.logUpdate(EntityType.DRIVER, driverId, adminEmail, "Driver info updated");
        log.info("Driver {} updated by {}", driverId, adminEmail);
        return mapToDriverResponse(driver);
    }

    public void deleteDriver(String driverId, String adminEmail) {
        User user = findDriverOrThrow(driverId);
        userRepository.delete(user);
        auditService.logDelete(EntityType.DRIVER, driverId, adminEmail, "Driver deleted");
        log.info("Driver {} deleted by {}", driverId, adminEmail);
    }

    public List<DriverDocumentResponse> getDriverDocuments(String driverId) {
        findDriverOrThrow(driverId);
        return driverDocumentRepository.findByDriverId(driverId).stream()
                .map(this::mapToDocumentResponse)
                .toList();
    }

    private User findDriverOrThrow(String driverId) {
        User user = userRepository.findByUserId(driverId);
        if (user == null || user.getUserType() != UserType.DRIVER) {
            throw new ResourceNotFoundException("Driver not found: " + driverId);
        }
        return user;
    }

    private Driver findDriverEntityOrThrow(String driverId) {
        User user = findDriverOrThrow(driverId);
        if (user instanceof Driver driver) {
            return driver;
        }
        throw new ResourceNotFoundException("Driver not found: " + driverId);
    }

    private DriverResponse mapToDriverResponse(User user) {
        String licenseNumber = null;
        DriverStatus driverStatus = DriverStatus.PENDING;
        if (user instanceof Driver driver) {
            licenseNumber = driver.getLicenseNumber();
            driverStatus = driver.getDriverStatus() != null ? driver.getDriverStatus() : DriverStatus.PENDING;
        }
        return DriverResponse.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .userType(user.getUserType())
                .phoneNumber(user.getPhoneNumber())
                .age(user.getAge())
                .licenseNumber(licenseNumber)
                .driverStatus(driverStatus)
                .createdDate(user.getCreatedDate())
                .build();
    }

    private DriverDocumentResponse mapToDocumentResponse(DriverDocument doc) {
        return DriverDocumentResponse.builder()
                .id(doc.getId())
                .driverId(doc.getDriverId())
                .documentType(doc.getDocumentType())
                .fileName(doc.getFileName())
                .fileUrl(doc.getFileUrl())
                .fileSize(doc.getFileSize())
                .uploadedAt(doc.getUploadedAt())
                .build();
    }
}
