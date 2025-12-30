package com.singhv.userservice.controller;

import com.singhv.common.dto.RequestDTO;
import com.singhv.common.dto.ResponseDTO;
import com.singhv.userservice.dto.LoginRequestDTO;
import com.singhv.userservice.dto.LoginResponseDTO;
import com.singhv.userservice.dto.SignUpResponseDTO;
import com.singhv.userservice.dto.UserInfoDTO;
import com.singhv.userservice.dto.UserProfileDTO;
import com.singhv.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "User registration, authentication, and profile management APIs")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/health")
    public ResponseDTO<String> healthCheck() {
        ResponseDTO<String> response = new ResponseDTO<>();
        response.setSuccess(true);
        response.setErrorMessage(null);
        response.setResponseContent("User Service is up and running!");
        return response;
    }

    @Operation(summary = "Get user information", description = "Retrieves user profile information by user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found", content = @Content(schema = @Schema(implementation = ResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping(value = "/{userId}")
    public ResponseDTO<UserInfoDTO> getUserInfo(
            @Parameter(description = "User's unique identifier", required = true) @PathVariable String userId) {
        log.info("Get user info for userId: {}", userId);
        ResponseDTO<UserInfoDTO> response = new ResponseDTO<>();

        try {
            UserInfoDTO userInfo = userService.getUserInfo(userId);
            response.setResponseContent(userInfo);
            response.setSuccess(true);
            response.setErrorMessage(null);
        } catch (Exception exp) {
            response.setResponseContent(null);
            response.setSuccess(false);
            response.setErrorMessage(exp.getMessage());
            exp.printStackTrace();
        }
        return response;
    }

    @Operation(summary = "Register new user", description = "Creates a new user account in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(schema = @Schema(implementation = ResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
    })
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDTO<SignUpResponseDTO> addNewUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User registration details", required = true) @RequestBody RequestDTO<UserInfoDTO> requestDTO) {
        log.info("Signup request: {}", requestDTO);
        ResponseDTO<SignUpResponseDTO> response = new ResponseDTO<>();

        try {
            SignUpResponseDTO signUpResponse = userService.registerNewUser(requestDTO.getRequestContent());
            if (signUpResponse.isSignUpSuccess()) {
                response.setResponseContent(signUpResponse);
                response.setSuccess(true);
                response.setErrorMessage(null);
            } else {
                response.setResponseContent(null);
                response.setSuccess(false);
                response.setErrorMessage("Registration failed");
            }
        } catch (Exception exp) {
            response.setResponseContent(null);
            response.setSuccess(false);
            response.setErrorMessage(exp.getMessage());
            exp.printStackTrace();
        }
        return response;
    }

    @Operation(summary = "User login", description = "Authenticates a user and returns login details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = ResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDTO<LoginResponseDTO> loginUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User login credentials", required = true) @RequestBody RequestDTO<LoginRequestDTO> loginInfo) {
        ResponseDTO<LoginResponseDTO> response = new ResponseDTO<>();

        try {
            LoginResponseDTO loginResponse = userService.checkUserLogin(loginInfo.getRequestContent());
            if (loginResponse.isLoginSuccess()) {
                response.setResponseContent(loginResponse);
                response.setSuccess(true);
                response.setErrorMessage(null);
            } else {
                response.setResponseContent(null);
                response.setSuccess(false);
                response.setErrorMessage(loginResponse.getErrMsg());
            }
        } catch (Exception exp) {
            response.setResponseContent(null);
            response.setSuccess(false);
            response.setErrorMessage(exp.getMessage());
            exp.printStackTrace();
        }
        return response;
    }

    @Operation(summary = "Create user profile", description = "Creates a detailed user profile")
    @PostMapping(value = "/profile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDTO<String> createUserProfile(@RequestBody UserProfileDTO profileDTO) {
        log.info("Create user profile for userId: {}", profileDTO.getUserId());
        ResponseDTO<String> response = new ResponseDTO<>();

        try {
            userService.createUserProfile(profileDTO);
            response.setResponseContent("Profile created successfully");
            response.setSuccess(true);
            response.setErrorMessage(null);
        } catch (Exception exp) {
            response.setResponseContent(null);
            response.setSuccess(false);
            response.setErrorMessage(exp.getMessage());
            exp.printStackTrace();
        }
        return response;
    }
}