package com.singhv.userservice.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserInfoDTO {
    private String fullName;
    private String emailId;
    private String userId = emailId;
    private String phoneNumber;
    private int age;
    private Date dob;
    private String empId;
    private String organisationName;
    private String profilePicUrl;
}