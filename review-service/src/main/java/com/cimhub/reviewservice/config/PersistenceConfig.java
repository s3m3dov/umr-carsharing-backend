package com.cimhub.reviewservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Persistence configuration for enabling MongoDB or JPA repositories based on active profile.
 * This configuration allows the application to switch between MongoDB and relational databases.
 */
@Configuration
@Slf4j
public class PersistenceConfig {

    /**
     * Enable MongoDB repositories when mongodb profile is active.
     */
    @Configuration
    @EnableMongoRepositories(basePackages = {
            "com.cimhub.reviewservice.repository"
    })
    @ConditionalOnProperty(
            name = "spring.profiles.active",
            havingValue = "mongodb"
    )
    @Slf4j
    public static class MongoConfig {
        public MongoConfig() {
            log.info("MongoDB persistence configuration loaded");
        }
    }

    /**
     * Enable JPA repositories when relational profile is active.
     */
    @Configuration
    @EnableJpaRepositories(basePackages = {
            "com.cimhub.reviewservice.repository"
    })
    @ConditionalOnProperty(
            name = "spring.profiles.active",
            havingValue = "relational"
    )
    @Slf4j
    public static class RelationalConfig {
        public RelationalConfig() {
            log.info("JPA (relational database) persistence configuration loaded");
        }
    }
}
