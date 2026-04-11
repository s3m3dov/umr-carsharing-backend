package com.cimhub.reviewservice.model;

import com.cimhub.common.enums.UserType;
import java.util.Date;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "users")
public class User {

    @Id
    @Field("userId")
    private String userId;

    @Field("firstName")
    private String firstName;

    @Field("lastName")
    private String lastName;

    @Field("email")
    private String email;

    @Field("phoneNumber")
    private String phoneNumber;

    @Field("userType")
    private UserType userType;

    @Field("age")
    private int age;

    @Field("dob")
    private Date dob;
}
