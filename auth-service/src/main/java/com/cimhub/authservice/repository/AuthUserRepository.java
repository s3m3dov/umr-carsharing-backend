package com.cimhub.authservice.repository;

import com.cimhub.authservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthUserRepository extends MongoRepository<User, String> {
    User findByEmailId(String email);
}
