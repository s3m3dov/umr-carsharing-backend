package com.cimhub.authservice.service;

import com.cimhub.authservice.dto.*;

public interface authService {

    SignUpResponseDTO registerNewUser(UserInfoDTO userInfo);

    LoginResponseDTO checkUserLogin(LoginRequestDTO requestContent);

    ValidateTokenResponseDTO validateToken(ValidateTokenRequestDTO requestContent);
}
