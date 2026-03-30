package com.cimhub.userservice.repository;

import com.cimhub.common.enums.UserType;
import com.cimhub.userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    User findByUserId(@Param("userId") String userId);

    User findByEmail(@Param("email") String email);

    Page<User> findByUserType(UserType userType, Pageable pageable);

    List<User> findByUserIdIn(List<String> userIds);

    long countByUserType(UserType userType);
}
