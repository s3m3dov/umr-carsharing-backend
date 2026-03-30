package com.cimhub.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Stores metadata for driver documents (NIDA, License, etc.).
 */
@Document(collection = "driver_documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverDocument {

    @Id
    private String id;

    private String driverId;
    private String documentType; // NIDA, LICENSE
    private String fileName;
    private String fileUrl;
    private long fileSize;

    @Builder.Default
    private Instant uploadedAt = Instant.now();
}
