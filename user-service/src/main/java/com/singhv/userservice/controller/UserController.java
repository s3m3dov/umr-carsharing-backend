package com.singhv.userservice.controller;

import com.singhv.common.dto.RequestDTO;
import com.singhv.common.dto.ResponseDTO;
import com.singhv.userservice.dto.LoginRequestDTO;
import com.singhv.userservice.dto.LoginResponseDTO;
import com.singhv.userservice.dto.SignUpResponseDTO;
import com.singhv.userservice.dto.UserInfoDTO;
import com.singhv.userservice.dto.UserProfileDTO;
import com.singhv.userservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
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

    @GetMapping(value = "/{userId}")
    public ResponseDTO<UserInfoDTO> getUserInfo(@PathVariable String userId) {
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

//    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseDTO<SignUpResponseDTO> addNewUser(@RequestBody RequestDTO<UserInfoDTO> requestDTO) {
//        log.info("Signup request: {}", requestDTO);
//        ResponseDTO<SignUpResponseDTO> response = new ResponseDTO<>();
//
//        try {
//            SignUpResponseDTO signUpResponse = userService.registerNewUser(requestDTO.getRequestContent());
//            if (signUpResponse.isSignUpSuccess()) {
//                response.setResponseContent(signUpResponse);
//                response.setSuccess(true);
//                response.setErrorMessage(null);
//            } else {
//                response.setResponseContent(null);
//                response.setSuccess(false);
//                response.setErrorMessage("Registration failed");
//            }
//        } catch (Exception exp) {
//            response.setResponseContent(null);
//            response.setSuccess(false);
//            response.setErrorMessage(exp.getMessage());
//            exp.printStackTrace();
//        }
//        return response;
//    }
//
//    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseDTO<LoginResponseDTO> loginUser(@RequestBody RequestDTO<LoginRequestDTO> loginInfo) {
//        ResponseDTO<LoginResponseDTO> response = new ResponseDTO<>();
//
//        try {
//            LoginResponseDTO loginResponse = userService.checkUserLogin(loginInfo.getRequestContent());
//            if (loginResponse.isLoginSuccess()) {
//                response.setResponseContent(loginResponse);
//                response.setSuccess(true);
//                response.setErrorMessage(null);
//            } else {
//                response.setResponseContent(null);
//                response.setSuccess(false);
//                response.setErrorMessage(loginResponse.getErrMsg());
//            }
//        } catch (Exception exp) {
//            response.setResponseContent(null);
//            response.setSuccess(false);
//            response.setErrorMessage(exp.getMessage());
//            exp.printStackTrace();
//        }
//        return response;
//    }
}