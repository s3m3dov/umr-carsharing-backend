package com.singhv.auth_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequestDTO {
    private String emailId;
    private String password;
}