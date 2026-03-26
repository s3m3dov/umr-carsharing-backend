package com.cimhub.reviewservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Persistence configuration for enabling MongoDB repositories.
 * Also scans user-service repositories to access user data directly.
 */
@Configuration
@Slf4j
public class PersistenceConfig {

    @Configuration
    @EnableMongoRepositories(basePackages = {
            "com.cimhub.reviewservice.repository",
            "com.cimhub.userservice.repository"
    })
    public static class MongoConfig {
        public MongoConfig() {
            log.info("MongoDB persistence configuration loaded - includes user-service repositories");
        }
    }
}