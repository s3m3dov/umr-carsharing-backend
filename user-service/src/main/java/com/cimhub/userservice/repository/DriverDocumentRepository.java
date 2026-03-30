package com.cimhub.userservice.repository;

import com.cimhub.userservice.model.DriverDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverDocumentRepository extends MongoRepository<DriverDocument, String> {
    List<DriverDocument> findByDriverId(String driverId);
}
