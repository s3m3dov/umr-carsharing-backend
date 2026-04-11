# Testing Guide - Car Sharing Backend

## Table of Contents
1. [Overview](#overview)
2. [Test Structure](#test-structure)
3. [Running Tests](#running-tests)
4. [Code Coverage](#code-coverage)
5. [Test Types](#test-types)
6. [Module-Specific Tests](#module-specific-tests)
7. [Writing New Tests](#writing-new-tests)
8. [CI/CD Integration](#cicd-integration)

---

## Overview

The Car Sharing Backend project uses a comprehensive testing strategy with unit tests (Mockito), controller tests (@WebMvcTest), and integration tests. The project currently has **83 test files** across **7 modules**, providing extensive coverage of business logic, service layer operations, REST controllers, and more.

### Technology Stack
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **JaCoCo** - Code coverage analysis
- **Maven Surefire** - Test execution
- **MockMvc** - Controller testing
- **Testcontainers** - Integration testing with containers

---

## Test Structure

### Test Organization by Module

```
carsharing-backend/
├── auth-service/          (16 test files)
│   └── src/test/java/com/cimhub/authservice/
│       ├── controller/AuthControllerTest.java
│       ├── exception/AuthExceptionHandlerTest.java
│       ├── security/JwtServiceTest.java
│       ├── security/TokenHasherTest.java
│       ├── security/UserDetailsBuilderTest.java
│       ├── service/AuthServiceTest.java
│       ├── service/CustomUserDetailServiceTest.java
│       ├── service/PasswordManagementServiceTest.java
│       ├── service/RegistrationServiceTest.java
│       ├── service/UserServiceClientIntegrationTest.java
│       ├── util/EmailNormalizerTest.java
│       ├── util/PasswordValidatorTest.java
│       └── testsupport/AuthFixtures.java
│
├── user-service/          (22 test files)
│   └── src/test/java/com/cimhub/userservice/
│       ├── config/OpenApiConfigTest.java
│       ├── controller/
│       │   ├── AdminControllerTest.java
│       │   ├── AdminDriverControllerTest.java
│       │   ├── AdminPassengerControllerTest.java
│       │   ├── DriverControllerTest.java
│       │   ├── PassengerControllerTest.java
│       │   ├── UserControllerTest.java
│       │   └── VehicleControllerTest.java
│       ├── service/
│       │   ├── AdminDriverServiceTest.java
│       │   ├── AdminPassengerServiceTest.java
│       │   ├── DriverServiceTest.java
│       │   ├── PassengerServiceTest.java
│       │   ├── ProfileUpdaterTest.java
│       │   └── UserServiceTest.java
│       └── vehicle/
│           ├── controller/AdminVehicleControllerTest.java
│           └── service/AdminVehicleServiceTest.java
│           └── service/VehicleServiceTest.java
│
├── trip-service/          (24 test files)
│   └── src/test/java/com/cimhub/tripservice/
│       ├── config/OSMClientConfigTest.java
│       ├── config/OpenApiConfigTest.java
│       ├── controller/
│       │   ├── AdminBookingControllerTest.java
│       │   ├── AdminTripControllerTest.java
│       │   ├── BookingControllerIntegrationTest.java
│       │   ├── BookingControllerTest.java
│       │   ├── ReportsControllerTest.java
│       │   ├── RideControllerTest.java
│       │   ├── TripControllerIntegrationTest.java
│       │   └── TripControllerTest.java
│       ├── dto/
│       │   ├── osrm/OsrmDtoRecordsTest.java
│       │   ├── request/FindRideRequestTest.java
│       │   └── response/DriverTripResponseTest.java
│       │   └── response/PassengerRideResponseTest.java
│       ├── model/GeoJsonPointTest.java
│       ├── service/
│       │   ├── booking/AdminBookingServiceTest.java
│       │   ├── booking/CancellationServiceTest.java
│       │   ├── booking/CompletionServiceTest.java
│       │   ├── booking/JoinTripServiceTest.java
│       │   ├── geo/TripGeospatialServiceTest.java
│       │   ├── notification/DriverNotificationServiceIntegrationTest.java
│       │   ├── notification/DriverNotificationServiceTest.java
│       │   ├── report/ReportsServiceTest.java
│       │   ├── route/OsrmRouteClientTest.java
│       │   ├── trip/ActiveRidesServiceTest.java
│       │   ├── trip/AdminTripServiceTest.java
│       │   ├── trip/OfferRideServiceTest.java
│       │   ├── trip/TripSearchServiceTest.java
│       │   ├── trip/TripServiceTest.java
│       │   └── util/TimezoneConversionServiceTest.java
│       └── testsupport/TripFixtures.java
│
├── review-service/        (10 test files)
│   └── src/test/java/com/cimhub/reviewservice/
│       ├── controller/
│       │   ├── AdminReviewControllerTest.java
│       │   └── ReviewControllerTest.java
│       ├── mapper/ReviewMapperTest.java
│       ├── model/ReviewStatusTest.java
│       ├── service/
│       │   ├── AdminReviewServiceTest.java
│       │   ├── ReviewServiceTest.java
│       │   └──/TripServiceClientIntegrationTest.java
│       └── testsupport/ReviewFixtures.java
│
├── notification-service/  (5 test files)
│   └── src/test/java/com/cimhub/notificationservice/
│       ├── controller/NotificationControllerTest.java
│       ├── mapper/NotificationMapperTest.java
│       ├── service/
│       │   ├── NotificationServiceTest.java
│       │   └── WebSocketNotificationPublisherTest.java
│       └── testsupport/NotificationFixtures.java
│
├── api-gateway/           (4 test files)
│   └── src/test/java/com/cimhub/apigateway/
│       ├── exception/GatewayExceptionHandlerTest.java
│       └── filter/
│           ├── AdminAuthorizationFilterTest.java
│           ├── JwtValidationFilterTest.java
│           └── RequestIdGatewayFilterTest.java
│
└── common/                (10 test files)
    └── src/test/java/com/cimhub/common/
        ├── dto/ApiResponseWrapperTest.java
        ├── dto/response/ApiErrorTest.java
        ├── dto/response/ErrorBodyAndDetailTest.java
        ├── exception/
        │   ├── BasicExceptionTest.java
        │   └── GlobalExceptionHandlerTest.java
        ├── filter/RequestIdFilterTest.java
        ├── service/AuditServiceTest.java
        └── testsupport/
            ├── ApiResponses.java
            ├── Points.java
            └── WebRequests.java
```

---

## Running Tests

### Prerequisites
- Java 21
- Maven 3.8+
- All dependencies installed

### Command Reference

#### Run All Tests in All Modules
```bash
# From project root
mvn clean test
```

#### Run Tests for a Specific Module
```bash
# Single module
mvn clean test -pl auth-service

# Multiple modules
mvn clean test -pl auth-service,user-service
```

#### Run Tests and Generate Coverage Reports
```bash
# All modules with coverage
mvn clean verify

# Single module with coverage
mvn clean verify -pl auth-service
```

#### Run a Specific Test Class
```bash
# Using Maven
mvn test -Dtest=AuthServiceTest -pl auth-service

# Run a specific test method
mvn test -Dtest=AuthServiceTest#registerUser_shouldCreateAuthUserAndProfile -pl auth-service
```

#### Run Tests in Parallel
```bash
# Run tests in parallel (faster execution)
mvn clean test -T 4
```

#### Skip Tests During Build
```bash
mvn clean install -DskipTests
```

#### Run Tests with Debug Output
```bash
mvn clean test -X -pl auth-service
```

### IDE Integration

#### IntelliJ IDEA
1. Right-click on test class/method
2. Select "Run 'TestClassName'" or "Debug 'TestClassName'"
3. Use keyboard shortcut: `Ctrl+Shift+F10` (Windows/Linux) or `Cmd+Shift+R` (Mac)

#### VS Code with Java Extension
1. Click "Run Test" or "Debug Test" above test method
2. Use Test Explorer sidebar
3. Keyboard shortcut: `Ctrl+; Ctrl+T`

---

## Code Coverage

### JaCoCo Configuration

JaCoCo is configured to generate coverage reports for all modules with the following settings:

- **Version**: Latest (Java 21 compatible)
- **Exclusions**: Mockito mocks, ByteBuddy classes, JDK internals
- **Minimum Coverage**: 0% (configurable)

### Generating Coverage Reports

#### Individual Module Coverage
```bash
# Generate coverage for a single module
mvn clean test -pl auth-service

# View report at:
# auth-service/target/site/jacoco/index.html
```

#### Aggregate Coverage Report (All Modules)
```bash
# Generate consolidated coverage report
mvn clean verify

# View aggregate report at:
# coverage-report/target/site/jacoco-aggregate/index.html
```

### Coverage Report Formats

JaCoCo generates multiple report formats:

1. **HTML Report** (`index.html`) - Interactive browser-based report
2. **XML Report** (`jacoco.xml`) - For CI/CD integration (SonarQube, Codecov)
3. **CSV Report** (`jacoco.csv`) - Data analysis

### Coverage Report Locations

```
<module>/target/site/jacoco/
├── index.html              # Main coverage report
├── jacoco.xml              # XML format (CI/CD)
├── jacoco.csv              # CSV format
├── jacoco-sessions.html    # Session details
└── com.cimhub.<module>/    # Package-level reports
```

### Viewing Coverage Reports

```bash
# Open HTML report in browser (macOS)
open auth-service/target/site/jacoco/index.html

# Open HTML report in browser (Linux)
xdg-open auth-service/target/site/jacoco/index.html

# Open aggregate report
open coverage-report/target/site/jacoco-aggregate/index.html
```

### Setting Coverage Thresholds

Edit `pom.xml` to enforce minimum coverage requirements:

```xml
<configuration>
    <rules>
        <rule>
            <element>PACKAGE</element>
            <limits>
                <limit>
                    <counter>LINE</counter>
                    <value>COVEREDRATIO</value>
                    <minimum>0.70</minimum> <!-- 70% minimum -->
                </limit>
                <limit>
                    <counter>BRANCH</counter>
                    <value>COVEREDRATIO</value>
                    <minimum>0.60</minimum> <!-- 60% minimum -->
                </limit>
            </limits>
        </rule>
    </rules>
</configuration>
```

Build will fail if coverage is below the threshold.

---

## Test Types

### Current Test Coverage

| Test Type | Coverage | Count | Status |
|-----------|----------|-------|--------|
| Unit Tests (Mockito) | ✅ 100% | 60+ files | Implemented |
| Controller Tests (@WebMvcTest) | ✅ 100% | 20+ files | Implemented |
| Integration Tests | ✅ Partial | 5+ files | Implemented |
| Mapper/Util Tests | ✅ 100% | 10+ files | Implemented |

### 1. Unit Tests (Current Implementation)

**Purpose**: Test individual components in isolation using mocks

**Pattern**:
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private AuthUserRepository authUserRepository;
    
    @Mock
    private UserServiceClient userServiceClient;
    
    @Mock
    private JwtService jwtService;
    
    private AuthService authService;
    
    @BeforeEach
    void setUp() {
        authService = new AuthService(
            authUserRepository, 
            userServiceClient, 
            jwtService
        );
    }
    
    @Test
    void registerUser_shouldCreateAuthUserAndProfile() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
            .email("john@example.com")
            .password("password123")
            .firstName("John")
            .build();
            
        // When
        when(authUserRepository.existsByEmail(anyString()))
            .thenReturn(false);
        when(authUserRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        // Then
        authService.registerUser(request);
        
        verify(authUserRepository).save(any(AuthUser.class));
        verify(userServiceClient).createProfile(any());
    }
}
```

**Coverage Areas**:
- ✅ Service layer business logic
- ✅ Exception handling
- ✅ Authorization checks
- ✅ Data validation
- ✅ Audit logging
- ✅ Gateway filters

### 2. Controller Tests (Implemented)

**Purpose**: Test REST controllers with MockMvc

**Pattern**:
```java
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AuthService authService;
    
    @Test
    void registerUser_shouldReturn201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
            .email("test@example.com")
            .password("password123")
            .firstName("Test")
            .lastName("User")
            .role("PASSENGER")
            .build();
            
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("Registration successful"));
    }
}
```

**Coverage Areas**:
- ✅ HTTP request/response handling
- ✅ Request validation
- ✅ Exception mapping
- ✅ Security integration
- ✅ Response formatting

### 3. Integration Tests (Implemented)

**Purpose**: Test complete flows with real components

**Pattern**:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TripControllerIntegrationTest {
    
    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7")
        .withDatabaseName("testdb");
    
    @Autowired
    private TripService tripService;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }
    
    @Test
    void shouldCreateAndRetrieveTrip() {
        // Test with real database
    }
}
```

**Coverage Areas**:
- ✅ Service-to-service communication
- ✅ Database operations
- ✅ Real component integration

---

## Module-Specific Tests

### Auth Service (16 test files)

**Controller Tests**
- `AuthControllerTest.java` - HTTP endpoint testing
- `AuthExceptionHandlerTest.java` - Exception handling

**Security Tests**
- `JwtServiceTest.java` - JWT token generation/validation
- `TokenHasherTest.java` - Token hashing
- `UserDetailsBuilderTest.java` - User details construction

**Service Tests**
- `AuthServiceTest.java` - Core authentication
- `RegistrationServiceTest.java` - User registration flow
- `PasswordManagementServiceTest.java` - Password reset/change
- `CustomUserDetailServiceTest.java` - User details loading

**Integration Tests**
- `UserServiceClientIntegrationTest.java` - Inter-service calls

**Utility Tests**
- `EmailNormalizerTest.java` - Email normalization
- `PasswordValidatorTest.java` - Password validation

**Test Fixtures**
- `AuthFixtures.java` - Reusable test data

**Run Auth Service Tests**:
```bash
mvn clean test -pl auth-service

# View coverage:
open auth-service/target/site/jacoco/index.html
```

---

### User Service (22 test files)

**Controller Tests**
- `UserControllerTest.java` - User profile endpoints
- `DriverControllerTest.java` - Driver profile endpoints
- `PassengerControllerTest.java` - Passenger profile endpoints
- `AdminControllerTest.java` - Admin user management
- `AdminDriverControllerTest.java` - Admin driver management
- `AdminPassengerControllerTest.java` - Admin passenger management
- `VehicleControllerTest.java` - Vehicle endpoints
- `AdminVehicleControllerTest.java` - Admin vehicle management

**Service Tests**
- `UserServiceTest.java` - User profile management
- `DriverServiceTest.java` - Driver operations
- `PassengerServiceTest.java` - Passenger operations
- `ProfileUpdaterTest.java` - Profile updates
- `AdminDriverServiceTest.java` - Admin driver operations
- `AdminPassengerServiceTest.java` - Admin passenger operations
- `AdminVehicleServiceTest.java` - Admin vehicle operations

**Vehicle Service Tests**
- `VehicleServiceTest.java` - Vehicle registration/retrieval

**Config Tests**
- `OpenApiConfigTest.java` - OpenAPI configuration

**Run User Service Tests**:
```bash
mvn clean test -pl user-service
```

---

### Trip Service (24 test files)

**Controller Tests**
- `TripControllerTest.java` - Trip CRUD operations
- `TripControllerIntegrationTest.java` - Trip integration flows
- `RideControllerTest.java` - Passenger ride management
- `BookingControllerTest.java` - Booking operations
- `BookingControllerIntegrationTest.java` - Booking integration flows
- `AdminTripControllerTest.java` - Admin trip management
- `AdminBookingControllerTest.java` - Admin booking management
- `ReportsControllerTest.java` - Reporting endpoints

**Service Tests - Booking**
- `JoinTripServiceTest.java` - Booking flows (auto-confirm, driver-accept)
- `CancellationServiceTest.java` - Cancellation logic
- `CompletionServiceTest.java` - Trip completion
- `AdminBookingServiceTest.java` - Admin booking operations

**Service Tests - Trip**
- `TripServiceTest.java` - Trip core operations
- `OfferRideServiceTest.java` - Ride offering
- `ActiveRidesServiceTest.java` - Active rides retrieval
- `TripSearchServiceTest.java` - Trip search/filtering
- `AdminTripServiceTest.java` - Admin trip operations
- `ReportsServiceTest.java` - Reporting service

**Service Tests - Geo/Route**
- `TripGeospatialServiceTest.java` - Geospatial operations
- `OsrmRouteClientTest.java` - OSRM routing client
- `TimezoneConversionServiceTest.java` - Timezone handling

**Service Tests - Notifications**
- `DriverNotificationServiceTest.java` - Driver notifications
- `DriverNotificationServiceIntegrationTest.java` - Notification integration

**DTO Tests**
- `FindRideRequestTest.java` - Request validation
- `DriverTripResponseTest.java` - Response mapping
- `PassengerRideResponseTest.java` - Response mapping
- `OsrmDtoRecordsTest.java` - OSRM DTO mapping

**Model Tests**
- `GeoJsonPointTest.java` - GeoJSON point operations

**Config Tests**
- `OpenApiConfigTest.java` - OpenAPI configuration
- `OSMClientConfigTest.java` - OSM client configuration

**Test Fixtures**
- `TripFixtures.java` - Reusable test data

**Run Trip Service Tests**:
```bash
mvn clean test -pl trip-service
```

---

### Review Service (10 test files)

**Controller Tests**
- `ReviewControllerTest.java` - Review endpoints
- `AdminReviewControllerTest.java` - Admin review management

**Service Tests**
- `ReviewServiceTest.java` - Review operations
- `AdminReviewServiceTest.java` - Admin review operations
- `TripServiceClientIntegrationTest.java` - Inter-service integration

**Mapper Tests**
- `ReviewMapperTest.java` - DTO mapping

**Model Tests**
- `ReviewStatusTest.java` - Status enum handling

**Test Fixtures**
- `ReviewFixtures.java` - Reusable test data

**Run Review Service Tests**:
```bash
mvn clean test -pl review-service
```

---

### Notification Service (5 test files)

**Controller Tests**
- `NotificationControllerTest.java` - Notification endpoints

**Service Tests**
- `NotificationServiceTest.java` - Notification operations
- `WebSocketNotificationPublisherTest.java` - WebSocket publishing

**Mapper Tests**
- `NotificationMapperTest.java` - DTO mapping

**Test Fixtures**
- `NotificationFixtures.java` - Reusable test data

**Run Notification Service Tests**:
```bash
mvn clean test -pl notification-service
```

---

### API Gateway (4 test files)

**Filter Tests**
- `JwtValidationFilterTest.java` - JWT validation
  - ✅ Public path bypass with header sanitization
  - ✅ Authorization header validation
  - ✅ Malformed token handling
  - ✅ Auth service failure mapping
  
- `AdminAuthorizationFilterTest.java` - Admin protection
  - ✅ Admin-only path enforcement
  - ✅ Role validation
  - ✅ Non-admin rejection (403 FORBIDDEN)
  
- `RequestIdGatewayFilterTest.java` - Request tracking

**Exception Tests**
- `GatewayExceptionHandlerTest.java` - Error handling

**Run API Gateway Tests**:
```bash
mvn clean test -pl api-gateway
```

---

### Common Module (10 test files)

**DTO Tests**
- `ApiResponseWrapperTest.java` - Response wrapper
- `ApiErrorTest.java` - Error response
- `ErrorBodyAndDetailTest.java` - Error body/details

**Exception Tests**
- `BasicExceptionTest.java` - Base exception
- `GlobalExceptionHandlerTest.java` - Global exception handling

**Filter Tests**
- `RequestIdFilterTest.java` - Request ID propagation

**Service Tests**
- `AuditServiceTest.java` - Audit logging

**Test Support**
- `ApiResponses.java` - Response helpers
- `Points.java` - Test utilities
- `WebRequests.java` - Web request helpers

**Run Common Tests**:
```bash
mvn clean test -pl common
```

---

## Writing New Tests

### Unit Test Template

```java
package com.cimhub.yourservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class YourServiceTest {
    
    @Mock
    private YourRepository repository;
    
    @Mock
    private AuditService auditService;
    
    private YourService service;
    
    @BeforeEach
    void setUp() {
        service = new YourService(repository, auditService);
    }
    
    @Test
    void methodName_shouldBehavior_whenCondition() {
        // Given (Arrange)
        YourEntity entity = new YourEntity();
        entity.setId("123");
        when(repository.findById("123")).thenReturn(Optional.of(entity));
        
        // When (Act)
        YourDTO result = service.getById("123");
        
        // Then (Assert)
        assertNotNull(result);
        assertEquals("123", result.getId());
        verify(repository).findById("123");
    }
    
    @Test
    void methodName_shouldThrowException_whenInvalid() {
        // Given
        when(repository.findById("invalid")).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(YourException.class, () -> {
            service.getById("invalid");
        });
    }
}
```

### Controller Test Template

```java
@WebMvcTest(YourController.class)
class YourControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private YourService yourService;
    
    @Test
    void getById_shouldReturn200_whenFound() throws Exception {
        YourDTO dto = new YourDTO();
        dto.setId("123");
        when(yourService.getById("123")).thenReturn(dto);
        
        mockMvc.perform(get("/api/v1/yours/123")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value("123"));
    }
}
```

### Test Naming Convention

Use descriptive names following this pattern:
```
methodName_shouldExpectedBehavior_whenSpecificCondition
```

**Examples**:
- `registerUser_shouldCreateAuthUserAndProfile_whenValidRequest`
- `joinTrip_shouldThrowException_whenNotEnoughSeats`
- `markAsRead_shouldThrowUnauthorized_whenDifferentRecipient`

### Best Practices

1. **Isolation**: Each test should be independent
2. **Arrange-Act-Assert**: Follow AAA pattern
3. **One Assertion Per Test**: Test one thing at a time
4. **Mock External Dependencies**: Don't test third-party code
5. **Use Descriptive Names**: Make test purpose clear
6. **Test Edge Cases**: Not just happy paths
7. **Verify Interactions**: Use `verify()` for important calls
8. **Clean Up**: Use `@BeforeEach` for setup

---

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Test and Coverage

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
        cache: maven
    
    - name: Run tests with coverage
      run: mvn clean verify
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v4
      with:
        files: ./coverage-report/target/site/jacoco-aggregate/jacoco.xml
        flags: unittests
        name: codecov-umbrella
    
    - name: Archive coverage reports
      uses: actions/upload-artifact@v4
      with:
        name: coverage-reports
        path: |
          **/target/site/jacoco/
          coverage-report/target/site/jacoco-aggregate/
```

### GitLab CI Example

```yaml
test:
  stage: test
  image: maven:3.9-eclipse-temurin-21
  script:
    - mvn clean verify
  coverage: '/Total.*?([0-9]{1,3})%/'
  artifacts:
    when: always
    reports:
      junit:
        - '**/target/surefire-reports/TEST-*.xml'
      coverage_report:
        coverage_format: jacoco
        path: coverage-report/target/site/jacoco-aggregate/jacoco.xml
    paths:
      - '**/target/site/jacoco/'
      - coverage-report/target/site/jacoco-aggregate/
```

### SonarQube Integration

```bash
# Run tests with coverage and send to SonarQube
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=carsharing-backend \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=your-token \
  -Dsonar.coverage.jacoco.xmlReportPaths=coverage-report/target/site/jacoco-aggregate/jacoco.xml
```

---

## Troubleshooting

### Common Issues

#### 1. Tests Not Running
```bash
# Check if test classes are named correctly (*Test.java)
# Verify JUnit 5 is in classpath
mvn dependency:tree | grep junit
```

#### 2. Coverage Report Not Generated
```bash
# Ensure tests are running
mvn clean test -X

# Check jacoco.exec file exists
ls -la target/jacoco.exec
```

#### 3. Mockito Issues with Java 21
```bash
# Verify correct JaCoCo version (0.8.12+)
# Add to maven-surefire-plugin:
<argLine>@{argLine} -Dnet.bytebuddy.experimental=true</argLine>
```

#### 4. Compilation Errors in Tests
```bash
# Build common module first
mvn clean install -pl common -DskipTests

# Then run tests
mvn clean test -pl auth-service
```

### Getting Help

- Check test logs: `mvn clean test -X`
- Review Surefire reports: `target/surefire-reports/`
- Verify dependencies: `mvn dependency:tree`

---

## Future Improvements

### Recommended Additions

1. **Contract Tests**
   - Add Spring Cloud Contract
   - Test inter-service communication
   - Validate API contracts

2. **Performance Tests**
   - Add JMeter tests
   - Load testing scenarios
   - Stress testing

3. **Security Tests**
   - Authentication flow tests
   - Authorization tests
   - Input validation tests

---

## Summary

### Quick Reference

| Task | Command |
|------|---------|
| Run all tests | `mvn clean test` |
| Run specific module | `mvn clean test -pl auth-service` |
| Generate coverage | `mvn clean verify` |
| View coverage | `open coverage-report/target/site/jacoco-aggregate/index.html` |
| Run specific test | `mvn test -Dtest=AuthServiceTest` |
| Skip tests | `mvn clean install -DskipTests` |

### Current State
- ✅ 83 test files across all modules
- ✅ Comprehensive service layer coverage (unit tests)
- ✅ Comprehensive controller layer coverage (@WebMvcTest)
- ✅ Integration tests for critical flows
- ✅ Mapper and utility tests
- ✅ JaCoCo code coverage configured
- ✅ Mockito and MockMvc best practices

### Next Steps
1. Add more contract tests for inter-service communication
2. Add performance/load testing
3. Add security-focused integration tests
4. Consider adding mutation testing for test quality