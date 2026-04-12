package com.cimhub.notificationservice.repository;

import com.cimhub.notificationservice.model.Notification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId, Pageable pageable);

    List<Notification> findByRecipientIdAndReadFalse(String recipientId);

    long countByRecipientIdAndReadFalse(String recipientId);

    void deleteByRecipientId(String recipientId);

    void deleteBySenderId(String senderId);
}
