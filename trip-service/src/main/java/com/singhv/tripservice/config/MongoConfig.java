package com.singhv.tripservice.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * MongoDB Configuration to ensure geospatial indexes are created
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class MongoConfig {

    private final MongoTemplate mongoTemplate;

    @Bean
    CommandLineRunner initIndexes() {
        return args -> {
            try {
                MongoDatabase database = mongoTemplate.getDb();
                MongoCollection<Document> collection = database.getCollection("trips");

                // Check and create indexes only if they don't exist
                if (!indexExists(collection, "sourceLocation_2dsphere")) {
                    collection.createIndex(
                            Indexes.geo2dsphere("sourceLocation"),
                            new IndexOptions().name("sourceLocation_2dsphere")
                    );
                    log.info("✅ Created 2dsphere index on sourceLocation");
                } else {
                    log.debug("Index 'sourceLocation_2dsphere' already exists, skipping creation");
                }

                if (!indexExists(collection, "destinationLocation_2dsphere")) {
                    collection.createIndex(
                            Indexes.geo2dsphere("destinationLocation"),
                            new IndexOptions().name("destinationLocation_2dsphere")
                    );
                    log.info("✅ Created 2dsphere index on destinationLocation");
                } else {
                    log.debug("Index 'destinationLocation_2dsphere' already exists, skipping creation");
                }

                // Optional: Create index on routeGeometry for route-based queries
                if (!indexExists(collection, "routeGeometry_2dsphere")) {
                    collection.createIndex(
                            Indexes.geo2dsphere("routeGeometry"),
                            new IndexOptions().name("routeGeometry_2dsphere")
                    );
                    log.info("✅ Created 2dsphere index on routeGeometry");
                } else {
                    log.debug("Index 'routeGeometry_2dsphere' already exists, skipping creation");
                }

                log.info("Geospatial index initialization completed");
            } catch (Exception e) {
                log.error("Error during geospatial index initialization", e);
            }
        };
    }

    /**
     * Check if an index with the given name already exists
     */
    private boolean indexExists(MongoCollection<Document> collection, String indexName) {
        for (Document index : collection.listIndexes()) {
            if (indexName.equals(index.getString("name"))) {
                return true;
            }
        }
        return false;
    }
}
