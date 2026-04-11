# Testing Quick Reference

## Run Tests

### All Modules
```bash
# Run all tests
mvn clean test

# Run all tests with coverage
mvn clean verify

# Run tests in parallel (4 threads)
mvn clean test -T 4
```

### Specific Module
```bash
# Single module
mvn clean test -pl auth-service
mvn clean test -pl user-service
mvn clean test -pl trip-service
mvn clean test -pl review-service
mvn clean test -pl notification-service
mvn clean test -pl api-gateway

# Multiple modules
mvn clean test -pl auth-service,trip-service
```

### Specific Test Class
```bash
# Run specific test class
mvn test -Dtest=AuthServiceTest -pl auth-service

# Run specific test method
mvn test -Dtest=AuthServiceTest#registerUser_shouldCreateAuthUserAndProfile -pl auth-service

# Run all tests matching pattern
mvn test -Dtest=*ServiceTest -pl auth-service
```

## Code Coverage

### Generate Coverage Reports
```bash
# Individual module
mvn clean test -pl auth-service

# Aggregate report (all modules)
mvn clean verify
```

### View Coverage Reports
```bash
# Individual module (macOS)
open auth-service/target/site/jacoco/index.html

# Individual module (Linux)
xdg-open auth-service/target/site/jacoco/index.html

# Aggregate report
open coverage-report/target/site/jacoco-aggregate/index.html
```

### Coverage Report Locations
```
# Individual module reports
<module>/target/site/jacoco/index.html

# Aggregate report (all modules)
coverage-report/target/site/jacoco-aggregate/index.html

# XML format (for CI/CD)
<module>/target/site/jacoco/jacoco.xml
coverage-report/target/site/jacoco-aggregate/jacoco.xml
```

## Test Output

### View Test Results
```bash
# Surefire reports (detailed)
cat auth-service/target/surefire-reports/*.txt

# Test summary
grep "Tests run:" auth-service/target/surefire-reports/*.txt
```

### Debug Mode
```bash
# Run with debug output
mvn clean test -X -pl auth-service

# Run with more logging
mvn clean test -Dtest=AuthServiceTest -Dlogging.level.root=DEBUG -pl auth-service
```

## Skip Tests

```bash
# Skip all tests during build
mvn clean install -DskipTests

# Skip only test execution (still compile tests)
mvn clean install -Dmaven.test.skip=false -DskipTests

# Skip test compilation and execution
mvn clean install -Dmaven.test.skip=true
```

## Current Test Count

| Module | Test Files | Status |
|--------|-----------|--------|
| auth-service | 16 | ✅ Working |
| user-service | 22 | ✅ Working |
| trip-service | 24 | ✅ Working |
| review-service | 10 | ✅ Working |
| notification-service | 5 | ✅ Working |
| api-gateway | 4 | ✅ Working |
| common | 8 | ✅ Working |
| **TOTAL** | **89** | |

## Coverage Thresholds

To enforce minimum coverage, edit `pom.xml`:

```xml
<configuration>
    <rules>
        <rule>
            <element>PACKAGE</element>
            <limits>
                <limit>
                    <counter>LINE</counter>
                    <value>COVEREDRATIO</value>
                    <minimum>0.70</minimum> <!-- 70% line coverage -->
                </limit>
                <limit>
                    <counter>BRANCH</counter>
                    <value>COVEREDRATIO</value>
                    <minimum>0.60</minimum> <!-- 60% branch coverage -->
                </limit>
            </limits>
        </rule>
    </rules>
</configuration>
```

## CI/CD

### GitHub Actions
```bash
# Trigger workflow
git push origin main

# View results
# Check: https://github.com/<user>/<repo>/actions
```

### Local CI Simulation
```bash
# Run same commands as CI
mvn clean verify
mvn clean install -DskipTests
```

## Common Issues

### Issue: Tests not found
```bash
# Ensure test files end with Test.java
find . -name "*Test.java" -type f
```

### Issue: Coverage report missing
```bash
# Verify jacoco.exec exists
ls -la target/jacoco.exec

# Re-run with clean
mvn clean test
```

### Issue: OutOfMemoryError
```bash
# Increase memory
export MAVEN_OPTS="-Xmx2048m"
mvn clean test
```

## Documentation

For detailed documentation, see [TESTING.md](testing.md)
