package com.cimhub.userservice.service;

import com.cimhub.userservice.dto.request.CreateProfileRequest;
import com.cimhub.userservice.dto.request.LoginRequest;
import com.cimhub.userservice.dto.request.RegisterUserRequest;
import com.cimhub.userservice.dto.response.LoginResponse;
import com.cimhub.userservice.dto.response.SignUpResponse;
import com.cimhub.userservice.dto.response.UserResponse;
import com.cimhub.userservice.model.User;
import com.cimhub.userservice.repository.UserRepository;
import com.cimhub.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void createProfile(CreateProfileRequest request) {
        log.info("Creating user profile for userId: {}", request.userId());

        User existingUser = userRepository.findByUserId(request.userId());
        if (existingUser != null) {
            throw BusinessException.alreadyExists("User profile", request.userId());
        }

        User newUser = new User();
        // Since it's a record, we can't use BeanUtils easily if we want to be clean,
        // but User is still a POJO.
        newUser.setUserId(request.userId());
        newUser.setEmailId(request.emailId());
        newUser.setFullName(request.fullName());
        newUser.setPhoneNumber(request.phoneNumber());
        newUser.setAge(request.age());
        newUser.setDob(request.dob());
        newUser.setEmpId(request.empId());
        newUser.setOrganisationName(request.organisationName());
        newUser.setProfilePicUrl(request.profilePicUrl());

        newUser.setCreatedBy(request.emailId());
        newUser.setCreatedDate(new Date());

        userRepository.save(newUser);
        log.info("User profile created successfully for userId: {}", request.userId());
    }

    public UserResponse getUserInfo(String userId) {
        User foundUser = userRepository.findByUserId(userId);

        if (foundUser == null) {
            throw BusinessException.notFound("User", userId);
        }

        return new UserResponse(
                foundUser.getFullName(),
                foundUser.getEmailId(),
                foundUser.getUserId(),
                foundUser.getPhoneNumber(),
                foundUser.getAge(),
                foundUser.getDob(),
                foundUser.getEmpId(),
                foundUser.getOrganisationName(),
                foundUser.getProfilePicUrl());
    }

    public SignUpResponse registerUser(RegisterUserRequest request) {
        User newUser = new User();
        newUser.setFullName(request.fullName());
        newUser.setEmailId(request.emailId());
        newUser.setUserId(request.emailId().toLowerCase());
        newUser.setPhoneNumber(request.phoneNumber());
        newUser.setAge(request.age());
        newUser.setDob(request.dob());
        newUser.setEmpId(request.empId());
        newUser.setOrganisationName(request.organisationName());
        newUser.setProfilePicUrl(request.profilePicUrl());
        newUser.setCreatedDate(new Date());

        log.info("Registering new user: {}", newUser.getUserId());
        userRepository.save(newUser);

        User savedUser = userRepository.findByUserId(request.emailId().toLowerCase());
        return new SignUpResponse(
                savedUser.getUserId(),
                savedUser.getFullName(),
                true);
    }

    public LoginResponse login(LoginRequest request) {
        User userData = userRepository.findByUserId(request.emailId().toLowerCase());
        log.info("Login check for user: {}", request.emailId());

        if (userData != null) {
            if (userData.getPassword() != null && userData.getPassword().equals(request.password())) {
                return new LoginResponse(
                        userData.getFullName(),
                        userData.getUserId(),
                        true,
                        null);
            } else {
                throw BusinessException.unauthorized("Invalid credentials");
            }
        } else {
            throw BusinessException.unauthorized("Invalid credentials");
        }
    }
}