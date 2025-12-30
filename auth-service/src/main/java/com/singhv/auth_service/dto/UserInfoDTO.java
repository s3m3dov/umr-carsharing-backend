package com.singhv.auth_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class UserInfoDTO {
    private String fullName;
    private String emailId;
    private String userId = emailId;
    private String phoneNumber;
    private String password;
    private int age;
    private Date dob;
    private String empId;
    private String organisationName;
    private String profilePicUrl;
}