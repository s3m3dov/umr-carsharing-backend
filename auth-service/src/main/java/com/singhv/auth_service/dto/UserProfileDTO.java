package com.singhv.auth_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class UserProfileDTO {
    private String userId;
    private String emailId;
    private String fullName;
    private String phoneNumber;
    private int age;
    private Date dob;
    private String empId;
    private String organisationName;
    private String profilePicUrl;
}

