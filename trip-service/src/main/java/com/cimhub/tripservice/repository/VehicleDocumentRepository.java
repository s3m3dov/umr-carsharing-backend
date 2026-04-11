package com.cimhub.tripservice.repository;

import com.cimhub.tripservice.model.VehicleDocument;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleDocumentRepository extends MongoRepository<VehicleDocument, String> {

    Optional<VehicleDocument> findByUserId(String userId);
}
