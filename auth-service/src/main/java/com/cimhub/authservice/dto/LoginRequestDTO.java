package com.cimhub.authservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequestDTO {
    private String emailId;
    private String password;
}