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
import com.cimhub.userservice.repository.DriverRepository;
import com.cimhub.userservice.repository.DriverDocumentRepository;
import com.cimhub.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDriverService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final DriverDocumentRepository driverDocumentRepository;
    private final AuditService auditService;

    public Page<DriverResponse> listDrivers(DriverStatus status, Pageable pageable) {
        if (status != null) {
            Page<Driver> drivers = driverRepository.findByDriverStatus(status, pageable);
            List<String> userIds = drivers.stream().map(Driver::getUserId).toList();
            Map<String, User> usersById = userRepository.findByUserIdIn(userIds).stream()
                    .collect(HashMap::new, (map, user) -> map.put(user.getUserId(), user), HashMap::putAll);

            List<DriverResponse> responses = drivers.stream()
                    .map(driver -> {
                        User user = usersById.get(driver.getUserId());
                        if (user == null || user.getUserType() != UserType.DRIVER) {
                            throw new ResourceNotFoundException("Driver not found: " + driver.getUserId());
                        }
                        return mapToDriverResponse(user, driver);
                    })
                    .toList();
            return new PageImpl<>(responses, pageable, drivers.getTotalElements());
        }

        Page<User> users = userRepository.findByUserType(UserType.DRIVER, pageable);
        List<String> userIds = users.stream().map(User::getUserId).toList();
        Map<String, Driver> driversByUserId = driverRepository.findByUserIdIn(userIds).stream()
                .collect(HashMap::new, (map, driver) -> map.put(driver.getUserId(), driver), HashMap::putAll);

        return users.map(user -> mapToDriverResponse(user, driversByUserId.get(user.getUserId())));
    }

    public DriverResponse getDriver(String driverId) {
        User user = findDriverUserOrThrow(driverId);
        Driver driver = driverRepository.findByUserId(driverId).orElse(null);
        return mapToDriverResponse(user, driver);
    }

    public DriverResponse approveDriver(String driverId, String adminEmail) {
        User user = findDriverUserOrThrow(driverId);
        Driver driver = findOrCreateDriverProfile(user);
        Instant now = Instant.now();
        driver.setDriverStatus(DriverStatus.ACTIVE);
        driver.setUpdatedAt(now);
        user.setUpdatedAt(now);
        userRepository.save(user);
        driverRepository.save(driver);
        auditService.logApprove(EntityType.DRIVER, driverId, adminEmail, "Driver approved");
        log.info("Driver {} approved by {}", driverId, adminEmail);
        return mapToDriverResponse(user, driver);
    }

    public DriverResponse rejectDriver(String driverId, String adminEmail) {
        User user = findDriverUserOrThrow(driverId);
        Driver driver = findOrCreateDriverProfile(user);
        Instant now = Instant.now();
        driver.setDriverStatus(DriverStatus.REJECTED);
        driver.setUpdatedAt(now);
        user.setUpdatedAt(now);
        userRepository.save(user);
        driverRepository.save(driver);
        auditService.logReject(EntityType.DRIVER, driverId, adminEmail, "Driver rejected");
        log.info("Driver {} rejected by {}", driverId, adminEmail);
        return mapToDriverResponse(user, driver);
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
        User user = findDriverUserOrThrow(driverId);
        Driver driver = findOrCreateDriverProfile(user);
        Instant now = Instant.now();

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.phoneNumber() != null) user.setPhoneNumber(request.phoneNumber());
        if (request.age() != null) user.setAge(request.age());
        if (request.licenseNumber() != null) driver.setLicenseNumber(request.licenseNumber());

        user.setUpdatedAt(now);
        driver.setUpdatedAt(now);
        userRepository.save(user);
        driverRepository.save(driver);
        auditService.logUpdate(EntityType.DRIVER, driverId, adminEmail, "Driver info updated");
        log.info("Driver {} updated by {}", driverId, adminEmail);
        return mapToDriverResponse(user, driver);
    }

    public void deleteDriver(String driverId, String adminEmail) {
        User user = findDriverUserOrThrow(driverId);
        driverRepository.deleteByUserId(driverId);
        userRepository.delete(user);
        auditService.logDelete(EntityType.DRIVER, driverId, adminEmail, "Driver deleted");
        log.info("Driver {} deleted by {}", driverId, adminEmail);
    }

    public List<DriverDocumentResponse> getDriverDocuments(String driverId) {
        findDriverUserOrThrow(driverId);
        return driverDocumentRepository.findByDriverId(driverId).stream()
                .map(this::mapToDocumentResponse)
                .toList();
    }

    private User findDriverUserOrThrow(String driverId) {
        User user = userRepository.findByUserId(driverId);
        if (user == null || user.getUserType() != UserType.DRIVER) {
            throw new ResourceNotFoundException("Driver not found: " + driverId);
        }
        return user;
    }

    private Driver findOrCreateDriverProfile(User user) {
        return driverRepository.findByUserId(user.getUserId()).orElseGet(() -> {
            Driver driver = new Driver();
            driver.setUserId(user.getUserId());
            driver.setCreatedAt(user.getCreatedAt());
            driver.setUpdatedAt(user.getUpdatedAt());
            driver.setCreatedBy(user.getCreatedBy());
            return driverRepository.save(driver);
        });
    }

    private DriverResponse mapToDriverResponse(User user, Driver driver) {
        String licenseNumber = driver != null ? driver.getLicenseNumber() : null;
        DriverStatus driverStatus = driver != null && driver.getDriverStatus() != null
                ? driver.getDriverStatus()
                : DriverStatus.PENDING;
        Instant createdAt = driver != null && driver.getCreatedAt() != null
                ? driver.getCreatedAt()
                : user.getCreatedAt();
        Instant updatedAt = driver != null && driver.getUpdatedAt() != null
                ? driver.getUpdatedAt()
                : user.getUpdatedAt();
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
                .createdAt(createdAt)
                .updatedAt(updatedAt)
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
