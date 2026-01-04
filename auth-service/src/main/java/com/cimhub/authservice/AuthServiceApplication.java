package com.cimhub.authservice;

import com.cimhub.authservice.client.UserServiceClient;
import com.cimhub.authservice.config.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.cimhub")
@EnableFeignClients(basePackageClasses = UserServiceClient.class)
@EnableConfigurationProperties({ SecurityProperties.class})
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

}
