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

The Car Sharing Backend project uses a comprehensive testing strategy focused on unit testing with Mockito. The project currently has **20 test files** across **7 modules**, providing extensive coverage of business logic and service layer operations.

### Technology Stack
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **JaCoCo 0.8.12** - Code coverage analysis
- **Maven Surefire** - Test execution

---

## Test Structure

### Test Organization by Module

```
carsharing-backend/
├── auth-service/          (3 test files)
│   └── src/test/java/com/cimhub/authservice/
│       ├── security/JwtServiceTest.java
│       ├── service/AuthServiceTest.java
│       └── service/AuthServiceResetPasswordTest.java
│
├── user-service/          (5 test files)
│   └── src/test/java/com/cimhub/userservice/
│       ├── service/UserServiceTest.java
│       ├── service/AdminDriverServiceTest.java
│       ├── service/AdminPassengerServiceTest.java
│       ├── vehicle/service/VehicleServiceTest.java
│       └── vehicle/service/AdminVehicleServiceTest.java
│
├── trip-service/          (4 test files)
│   └── src/test/java/com/cimhub/tripservice/service/
│       ├── JoinTripServiceTest.java
│       ├── CancellationServiceTest.java
│       ├── AdminTripServiceTest.java
│       └── AdminBookingServiceTest.java
│
├── review-service/        (2 test files)
│   └── src/test/java/com/cimhub/reviewservice/service/
│       ├── ReviewServiceTest.java
│       └── AdminReviewServiceTest.java
│
├── notification-service/  (1 test file)
│   └── src/test/java/com/cimhub/notificationservice/service/
│       └── NotificationServiceTest.java
│
├── api-gateway/           (4 test files)
│   └── src/test/java/com/cimhub/apigateway/
│       ├── filter/JwtValidationFilterTest.java
│       ├── filter/AdminAuthorizationFilterTest.java
│       ├── filter/RequestIdGatewayFilterTest.java
│       └── exception/GatewayExceptionHandlerTest.java
│
└── common/                (1 test file)
    └── src/test/java/com/cimhub/common/util/
        └── AdminGuardTest.java
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

- **Version**: 0.8.12 (Java 21 compatible)
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
| Unit Tests (Mockito) | ✅ 100% | 20 files | Implemented |
| Integration Tests | ❌ 0% | 0 files | Not Implemented |
| HTTP/API Tests | ❌ 0% | 0 files | Not Implemented |
| Database Tests | ❌ 0% | 0 files | Not Implemented |

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

### 2. Integration Tests (Recommended - Not Implemented)

**Purpose**: Test complete flows with real components

**Recommended Pattern**:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private AuthUserRepository authUserRepository;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Test
    void shouldRegisterUserEndToEnd() {
        // Test with real database
        RegisterRequest request = RegisterRequest.builder()
            .email("integration@test.com")
            .password("password123")
            .build();
            
        authService.registerUser(request);
        
        assertTrue(authUserRepository.existsByEmail("integration@test.com"));
    }
}
```

**Dependencies Needed**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

### 3. HTTP/API Tests (Recommended - Not Implemented)

**Purpose**: Test REST endpoints with HTTP requests

**Recommended Pattern**:
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

### 4. Database/Repository Tests (Recommended - Not Implemented)

**Purpose**: Test JPA repositories with actual database

**Recommended Pattern**:
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class AuthUserRepositoryTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Autowired
    private AuthUserRepository repository;
    
    @Test
    void findByEmail_shouldReturnUser() {
        AuthUser user = new AuthUser();
        user.setEmail("test@example.com");
        user.setPasswordHash("hash");
        repository.save(user);
        
        Optional<AuthUser> found = repository.findByEmail("test@example.com");
        
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }
}
```

---

## Module-Specific Tests

### Auth Service (3 test files, 26 tests)

**AuthServiceTest.java** - Core authentication
- ✅ User registration with email normalization
- ✅ Login with credential validation
- ✅ Password change
- ✅ Token validation (valid/expired/malformed)
- ✅ Profile creation rollback on failure

**AuthServiceResetPasswordTest.java** - Password reset
- ✅ Initiate password reset
- ✅ Confirm password reset with token validation
- ✅ Token expiry handling
- ✅ Invalid token rejection

**JwtServiceTest.java** - JWT operations
- ✅ Token generation with roles
- ✅ Token validation
- ✅ Username extraction
- ✅ Role extraction

**Run Auth Service Tests**:
```bash
mvn clean test -pl auth-service

