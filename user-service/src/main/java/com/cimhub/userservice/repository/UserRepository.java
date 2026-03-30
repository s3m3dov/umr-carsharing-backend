package com.cimhub.userservice.repository;

import com.cimhub.common.enums.DriverStatus;
import com.cimhub.common.enums.UserType;
import com.cimhub.userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    User findByUserId(@Param("userId") String userId);

    User findByEmail(@Param("email") String email);

    Page<User> findByUserType(UserType userType, Pageable pageable);

    Page<User> findByUserTypeAndDriverStatus(UserType userType, DriverStatus driverStatus, Pageable pageable);

    long countByUserType(UserType userType);

    long countByUserTypeAndDriverStatus(UserType userType, DriverStatus driverStatus);
}