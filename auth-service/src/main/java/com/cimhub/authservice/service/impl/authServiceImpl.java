package com.cimhub.authservice.service.impl;

import com.cimhub.authservice.client.UserServiceClient;
import com.cimhub.authservice.dto.*;
import com.cimhub.authservice.model.User;
import com.cimhub.authservice.repository.AuthUserRepository;
import com.cimhub.authservice.security.JwtService;
import com.cimhub.authservice.service.authService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class authServiceImpl implements authService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserServiceClient userServiceClient;

    @Override
    public SignUpResponseDTO registerNewUser(UserInfoDTO userInfo) {

        if (authUserRepository.findByEmailId(userInfo.getEmailId()) != null) {
            throw new com.cimhub.authservice.exception.UserAlreadyExistsException(
                    "User Already Exists with emailId: " + userInfo.getEmailId());
        }

        // Create auth record with only authentication data
        User newUser = new User();
        newUser.setUserId(userInfo.getEmailId().toLowerCase());
        newUser.setEmailId(userInfo.getEmailId());
        newUser.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        newUser.setCreatedDate(new java.util.Date());
        authUserRepository.save(newUser);

        // Call user-service to create profile
        UserProfileDTO profileDTO = UserProfileDTO.builder()
                .userId(newUser.getUserId())
                .emailId(newUser.getEmailId())
                .fullName(userInfo.getFullName())
                .phoneNumber(userInfo.getPhoneNumber())
                .age(userInfo.getAge())
                .dob(userInfo.getDob())
                .empId(userInfo.getEmpId())
                .organisationName(userInfo.getOrganisationName())
                .profilePicUrl(userInfo.getProfilePicUrl())
                .build();

        try {
            userServiceClient.createUserProfile(profileDTO);
        } catch (Exception e) {
            // If user-service fails, rollback auth creation
            authUserRepository.delete(newUser);
            throw new RuntimeException("Failed to create user profile: " + e.getMessage());
        }

        // Generate JWT token
        String token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(newUser.getEmailId())
                        .password(newUser.getPassword())
                        .roles("USER")
                        .build());

        return SignUpResponseDTO.builder()
                .token(token)
                .emailId(newUser.getEmailId())
                .build();
    }

    @Override
    public LoginResponseDTO checkUserLogin(LoginRequestDTO requestContent) {

        try {
            authenticationManager.authenticate(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            requestContent.getEmailId(),
                            requestContent.getPassword()));
        } catch (Exception e) {
            throw new RuntimeException("Invalid Credentials");
        }

        User userData = authUserRepository.findByEmailId(requestContent.getEmailId().toLowerCase());

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(userData.getEmailId())
                .password(userData.getPassword())
                .roles("USER")
                .build();

        String token = jwtService.generateToken(userDetails);

        return LoginResponseDTO.builder()
                .token(token)
                .emailId(userData.getEmailId())
                .build();
    }

    @Override
    public ValidateTokenResponseDTO validateToken(ValidateTokenRequestDTO requestContent) {
        try {
            String emailId = jwtService.extractUsername(requestContent.getToken());
            boolean isTokenValid = jwtService.isTokenValid(requestContent.getToken(), emailId);

            return ValidateTokenResponseDTO.builder()
                    .isValid(isTokenValid)
                    .emailId(emailId)
                    .build();
        } catch (Exception e) {
            return ValidateTokenResponseDTO.builder()
                    .isValid(false)
                    .emailId(null)
                    .build();
        }
    }
}
