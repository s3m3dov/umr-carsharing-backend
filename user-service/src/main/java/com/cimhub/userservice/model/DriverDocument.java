package com.cimhub.userservice.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Stores metadata for driver documents (NIDA, License, etc.).
 */
@Document(collection = "driver_documents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverDocument {

    @Id
    private String id;

    @Indexed
    private String driverId;

    private String documentType; // NIDA, LICENSE
    private String fileName;
    private String fileUrl;
    private long fileSize;

    @Builder.Default
    private Instant uploadedAt = Instant.now();
}
