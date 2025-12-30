package com.singhv.userservice.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document
public class User {
    @Id
    private String userId;

    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String emailId;

    @NotBlank
    private String phoneNumber;


    @Positive
    private int age;

    @Past
    private Date dob;

    private String empId;

    private String organisationName;

    private String profilePicUrl;

    private Date createdDate = new Date();

    private String createdBy;

    public User() {
        this.createdDate = new Date();
    }

    public void initializeCreatedBy() {
        if (this.createdBy == null && this.emailId != null) {
            this.createdBy = this.emailId;
        }
    }
}