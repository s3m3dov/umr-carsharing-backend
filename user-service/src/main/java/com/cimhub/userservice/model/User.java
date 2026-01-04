package com.cimhub.userservice.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String userId;

    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    @Indexed(unique = true)
    private String email;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String password;

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
        if (this.createdBy == null && this.email != null) {
            this.createdBy = this.email;
        }
    }
}