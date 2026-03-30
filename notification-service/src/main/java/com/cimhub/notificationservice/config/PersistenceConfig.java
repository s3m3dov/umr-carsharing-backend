package com.cimhub.notificationservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@Slf4j
public class PersistenceConfig {

    @Configuration
    @EnableMongoRepositories(basePackages = "com.cimhub.notificationservice.repository")
    public static class MongoConfig {
        public MongoConfig() {
            log.info("MongoDB persistence configuration loaded for notification-service");
        }
    }
}
