package com.cimhub.reviewservice.repository;

import com.cimhub.reviewservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    User findByUserId(String userId);

    boolean existsByUserId(String userId);
}
