package com.cimhub.authservice.model;

import com.cimhub.common.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Auth User model - stores authentication credentials and auth metadata.
 * Uses MongoDB for persistence.
 * Stored in "users" collection.
 */
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

    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    @Indexed(sparse = true)
    private String resetToken;

    private String resetTokenHash;
    private Date resetTokenExpiry;
}
