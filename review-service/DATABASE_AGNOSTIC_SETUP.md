# Review Service Database-Agnostic Setup

The review-service is designed to be **database-agnostic**, supporting both MongoDB and relational databases (PostgreSQL/MySQL) without code changes. The implementation switches between databases using Spring profiles.

## Architecture Overview

### Key Components

1. **Custom ReviewRepository Interface** - Abstracted interface with no database-specific dependencies
2. **Review Model** - Annotated with both `@Document` (MongoDB) and `@Entity/@Column` (JPA) annotations
3. **Two Implementation Classes**:
   - `MongoReviewRepositoryImpl` - Uses Spring Data MongoDB
   - `JpaReviewRepositoryImpl` - Uses Spring Data JPA
4. **Profile-based Activation** - Implementations are activated via Spring `@Profile` annotations
5. **PersistenceConfig** - Conditionally enables MongoDB or JPA repositories based on active profile

## Deployment Scenarios

### Scenario 1: MongoDB Deployment

**Default Configuration** (profile: `mongodb`)

```bash
# Using environment variable
export SPRING_PROFILES_ACTIVE=mongodb
java -jar review-service.jar

# Or via Docker
docker run -e SPRING_PROFILES_ACTIVE=mongodb \
           -e MONGODB_URI=mongodb://mongo:27017/carsharing \
           review-service:latest
```

**Configuration File**: `application-mongodb.yml`
- MongoDB connection via `MONGODB_URI` environment variable
- Disables JPA and datasource configurations

### Scenario 2: PostgreSQL Deployment

```bash
# Using environment variable
export SPRING_PROFILES_ACTIVE=relational
export DB_URL=jdbc:postgresql://postgres:5432/carsharing
export DB_USERNAME=postgres
export DB_PASSWORD=yourpassword
java -jar review-service.jar

# Or via Docker Compose
docker-compose -f docker-compose-relational.yml up
```

**Configuration File**: `application-relational.yml`
- PostgreSQL connection via `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- Disables MongoDB configuration
- Uses Hibernate with PostgreSQL dialect
- Default DDL mode: `validate` (schema must exist)

### Scenario 3: MySQL Deployment

```bash
# Update spring.datasource.driver-class-name to MySQL driver
export SPRING_PROFILES_ACTIVE=relational
export DB_URL=jdbc:mysql://mysql:3306/carsharing
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
java -jar review-service.jar
```

## File Structure

```
review-service/
├── pom.xml                                          # Dependencies for MongoDB and JPA
├── Dockerfile                                       # Container build
├── src/main/
│   ├── java/com/cimhub/reviewservice/
│   │   ├── model/
│   │   │   └── Review.java                         # Dual-annotated model
│   │   ├── repository/
│   │   │   ├── ReviewRepository.java               # Abstracted interface
│   │   │   ├── MongoReviewDataRepository.java      # Spring Data MongoDB interface
│   │   │   ├── MongoReviewRepositoryImpl.java       # MongoDB implementation
│   │   │   ├── JpaReviewDataRepository.java        # Spring Data JPA interface
│   │   │   └── JpaReviewRepositoryImpl.java         # JPA implementation
│   │   ├── service/
│   │   │   └── ReviewService.java                  # Service using ReviewRepository
│   │   ├── config/
│   │   │   └── PersistenceConfig.java             # Profile-based config
│   │   └── ReviewServiceApplication.java
│   └── resources/
│       ├── application.yml                         # Base configuration
│       ├── application-mongodb.yml                 # MongoDB profile
│       └── application-relational.yml              # Relational profile
```

## Key Design Decisions

### 1. Custom Interface Over Spring Data Repositories

**Why**: The `ReviewRepository` interface doesn't extend `MongoRepository` or `JpaRepository`. This ensures:
- Service layer has no database-specific dependencies
- Clean separation of concerns
- Easy to mock for testing

```java
// ✓ Correct: Abstracted interface
public interface ReviewRepository {
    Review save(Review review);
    List<Review> findByRevieweeId(String revieweeId);
    // ...
}

// ✗ Avoid: Database-specific extends
// public interface ReviewRepository extends MongoRepository<Review, String>
```

### 2. Dual Model Annotations

The `Review` model is annotated for both MongoDB and JPA:

```java
@Document(collection = "reviews")      // MongoDB
@Entity                                 // JPA/Relational
@Table(name = "reviews")               // JPA
public class Review {
    @Id                                 // Works for both
    @GeneratedValue(strategy = GenerationType.UUID)
    private String reviewId;

