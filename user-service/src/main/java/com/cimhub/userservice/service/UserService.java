package com.cimhub.userservice.service;

import com.cimhub.common.exception.ResourceNotFoundException;
import com.cimhub.common.exception.UserAlreadyExistsException;
import com.cimhub.common.exception.UnauthorizedException;
import com.cimhub.userservice.dto.request.CreateProfileRequest;
import com.cimhub.userservice.dto.request.LoginRequest;
import com.cimhub.userservice.dto.request.RegisterUserRequest;
import com.cimhub.userservice.dto.response.LoginResponse;
import com.cimhub.userservice.dto.response.SignUpResponse;
import com.cimhub.userservice.dto.response.UserResponse;
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

        User newUser = new User();
        // TODO: Add RateLimiter (Globally)
        // TODO: Make DB calls (create/update/delete) locking to avoid race conditions
        // TODO: Make User models in AuthService and UserService consistent

        newUser.setUserId(request.userId());
        newUser.setEmail(request.email());
        newUser.setUserType(request.userType());
        newUser.setFullName(request.fullName());
        newUser.setPhoneNumber(request.phoneNumber());
        newUser.setAge(request.age());
        newUser.setDob(request.dob());
        newUser.setEmpId(request.empId());
        newUser.setOrganisationName(request.organisationName());
        newUser.setProfilePicUrl(request.profilePicUrl());

        newUser.setCreatedBy(request.email());
        newUser.setCreatedDate(new Date());

        userRepository.save(newUser);
        log.info("User profile created successfully for userId: {}", request.userId());

        return new UserResponse(
                newUser.getFullName(),
                newUser.getEmail(),
                newUser.getUserId(),
                newUser.getUserType(),
                newUser.getPhoneNumber(),
                newUser.getAge(),
                newUser.getDob(),
                newUser.getEmpId(),
                newUser.getOrganisationName(),
                newUser.getProfilePicUrl());
    }

    public UserResponse getUserInfo(String userId) {
        User foundUser = userRepository.findByUserId(userId);

        if (foundUser == null) {
            throw new ResourceNotFoundException("User with id " + userId + " not found");
        }

        return new UserResponse(
                foundUser.getFullName(),
                foundUser.getEmail(),
                foundUser.getUserId(),
                foundUser.getUserType(),
                foundUser.getPhoneNumber(),
                foundUser.getAge(),
                foundUser.getDob(),
                foundUser.getEmpId(),
                foundUser.getOrganisationName(),
                foundUser.getProfilePicUrl());
    }

    public SignUpResponse registerUser(RegisterUserRequest request) {
        String normalizedEmail = request.email().toLowerCase();
        User newUser = new User();
        newUser.setFullName(request.fullName());
        newUser.setEmail(normalizedEmail);
        newUser.setUserId(normalizedEmail);
        newUser.setPhoneNumber(request.phoneNumber());
        newUser.setAge(request.age());
        newUser.setDob(request.dob());
        newUser.setEmpId(request.empId());
        newUser.setOrganisationName(request.organisationName());
        newUser.setProfilePicUrl(request.profilePicUrl());
        newUser.setCreatedDate(new Date());

        log.info("Registering new user: {}", newUser.getUserId());
        userRepository.save(newUser);

        User savedUser = userRepository.findByUserId(normalizedEmail);
        return new SignUpResponse(
                savedUser.getUserId(),
                savedUser.getFullName(),
                true);
    }

    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.email().toLowerCase();
        User userData = userRepository.findByUserId(normalizedEmail);
        log.info("Login check for user: {}", normalizedEmail);

        if (userData != null) {
            if (userData.getPassword() != null && userData.getPassword().equals(request.password())) {
                return new LoginResponse(
                        userData.getFullName(),
                        userData.getUserId(),
                        userData.getUserType(),
                        true,
                        null);
            } else {
                throw new UnauthorizedException("Invalid credentials");
            }
        } else {
            throw new UnauthorizedException("Invalid credentials");
        }
    }
}