# View coverage:
open auth-service/target/site/jacoco/index.html
```

---

### User Service (5 test files)

**UserServiceTest.java** - Profile management
- ✅ Create driver/passenger profiles
- ✅ Retrieve user information
- ✅ Check user existence

**VehicleServiceTest.java** - Vehicle operations
- ✅ Register vehicle with plate normalization
- ✅ Duplicate plate checking
- ✅ Retrieve user vehicles

**AdminDriverServiceTest.java** - Admin driver management
**AdminPassengerServiceTest.java** - Admin passenger management
**AdminVehicleServiceTest.java** - Admin vehicle management

**Run User Service Tests**:
```bash
mvn clean test -pl user-service

# Note: Current compilation errors need fixing before tests can run
# Requires missing common module classes
```

---

### Trip Service (4 test files)

**JoinTripServiceTest.java** - Booking flows (479 lines)
- ✅ Auto-confirm booking (auto_confirm=true)
- ✅ Driver-accept booking (auto_confirm=false)
- ✅ Seat availability validation
- ✅ Duplicate booking prevention
- ✅ Accept/reject booking operations

**CancellationServiceTest.java** - Cancellation
- ✅ Passenger cancels booking
- ✅ Driver cancels trip (cascade to bookings)
- ✅ Seat release on cancellation
- ✅ Authorization checks

**AdminTripServiceTest.java** - Admin trip operations
**AdminBookingServiceTest.java** - Admin booking operations

**Run Trip Service Tests**:
```bash
mvn clean test -pl trip-service
```

---

### Review Service (2 test files)

**ReviewServiceTest.java** - Review management
- ✅ Submit driver reviews (by passengers)
- ✅ Submit passenger reviews (by drivers)
- ✅ Authorization validation
- ✅ Average rating calculation
- ✅ Duplicate review prevention
- ✅ Trip completion validation

**AdminReviewServiceTest.java** - Admin review operations

**Run Review Service Tests**:
```bash
mvn clean test -pl review-service
```

---

### Notification Service (1 test file, 9 tests)

**NotificationServiceTest.java**
- ✅ Create notification with realtime publishing
- ✅ Mark as read (single and bulk)
- ✅ Unread count tracking
- ✅ Recipient authorization
- ✅ Sender/recipient validation

**Run Notification Service Tests**:
```bash
mvn clean test -pl notification-service
```

---

### API Gateway (4 test files)

**JwtValidationFilterTest.java** - JWT validation
- ✅ Public path bypass with header sanitization
- ✅ Authorization header validation
- ✅ Malformed token handling
- ✅ Auth service failure mapping

**AdminAuthorizationFilterTest.java** - Admin protection
- ✅ Admin-only path enforcement
- ✅ Role validation
- ✅ Non-admin rejection (403 FORBIDDEN)

**RequestIdGatewayFilterTest.java** - Request tracking
**GatewayExceptionHandlerTest.java** - Error handling

**Run API Gateway Tests**:
```bash
mvn clean test -pl api-gateway
```

---

### Common Module (1 test file)

**AdminGuardTest.java** - Authorization utility
- ✅ ADMIN role validation (case-insensitive)
- ✅ Non-admin rejection
- ✅ Null/empty role handling

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
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
        cache: maven
    
    - name: Run tests with coverage
      run: mvn clean verify
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        files: ./coverage-report/target/site/jacoco-aggregate/jacoco.xml
        flags: unittests
        name: codecov-umbrella
    
    - name: Archive coverage reports
      uses: actions/upload-artifact@v3
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

1. **Integration Tests**
   - Add `@SpringBootTest` tests
   - Use Testcontainers for databases
   - Test complete flows end-to-end

2. **HTTP/API Tests**
   - Add `@WebMvcTest` for controllers
   - Test REST endpoints
   - Validate request/response formats

3. **Database Tests**
   - Add `@DataJpaTest` for repositories
   - Test custom queries
   - Validate constraints

4. **Test Configuration**
   - Create `application-test.yml`
   - Add test-specific properties
   - Configure in-memory databases

5. **Contract Tests**
   - Add Spring Cloud Contract
   - Test inter-service communication
   - Validate API contracts

6. **Performance Tests**
   - Add JMeter tests
   - Load testing scenarios
   - Stress testing

7. **Security Tests**
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
- ✅ 20 unit test files
- ✅ Comprehensive service layer coverage
- ✅ JaCoCo code coverage configured
- ✅ Mockito best practices
- ❌ No integration tests
- ❌ No HTTP/API tests
- ❌ No database tests

### Next Steps
1. Fix compilation errors in user-service and notification-service
2. Add integration tests with Testcontainers
3. Add HTTP/API tests with `@WebMvcTest`
4. Create test configuration files
5. Add CI/CD pipeline with coverage reporting