    @Field("tripId")                   // MongoDB field name
    @Column(name = "trip_id")          // JPA column name
    private String tripId;

    // ... more fields with dual annotations
}
```

### 3. Profile-based Component Selection

Implementation classes use `@Profile` to activate based on configuration:

```java
@Component
@Profile("mongodb")
public class MongoReviewRepositoryImpl implements ReviewRepository { ... }

@Component
@Profile("relational")
public class JpaReviewRepositoryImpl implements ReviewRepository { ... }
```

Only one implementation will be loaded at runtime based on the active profile.

### 4. Conditional Repository Scanning

`PersistenceConfig` conditionally enables repository scanning:

```java
@Configuration
@EnableMongoRepositories(basePackages = {...})
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "mongodb")
public static class MongoConfig { ... }

@Configuration
@EnableJpaRepositories(basePackages = {...})
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "relational")
public static class RelationalConfig { ... }
```

## Migration Between Databases

### From MongoDB to PostgreSQL

1. **Export MongoDB data** to a format both databases understand (JSON/CSV)
2. **Create PostgreSQL schema** matching the Review model's column definitions
3. **Import data** into PostgreSQL
4. **Update deployment configuration**:
   ```bash
   # Change environment variables
   SPRING_PROFILES_ACTIVE=relational
   DB_URL=jdbc:postgresql://postgres:5432/carsharing
   DB_USERNAME=postgres
   DB_PASSWORD=password
   ```
5. **Deploy** the service - no code changes needed!

### From PostgreSQL back to MongoDB

Simply switch the profile back:
```bash
SPRING_PROFILES_ACTIVE=mongodb
MONGODB_URI=mongodb://mongo:27017/carsharing
```

## Testing

### Unit Tests

The service uses interface-based dependency injection, making it easy to mock:

```java
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    // Tests work regardless of underlying database
}
```

### Integration Tests

Create separate test classes for each database:

```java
@SpringBootTest
@ActiveProfiles("mongodb")
class MongoReviewServiceIntegrationTest { ... }

@SpringBootTest
@ActiveProfiles("relational")
class JpaReviewServiceIntegrationTest { ... }
```

## Environment Variables

| Variable | Purpose | Default | Example |
|----------|---------|---------|---------|
| `SPRING_PROFILES_ACTIVE` | Active profile | `mongodb` | `mongodb`, `relational` |
| `MONGODB_URI` | MongoDB connection | `mongodb://localhost:27017/carsharing` | `mongodb://mongo:27017/carsharing` |
| `DB_URL` | JDBC URL (relational) | `jdbc:postgresql://localhost:5432/carsharing` | `jdbc:postgresql://postgres:5432/carsharing` |
| `DB_USERNAME` | Database username | `postgres` | `postgres`, `root` |
| `DB_PASSWORD` | Database password | `password` | Your actual password |
| `JPA_DDL_AUTO` | Hibernate DDL mode | `validate` | `validate`, `update`, `create` |

## Performance Considerations

### MongoDB
- Document-based: Better for nested/hierarchical data
- No schema migration needed
- Suitable for rapid prototyping

### PostgreSQL/MySQL
- ACID compliance
- Better for complex queries with joins
- Mature ecosystem and tooling
- Better for high-consistency requirements

## Troubleshooting

### Issue: "No Repository Beans Found"

**Solution**: Verify the active profile matches your deployment target:
```bash
# Check which profile is active
java -jar review-service.jar --debug | grep "active"

# Explicitly set profile
java -Dspring.profiles.active=mongodb -jar review-service.jar
```

### Issue: Database Connection Failures

**MongoDB**:
```bash
# Verify MongoDB URI
echo $MONGODB_URI
# Should output: mongodb://host:port/database
```

**PostgreSQL**:
```bash
# Test connection
psql -h $DB_HOST -U $DB_USERNAME -d carsharing
```

### Issue: Hibernate Dialect Not Found

**Solution**: Ensure the correct driver JAR is in classpath:
```xml
<!-- For PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>

<!-- For MySQL -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>
```

## Future Enhancements

1. **Database Migration Tools**: Integrate Liquibase for schema versioning
2. **Read Replicas**: Support read-only MongoDB replicas
3. **Caching Layer**: Add Redis caching on top of both databases
4. **Query Performance**: Add database-specific query optimization
5. **Monitoring**: Database-specific health checks and metrics

## References

- [Spring Data MongoDB Documentation](https://spring.io/projects/spring-data-mongodb)
- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Spring Boot Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
- [Hibernate PostgreSQL Dialect](https://hibernate.org/orm/documentation/)
