package com.cimhub.tripservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.cimhub.tripservice", "com.cimhub.common"})
public class TripServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TripServiceApplication.class, args);
    }
}
