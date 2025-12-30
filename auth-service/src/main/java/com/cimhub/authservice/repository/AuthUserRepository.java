package com.cimhub.authservice.repository;

import com.cimhub.authservice.model.Users;
import org.apache.tomcat.util.buf.UDecoder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Repository
public interface AuthUserRepository extends MongoRepository<Users, String> {
    Users findByEmailId(String email);
}
