package com.cimhub.authservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "blacklisted_tokens")
@Data
public class BlacklistedToken {
    @Id
    private String id;
    private String token;
    private Date expiresAt;
}
