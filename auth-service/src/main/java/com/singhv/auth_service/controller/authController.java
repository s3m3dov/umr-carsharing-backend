package com.singhv.auth_service.controller;

import com.singhv.auth_service.dto.*;
import com.singhv.auth_service.services.authService;
import com.singhv.common.dto.ResponseDTO;
import jakarta.ws.rs.POST;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class authController {

    private final authService authService;

    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDTO<SignUpResponseDTO> signUp(@RequestBody UserInfoDTO userInfoDTO) {
        SignUpResponseDTO signUpResponse = authService.registerNewUser(userInfoDTO);
        ResponseDTO<SignUpResponseDTO> response = new ResponseDTO<>();
        response.setSuccess(true);
        response.setResponseContent(signUpResponse);
        response.setErrorMessage(null);
        return response;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDTO<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        LoginResponseDTO loginResponse = authService.checkUserLogin(loginRequestDTO);
        ResponseDTO<LoginResponseDTO> response = new ResponseDTO<>();
        response.setSuccess(true);
        response.setResponseContent(loginResponse);
        response.setErrorMessage(null);
        return response;
    }

    @PostMapping(value = "/validate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ValidateTokenResponseDTO validateToken(@RequestBody ValidateTokenRequestDTO requestContent) {
        return authService.validateToken(requestContent);
    }
}
