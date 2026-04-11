package com.cimhub.userservice.repository;

import com.cimhub.userservice.model.Passenger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassengerRepository extends MongoRepository<Passenger, String> {
    Optional<Passenger> findByUserId(String userId);

    Optional<Passenger> findByEmail(String email);

    Page<Passenger> findAll(Pageable pageable);
}
