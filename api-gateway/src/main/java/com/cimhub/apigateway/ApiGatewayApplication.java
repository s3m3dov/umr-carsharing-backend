package com.cimhub.apigateway;

import com.cimhub.apigateway.config.SecurityProperties;
import com.cimhub.apigateway.config.ServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.cimhub.apigateway"})
@EnableConfigurationProperties({ SecurityProperties.class, ServiceProperties.class })
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
