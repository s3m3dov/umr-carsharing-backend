package com.cimhub.vehicleservice.repository;

import com.cimhub.vehicleservice.model.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    List<Vehicle> findByUserId(@Param("userId") String userId);
    boolean existsByVehicleNumber(@Param("vehicleNumber") String vehicleNumber);
}