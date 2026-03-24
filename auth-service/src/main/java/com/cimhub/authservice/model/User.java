package com.cimhub.authservice.model;

import com.cimhub.common.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "users")
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    private String userId;

    @NotBlank
    @Email
    @Indexed(unique = true)
    private String email;

    @NotBlank
    private String password;

    @NotNull
    private UserType userType;

    private Date createdDate = new Date();
}
