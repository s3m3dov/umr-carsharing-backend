package com.cimhub.userservice.repository;

import com.cimhub.common.enums.DriverStatus;
import com.cimhub.userservice.model.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends MongoRepository<Driver, String> {
    Optional<Driver> findByUserId(String userId);

    Optional<Driver> findByEmail(String email);

    List<Driver> findByUserIdIn(List<String> userIds);

    Page<Driver> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    Page<Driver> findByDriverStatus(DriverStatus driverStatus, Pageable pageable);

    long countByDriverStatus(DriverStatus driverStatus);

    void deleteByUserId(String userId);
}
