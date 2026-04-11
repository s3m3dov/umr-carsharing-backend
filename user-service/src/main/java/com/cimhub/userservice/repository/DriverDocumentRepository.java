package com.cimhub.userservice.repository;

import com.cimhub.userservice.model.DriverDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverDocumentRepository extends MongoRepository<DriverDocument, String> {
    List<DriverDocument> findByDriverId(String driverId);
}
