package com.singhv.auth_service.model;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document
@AllArgsConstructor
@NoArgsConstructor
public class Users {

    @Id
    private String userId;

    @NotBlank
    @Email
    private String emailId;

    @NotBlank
    private String password;

    private Date createdDate = new Date();
}
