package com.cimhub.notificationservice.model;

import com.cimhub.common.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private String notificationId;

    @NotBlank
    private String recipientId;

    private String senderId;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    private NotificationType type;

    private String relatedEntityId;

    private Map<String, Object> metadata;

    @Builder.Default
    private boolean read = false;

    private Instant readAt;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();
}
