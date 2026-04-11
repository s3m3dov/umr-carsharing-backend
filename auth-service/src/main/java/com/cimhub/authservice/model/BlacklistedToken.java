package com.cimhub.authservice.model;

import java.util.Date;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "blacklisted_tokens")
@Data
public class BlacklistedToken {
    @Id
    private String id;

    private String token;
    private Date expiresAt;
}
