package com.cimhub.authservice.services;

import com.cimhub.authservice.dto.*;

public interface authService {

    SignUpResponseDTO registerNewUser(UserInfoDTO userInfo);
    LoginResponseDTO checkUserLogin(LoginRequestDTO requestContent);
    ValidateTokenResponseDTO validateToken(ValidateTokenRequestDTO requestContent);
}
