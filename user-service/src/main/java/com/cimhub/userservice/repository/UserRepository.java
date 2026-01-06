package com.cimhub.userservice.repository;

import com.cimhub.userservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    User findByUserId(@Param("userId") String userId);

    User findByEmail(@Param("email") String email);
}