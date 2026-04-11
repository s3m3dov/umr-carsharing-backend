package com.cimhub.userservice.vehicle.repository;

import com.cimhub.userservice.vehicle.model.Vehicle;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    List<Vehicle> findByUserId(@Param("userId") String userId);

    boolean existsByUserId(@Param("userId") String userId);

    boolean existsByVehicleNumber(@Param("vehicleNumber") String vehicleNumber);
}
