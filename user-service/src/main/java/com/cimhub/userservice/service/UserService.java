package com.cimhub.userservice.service;

import com.cimhub.common.enums.UserType;
import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UserAlreadyExistsException;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.userservice.dto.request.CreateProfileRequest;
import com.cimhub.userservice.dto.request.LoginRequest;
import com.cimhub.userservice.dto.request.RegisterUserRequest;
import com.cimhub.userservice.dto.response.LoginResponse;
import com.cimhub.userservice.dto.response.SignUpResponse;
import com.cimhub.userservice.dto.response.UserResponse;
import com.cimhub.userservice.model.Driver;
import com.cimhub.userservice.model.Passenger;
import com.cimhub.userservice.model.User;
import com.cimhub.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse createProfile(CreateProfileRequest request) {
        log.info("Creating user profile for userId: {}", request.userId());

        User existingUser = userRepository.findByUserId(request.userId());
        if (existingUser != null) {
            throw new UserAlreadyExistsException("User profile already exists for " + request.userId());
        }

        // Create appropriate user type based on userType enum
        User newUser = createUserByType(request.userType());

        newUser.setUserId(request.userId());
        newUser.setEmail(request.email());
        newUser.setFirstName(request.firstName());
        newUser.setLastName(request.lastName());
        newUser.setPhoneNumber(request.phoneNumber());
        newUser.setAge(request.age());

        // Set the driver's license number if this is a driver
        if (newUser instanceof Driver driver) {
            driver.setLicenseNumber(request.licenseNumber());
        }

        newUser.setCreatedBy(request.email());
        newUser.setCreatedDate(new Date());

        userRepository.save(newUser);
        log.info("User profile created successfully for userId: {} as {}", request.userId(), request.userType());

        return mapUserToResponse(newUser);
    }

    private User createUserByType(UserType userType) {
        return switch (userType) {
            case DRIVER -> new Driver();
            case PASSENGER -> new Passenger();
            default -> new Passenger();
        };
    }

    private UserResponse mapUserToResponse(User user) {
        String licenseNumber = null;

        if (user instanceof Driver driver) {
            licenseNumber = driver.getLicenseNumber();
        }

        return new UserResponse(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUserType(),
                user.getPhoneNumber(),
                user.getAge(),
                licenseNumber);
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