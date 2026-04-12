package com.cimhub.tripservice.repository;

import com.cimhub.tripservice.model.Passenger;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassengerRepository extends MongoRepository<Passenger, String> {

    List<Passenger> findByUserIdIn(List<String> userIds);

    void deleteByUserId(String userId);
}
