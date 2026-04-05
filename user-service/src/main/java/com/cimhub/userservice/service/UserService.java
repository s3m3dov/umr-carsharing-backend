package com.cimhub.userservice.service;

import com.cimhub.common.enums.EntityType;
import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UserAlreadyExistsException;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.common.service.AuditService;
import com.cimhub.userservice.dto.request.CreateProfileRequest;
import com.cimhub.userservice.dto.request.LoginRequest;
import com.cimhub.userservice.dto.request.RegisterUserRequest;
import com.cimhub.userservice.dto.response.LoginResponse;
import com.cimhub.userservice.dto.response.SignUpResponse;
import com.cimhub.userservice.dto.response.UserResponse;
import com.cimhub.userservice.model.Driver;
import com.cimhub.userservice.model.Passenger;
import com.cimhub.userservice.model.User;
import com.cimhub.userservice.repository.DriverRepository;
import com.cimhub.userservice.repository.PassengerRepository;
import com.cimhub.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PassengerRepository passengerRepository;
    private final AuditService auditService;

    public UserResponse createProfile(CreateProfileRequest request) {
        log.info("Creating user profile for userId: {}", request.userId());

        User existingUser = userRepository.findByUserId(request.userId());
        if (existingUser != null) {
            throw new UserAlreadyExistsException("User profile already exists for " + request.userId());
        }

        User newUser = new User();

        newUser.setUserId(request.userId());
        newUser.setEmail(request.email());
        newUser.setUserType(request.userType());
        newUser.setFirstName(request.firstName());
        newUser.setLastName(request.lastName());
        newUser.setPhoneNumber(request.phoneNumber());
        newUser.setAge(request.age());

        newUser.setCreatedBy(request.email());
        Instant now = Instant.now();
        newUser.setCreatedAt(now);
        newUser.setUpdatedAt(now);

        userRepository.save(newUser);
        createTypeProfile(newUser, request);
        auditService.logCreate(toEntityType(request.userType()), request.userId(), request.email(), "User profile created");
        log.info("User profile created successfully for userId: {} as {}", request.userId(), request.userType());

        return mapUserToResponse(newUser);
    }

    private void createTypeProfile(User user, CreateProfileRequest request) {
        if (request.userType() == UserType.DRIVER) {
            Driver driver = new Driver();
            driver.setUserId(user.getUserId());
            driver.setLicenseNumber(request.licenseNumber());
            driver.setCreatedBy(user.getCreatedBy());
            driver.setCreatedAt(user.getCreatedAt());
            driver.setUpdatedAt(user.getUpdatedAt());
            driverRepository.save(driver);
            return;
        }

        if (request.userType() == UserType.PASSENGER) {
            Passenger passenger = new Passenger();
            passenger.setUserId(user.getUserId());
            passenger.setCreatedBy(user.getCreatedBy());
            passenger.setCreatedAt(user.getCreatedAt());
            passenger.setUpdatedAt(user.getUpdatedAt());
            passengerRepository.save(passenger);
        }
    }

    private EntityType toEntityType(UserType userType) {
        return switch (userType) {
            case DRIVER -> EntityType.DRIVER;
            case PASSENGER -> EntityType.PASSENGER;
            default -> EntityType.USER;
        };
    }

    private UserResponse mapUserToResponse(User user) {
        String licenseNumber = null;

        if (user.getUserType() == UserType.DRIVER) {
            licenseNumber = driverRepository.findByUserId(user.getUserId())
                    .map(Driver::getLicenseNumber)
                    .orElse(null);
        }

        return new UserResponse(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUserType(),
                user.getPhoneNumber(),
                user.getAge(),
                licenseNumber,
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    public UserResponse getUserInfo(String userId) {
        User foundUser = userRepository.findByUserId(userId);

        if (foundUser == null) {
            throw new ResourceNotFoundException("User with id " + userId + " not found");
        }

        return mapUserToResponse(foundUser);
    }

    /**
     * Deprecated: Use auth-service for registration instead.
     * This method is kept for backward compatibility but should not be called.
     */
    @Deprecated(forRemoval = true)
    public SignUpResponse registerUser(RegisterUserRequest request) {
        throw new UnsupportedOperationException("Registration must be done through auth-service");
    }

    /**
     * Deprecated: Use auth-service for login instead.
     * This method is kept for backward compatibility but should not be called.
     */
    @Deprecated(forRemoval = true)
    public LoginResponse login(LoginRequest request) {
        throw new UnsupportedOperationException("Login must be done through auth-service");
    }
}
