package com.cimhub.userservice.service;

import com.cimhub.common.enums.DriverStatus;
import com.cimhub.common.enums.EntityType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.service.AuditService;
import com.cimhub.userservice.client.AuthServiceClient;
import com.cimhub.userservice.dto.request.BulkDriverActionRequest;
import com.cimhub.userservice.dto.request.UpdateDriverRequest;
import com.cimhub.userservice.dto.response.DriverDocumentResponse;
import com.cimhub.userservice.dto.response.DriverResponse;
import com.cimhub.userservice.model.Driver;
import com.cimhub.userservice.model.DriverDocument;
import com.cimhub.userservice.repository.DriverDocumentRepository;
import com.cimhub.userservice.repository.DriverRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDriverService {

    private final DriverRepository driverRepository;
    private final DriverDocumentRepository driverDocumentRepository;
    private final AuditService auditService;
    private final AuthServiceClient authServiceClient;

    public Page<DriverResponse> listDrivers(DriverStatus status, Pageable pageable) {
        if (status != null) {
            Page<Driver> drivers = driverRepository.findByDriverStatus(status, pageable);
            List<DriverResponse> responses =
                    drivers.stream().map(this::mapToDriverResponse).toList();
            return new PageImpl<>(responses, pageable, drivers.getTotalElements());
        }
        return driverRepository.findAll(pageable).map(this::mapToDriverResponse);
    }

    public DriverResponse getDriver(String driverId) {
        Driver driver = findDriverOrThrow(driverId);
        return mapToDriverResponse(driver);
    }

    public DriverResponse approveDriver(String driverId, String adminEmail) {
        Driver driver = findDriverOrThrow(driverId);
        Instant now = Instant.now();
        driver.setDriverStatus(DriverStatus.ACTIVE);
        driver.setUpdatedAt(now);
        driverRepository.save(driver);
        auditService.logApprove(EntityType.DRIVER, driverId, adminEmail, "Driver approved");
        log.info("Driver {} approved by {}", driverId, adminEmail);
        return mapToDriverResponse(driver);
    }

    public DriverResponse rejectDriver(String driverId, String adminEmail) {
        Driver driver = findDriverOrThrow(driverId);
        Instant now = Instant.now();
        driver.setDriverStatus(DriverStatus.REJECTED);
        driver.setUpdatedAt(now);
        driverRepository.save(driver);
        auditService.logReject(EntityType.DRIVER, driverId, adminEmail, "Driver rejected");
        log.info("Driver {} rejected by {}", driverId, adminEmail);
        return mapToDriverResponse(driver);
    }

    @Transactional
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

    @Transactional
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
        Driver driver = findDriverOrThrow(driverId);
        Instant now = Instant.now();

        if (request.firstName() != null) driver.setFirstName(request.firstName());
        if (request.lastName() != null) driver.setLastName(request.lastName());
        if (request.phoneNumber() != null) driver.setPhoneNumber(request.phoneNumber());
        if (request.age() != null) driver.setAge(request.age());
        if (request.licenseNumber() != null) driver.setLicenseNumber(request.licenseNumber());

        driver.setUpdatedAt(now);
        driverRepository.save(driver);
        auditService.logUpdate(EntityType.DRIVER, driverId, adminEmail, "Driver info updated");
        log.info("Driver {} updated by {}", driverId, adminEmail);
        return mapToDriverResponse(driver);
    }

    public void deleteDriver(String driverId, String adminEmail) {
        findDriverOrThrow(driverId);
        driverRepository.deleteByUserId(driverId);
        authServiceClient.deleteUser(driverId);
        auditService.logDelete(EntityType.DRIVER, driverId, adminEmail, "Driver deleted");
        log.info("Driver {} deleted by {}", driverId, adminEmail);
    }

    public List<DriverDocumentResponse> getDriverDocuments(String driverId) {
        findDriverOrThrow(driverId);
        return driverDocumentRepository.findByDriverId(driverId).stream()
                .map(this::mapToDocumentResponse)
                .toList();
    }

    private Driver findDriverOrThrow(String driverId) {
        Driver driver = driverRepository.findByUserId(driverId).orElse(null);
        if (driver == null) {
            throw new ResourceNotFoundException("Driver not found: " + driverId);
        }
        if (driver.getEmail() == null || driver.getEmail().isBlank()) {
            driver.setEmail(driver.getUserId());
            driverRepository.save(driver);
        }
        return driver;
    }

    private DriverResponse mapToDriverResponse(Driver driver) {
        String resolvedEmail =
                (driver.getEmail() == null || driver.getEmail().isBlank()) ? driver.getUserId() : driver.getEmail();

        return DriverResponse.builder()
                .userId(driver.getUserId())
                .firstName(driver.getFirstName())
                .lastName(driver.getLastName())
                .email(resolvedEmail)
                .userType(driver.getUserType())
                .phoneNumber(driver.getPhoneNumber())
                .age(driver.getAge())
                .licenseNumber(driver.getLicenseNumber())
                .driverStatus(driver.getDriverStatus())
                .createdAt(driver.getCreatedAt())
                .updatedAt(driver.getUpdatedAt())
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
