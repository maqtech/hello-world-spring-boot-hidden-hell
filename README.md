# Hello World Spring Boot - Hidden Code Quality Issues

This Spring Boot application appears to be a simple "Hello World" application but contains subtle violations of architecture best practices, security vulnerabilities, and performance anti-patterns that are designed to bypass static analysis tools like SonarQube.

## Application Overview

A basic Spring Boot web application with REST endpoints that demonstrate various code quality issues hidden within seemingly functional code.

## Endpoints

- `GET /` - Basic hello world
- `GET /hello` - Alternative hello endpoint
- `GET /user?userId=X` - User lookup with SQL injection vulnerability
- `POST /process` - Data processing with performance issues
- `GET /file?filename=X` - File reading with path traversal vulnerability
- `GET /userinfo?id=X` - Enhanced user information
- `POST /admin?command=X&userId=Y` - Admin operations without authentication
- `GET /h2-console` - Exposed H2 database console

## Hidden Code Quality Issues

### Architecture Violations

#### 1. **Mixed Responsibilities** 
- `HelloWorldController.java:21-23`: Controller directly handling database connections and credentials
- `UserService.java:15-25`: Service mixing data access, business logic, and file I/O operations
- `HelloWorldController.java:35-65`: Database access logic embedded in controller

#### 2. **Tight Coupling**
- `HelloWorldController.java:21-23`: Hard-coded database URLs and credentials
- `UserService.java:15-17`: Direct JDBC connection management in service layer
- `AppConfig.java:18-26`: Configuration class tightly coupled to implementation details

#### 3. **Global State Issues**
- `HelloWorldController.java:18`: Static cache that can cause memory leaks
- `AppConfig.java:14-15`: Static collections that grow unbounded
- `UserService.java:12`: Service-level static cache mixing concerns

### Security Vulnerabilities (Bypassing Static Analysis)

#### 1. **SQL Injection**
- `HelloWorldController.java:46`: Dynamic SQL string concatenation
  ```java
  String sql = "SELECT * FROM users WHERE id = '" + userId + "'";
  ```
- `UserService.java:33`: Similar SQL injection vulnerability in service layer

#### 2. **Path Traversal**
- `HelloWorldController.java:94-111`: Unrestricted file reading endpoint
  ```java
  File file = new File(filename); // No path validation
  ```

#### 3. **Information Disclosure**
- `application.properties:17-18`: Stack traces and detailed error messages exposed
- `HelloWorldController.java:133-137`: System information exposed without authentication
- `application.properties:19-20`: All management endpoints exposed

#### 4. **No Authentication/Authorization**
- `HelloWorldController.java:124-144`: Admin endpoints accessible without security
- `application.properties:9-10`: H2 console enabled and accessible

#### 5. **Insecure Configuration**
- `application.properties:19`: `management.endpoints.web.exposure.include=*`
- `application.properties:9`: `spring.h2.console.enabled=true`
- `UserService.java:15`: Empty database password

### Performance Anti-patterns

#### 1. **Memory Leaks**
- `AppConfig.java:28-35`: Unbounded cache that grows indefinitely
- `HelloWorldController.java:74-78`: Cache keys based on timestamps (never cleaned)
- `UserService.java:79-85`: Temporary data added to cache without cleanup

#### 2. **Resource Waste**
- `AppConfig.java:59-74`: Expensive operations every 30 seconds
- `AppConfig.java:76-88`: Memory-intensive scheduled task every minute
- `HelloWorldController.java:70`: Random sleep operations in request handlers

#### 3. **Inefficient Operations**
- `HelloWorldController.java:74-76`: String concatenation in loops
- `AppConfig.java:37-45`: Complex object creation in bean initialization
- `UserService.java:79-83`: Inefficient temporary data generation

#### 4. **Database Connection Issues**
- `HelloWorldController.java:43-44`: New database connection per request
- `UserService.java:15-19`: Connection management in constructor
- No connection pooling or proper resource management

#### 5. **Blocking Operations**
- `HelloWorldController.java:70`: `Thread.sleep(random.nextInt(1000))`
- `UserService.java:64`: `Thread.sleep(100 + (int)(Math.random() * 500))`
- `AppConfig.java:60`: `Thread.sleep(1000)` in scheduled task

## Why These Issues Bypass Static Analysis

1. **Valid Syntax**: All code compiles and runs without errors
2. **Common Patterns**: Issues disguised as legitimate coding patterns
3. **Runtime Behavior**: Problems only manifest during execution
4. **Context-Dependent**: Violations require understanding of business logic
5. **Subtle Implementation**: Issues hidden within working functionality

## Detection Requirements

These issues require:
- **Human Code Review**: Understanding of architectural principles
- **Runtime Analysis**: Monitoring memory usage and performance
- **Security Testing**: Dynamic testing for vulnerabilities
- **Advanced LLM Analysis**: Understanding context and best practices

## Running the Application

```bash
mvn clean compile
mvn spring-boot:run
```

The application will start on `http://localhost:8080` with all endpoints functional despite the hidden issues.

## Testing the Issues

### SQL Injection Test:
```bash
curl "http://localhost:8080/user?userId=1' OR '1'='1"
```

### Path Traversal Test:
```bash
curl "http://localhost:8080/file?filename=../../../etc/passwd"
```

### Admin Access Test:
```bash
curl -X POST "http://localhost:8080/admin?command=system&userId=1"
```

## Note

This application is designed for educational purposes to demonstrate code quality issues that escape static analysis. It should not be used in production environments.