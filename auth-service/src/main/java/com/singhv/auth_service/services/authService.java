package com.singhv.auth_service.services;

import com.singhv.auth_service.dto.*;

public interface authService {

    SignUpResponseDTO registerNewUser(UserInfoDTO userInfo);
    LoginResponseDTO checkUserLogin(LoginRequestDTO requestContent);
    ValidateTokenResponseDTO validateToken(ValidateTokenRequestDTO requestContent);
}
