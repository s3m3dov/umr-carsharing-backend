package com.cimhub.authservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignUpResponseDTO {
    private String  token;
    private String emailId;
}