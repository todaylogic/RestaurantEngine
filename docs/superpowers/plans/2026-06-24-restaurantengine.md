# RestaurantEngine Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a production-quality Spring Boot 3.x REST API for restaurant management covering staff, menu, tables, and orders with JWT auth and role-based access control.

**Architecture:** Layered monolith (Controller → Service → Repository), package-by-feature under `com.langko.restaurantengine`. Each domain owns its entities, DTOs, repository, service, and controller. Cross-cutting concerns (security, exceptions) live in `config/` and `exception/`.

**Tech Stack:** Spring Boot 3.x · Java 17 · MySQL 8 · Spring Data JPA · Spring Security 6 · JJWT 0.12.x · Lombok · Jakarta Bean Validation · Maven

## Global Constraints

- Java 17, Spring Boot 3.3.x
- Base package: `com.langko.restaurantengine`
- All endpoints prefixed `/api`
- Response envelope: `{ "success": true/false, "data": {...}, "message": "OK" }` / `{ "success": false, "error": "...", "status": 404 }`
- Sensitive config via env vars: `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`
- JWT: HS256, 24h expiry (`jwt.expiration=86400000`)
- BCrypt for password hashing
- Tests use H2 in-memory via `application-test.properties`

---

### Task 1: Project Bootstrap & Configuration

**Files:**
- Create: `pom.xml`
- Create: `src/main/java/com/langko/restaurantengine/RestaurantEngineApplication.java`
- Create: `src/main/resources/application.properties`
- Create: `src/test/resources/application-test.properties`

**Interfaces:**
- Produces: runnable Spring Boot app skeleton all other tasks build on

- [ ] **Step 1: Create project directory**

```
RestaurantEngine/
├── src/
│   ├── main/
│   │   ├── java/com/langko/restaurantengine/
│   │   └── resources/
│   └── test/
│       ├── java/com/langko/restaurantengine/
│       └── resources/
└── pom.xml
```

Run:
```bash
mkdir -p src/main/java/com/langko/restaurantengine
mkdir -p src/main/resources
mkdir -p src/test/java/com/langko/restaurantengine
mkdir -p src/test/resources
```

- [ ] **Step 2: Create pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>com.langko</groupId>
    <artifactId>restaurant-engine</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>RestaurantEngine</name>

    <properties>
        <java.version>17</java.version>
        <jjwt.version>0.12.6</jjwt.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: Create main application class**

`src/main/java/com/langko/restaurantengine/RestaurantEngineApplication.java`:
```java
package com.langko.restaurantengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestaurantEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestaurantEngineApplication.class, args);
    }
}
```

- [ ] **Step 4: Create application.properties**

`src/main/resources/application.properties`:
```properties
spring.application.name=RestaurantEngine
spring.datasource.url=jdbc:mysql://localhost:3306/restaurant_engine
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000
```

- [ ] **Step 5: Create test application.properties (H2)**

`src/test/resources/application-test.properties`:
```properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
jwt.secret=testSecretKeyForTestingPurposesOnly1234567890ABCDEF
jwt.expiration=86400000
```

- [ ] **Step 6: Create MySQL database**

Run in MySQL:
```sql
CREATE DATABASE IF NOT EXISTS restaurant_engine CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

- [ ] **Step 7: Set environment variables and verify build**

```bash
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
export JWT_SECRET=your256bitSecretKeyForJWTSigningPurposes123456
mvn clean compile
```
Expected: `BUILD SUCCESS`

- [ ] **Step 8: Commit**

```bash
git init
git add pom.xml src/
git commit -m "chore: bootstrap RestaurantEngine Spring Boot project"
```

---

### Task 2: Response Envelope & Exception Handling

**Files:**
- Create: `src/main/java/com/langko/restaurantengine/exception/ResourceNotFoundException.java`
- Create: `src/main/java/com/langko/restaurantengine/exception/GlobalExceptionHandler.java`
- Create: `src/main/java/com/langko/restaurantengine/common/ApiResponse.java`
- Test: `src/test/java/com/langko/restaurantengine/exception/GlobalExceptionHandlerTest.java`

**Interfaces:**
- Produces:
  - `ApiResponse.success(data)` → `ApiResponse<T>`
  - `ApiResponse.error(message, status)` → `ApiResponse<Void>`
  - `ResourceNotFoundException(String message)` → `RuntimeException`
  - `GlobalExceptionHandler` handles: `ResourceNotFoundException`, `MethodArgumentNotValidException`, `AccessDeniedException`, `AuthenticationException`, `DataIntegrityViolationException`, `Exception`

- [ ] **Step 1: Create ApiResponse wrapper**

`src/main/java/com/langko/restaurantengine/common/ApiResponse.java`:
```java
package com.langko.restaurantengine.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String message;
    private final String error;
    private final Integer status;

    private ApiResponse(boolean success, T data, String message, String error, Integer status) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.error = error;
        this.status = status;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, "OK", null, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null, null);
    }

    public static ApiResponse<Void> error(String error, int status) {
        return new ApiResponse<>(false, null, null, error, status);
    }
}
```

- [ ] **Step 2: Create ResourceNotFoundException**

`src/main/java/com/langko/restaurantengine/exception/ResourceNotFoundException.java`:
```java
package com.langko.restaurantengine.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

- [ ] **Step 3: Create GlobalExceptionHandler**

`src/main/java/com/langko/restaurantengine/exception/GlobalExceptionHandler.java`:
```java
package com.langko.restaurantengine.exception;

import com.langko.restaurantengine.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), 404));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errors, 400));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied", 403));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuth(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Unauthorized", 401));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Resource already exists", 409));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error", 500));
    }
}
```

- [ ] **Step 4: Write test**

`src/test/java/com/langko/restaurantengine/exception/GlobalExceptionHandlerTest.java`:
```java
package com.langko.restaurantengine.exception;

import com.langko.restaurantengine.common.ApiResponse;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_returns404() {
        var response = handler.handleNotFound(new ResourceNotFoundException("Staff not found"));
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getError()).isEqualTo("Staff not found");
    }

    @Test
    void handleConflict_returns409() {
        var response = handler.handleConflict(
            new org.springframework.dao.DataIntegrityViolationException("dup"));
        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void handleGeneral_returns500() {
        var response = handler.handleGeneral(new RuntimeException("boom"));
        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }
}
```

- [ ] **Step 5: Run tests**

```bash
mvn test -Dtest=GlobalExceptionHandlerTest -Dspring.profiles.active=test
```
Expected: 3 tests pass.

- [ ] **Step 6: Commit**

```bash
git add src/
git commit -m "feat: add ApiResponse envelope and global exception handler"
```

---

### Task 3: Staff Entity & Security Infrastructure

**Files:**
- Create: `src/main/java/com/langko/restaurantengine/staff/Role.java`
- Create: `src/main/java/com/langko/restaurantengine/staff/Staff.java`
- Create: `src/main/java/com/langko/restaurantengine/staff/StaffRepository.java`
- Create: `src/main/java/com/langko/restaurantengine/auth/JwtUtil.java`
- Create: `src/main/java/com/langko/restaurantengine/auth/JwtAuthFilter.java`
- Create: `src/main/java/com/langko/restaurantengine/config/SecurityConfig.java`
- Test: `src/test/java/com/langko/restaurantengine/auth/JwtUtilTest.java`

**Interfaces:**
- Consumes: nothing from earlier tasks
- Produces:
  - `Role` enum: `ADMIN`, `MANAGER`, `STAFF`
  - `Staff` entity implementing `UserDetails`; fields: `id`, `firstName`, `lastName`, `email`, `password`, `role`, `phone`, `createdAt`
  - `StaffRepository extends JpaRepository<Staff, Long>` with `Optional<Staff> findByEmail(String email)`
  - `JwtUtil.generateToken(Staff staff)` → `String`
  - `JwtUtil.extractEmail(String token)` → `String`
  - `JwtUtil.isTokenValid(String token, UserDetails userDetails)` → `boolean`
  - `JwtAuthFilter extends OncePerRequestFilter`
  - `SecurityConfig` — beans: `PasswordEncoder`, `AuthenticationManager`, `SecurityFilterChain`

- [ ] **Step 1: Create Role enum**

`src/main/java/com/langko/restaurantengine/staff/Role.java`:
```java
package com.langko.restaurantengine.staff;

public enum Role {
    ADMIN, MANAGER, STAFF
}
```

- [ ] **Step 2: Create Staff entity**

`src/main/java/com/langko/restaurantengine/staff/Staff.java`:
```java
package com.langko.restaurantengine.staff;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "staff")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Staff implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String phone;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
```

- [ ] **Step 3: Create StaffRepository**

`src/main/java/com/langko/restaurantengine/staff/StaffRepository.java`:
```java
package com.langko.restaurantengine.staff;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByEmail(String email);
    boolean existsByEmail(String email);
    long count();
}
```

- [ ] **Step 4: Create JwtUtil**

`src/main/java/com/langko/restaurantengine/auth/JwtUtil.java`:
```java
package com.langko.restaurantengine.auth;

import com.langko.restaurantengine.staff.Staff;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Staff staff) {
        return Jwts.builder()
                .subject(staff.getEmail())
                .claim("role", staff.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String email = extractEmail(token);
            return email.equals(userDetails.getUsername()) && !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

- [ ] **Step 5: Create JwtAuthFilter**

`src/main/java/com/langko/restaurantengine/auth/JwtAuthFilter.java`:
```java
package com.langko.restaurantengine.auth;

import com.langko.restaurantengine.staff.StaffRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final StaffRepository staffRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            staffRepository.findByEmail(email).ifPresent(staff -> {
                if (jwtUtil.isTokenValid(token, staff)) {
                    var auth = new UsernamePasswordAuthenticationToken(
                            staff, null, staff.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            });
        }
        filterChain.doFilter(request, response);
    }
}
```

- [ ] **Step 6: Create SecurityConfig**

`src/main/java/com/langko/restaurantengine/config/SecurityConfig.java`:
```java
package com.langko.restaurantengine.config;

import com.langko.restaurantengine.auth.JwtAuthFilter;
import com.langko.restaurantengine.staff.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final StaffRepository staffRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> staffRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Staff not found: " + email));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("GET", "/api/menu/**").permitAll()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

- [ ] **Step 7: Write JwtUtil test**

`src/test/java/com/langko/restaurantengine/auth/JwtUtilTest.java`:
```java
package com.langko.restaurantengine.auth;

import com.langko.restaurantengine.staff.Role;
import com.langko.restaurantengine.staff.Staff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private Staff staff;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
            "testSecretKeyForTestingPurposesOnly1234567890ABCDEF");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);

        staff = Staff.builder()
            .id(1L).firstName("John").lastName("Doe")
            .email("john@test.com").password("hashed")
            .role(Role.ADMIN).build();
    }

    @Test
    void generateToken_returnsNonBlankToken() {
        String token = jwtUtil.generateToken(staff);
        assertThat(token).isNotBlank();
    }

    @Test
    void extractEmail_returnsCorrectEmail() {
        String token = jwtUtil.generateToken(staff);
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("john@test.com");
    }

    @Test
    void isTokenValid_returnsTrueForValidToken() {
        String token = jwtUtil.generateToken(staff);
        assertThat(jwtUtil.isTokenValid(token, staff)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalseForWrongUser() {
        String token = jwtUtil.generateToken(staff);
        Staff other = Staff.builder().email("other@test.com").build();
        assertThat(jwtUtil.isTokenValid(token, other)).isFalse();
    }
}
```

- [ ] **Step 8: Run tests**

```bash
mvn test -Dtest=JwtUtilTest
```
Expected: 4 tests pass.

- [ ] **Step 9: Commit**

```bash
git add src/
git commit -m "feat: add Staff entity, JWT infrastructure, and SecurityConfig"
```

---

### Task 4: Auth Endpoints (Login & Register)

**Files:**
- Create: `src/main/java/com/langko/restaurantengine/auth/dto/LoginRequest.java`
- Create: `src/main/java/com/langko/restaurantengine/auth/dto/RegisterRequest.java`
- Create: `src/main/java/com/langko/restaurantengine/auth/dto/AuthResponse.java`
- Create: `src/main/java/com/langko/restaurantengine/staff/dto/StaffResponse.java`
- Create: `src/main/java/com/langko/restaurantengine/auth/AuthService.java`
- Create: `src/main/java/com/langko/restaurantengine/auth/AuthController.java`
- Test: `src/test/java/com/langko/restaurantengine/auth/AuthControllerTest.java`

**Interfaces:**
- Consumes: `StaffRepository`, `JwtUtil`, `PasswordEncoder`, `AuthenticationManager` from Task 3
- Produces:
  - `POST /api/auth/login` → `AuthResponse { token, role, firstName, lastName }`
  - `POST /api/auth/register` → `StaffResponse` (password never exposed; public if no staff exist, ADMIN-only otherwise)
  - `StaffResponse(Staff staff)` constructor mapping all fields except password

- [ ] **Step 1: Create LoginRequest DTO**

`src/main/java/com/langko/restaurantengine/auth/dto/LoginRequest.java`:
```java
package com.langko.restaurantengine.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginRequest {
    @NotBlank @Email
    private String email;
    @NotBlank
    private String password;
}
```

- [ ] **Step 2: Create RegisterRequest DTO**

`src/main/java/com/langko/restaurantengine/auth/dto/RegisterRequest.java`:
```java
package com.langko.restaurantengine.auth.dto;

import com.langko.restaurantengine.staff.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotBlank @Email private String email;
    @NotBlank private String password;
    @NotNull private Role role;
    private String phone;
}
```

- [ ] **Step 3: Create AuthResponse DTO**

`src/main/java/com/langko/restaurantengine/auth/dto/AuthResponse.java`:
```java
package com.langko.restaurantengine.auth.dto;

import com.langko.restaurantengine.staff.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class AuthResponse {
    private String token;
    private Role role;
    private String firstName;
    private String lastName;
}
```

- [ ] **Step 4: Create AuthService**

`src/main/java/com/langko/restaurantengine/auth/AuthService.java`:
```java
package com.langko.restaurantengine.auth;

import com.langko.restaurantengine.auth.dto.AuthResponse;
import com.langko.restaurantengine.auth.dto.LoginRequest;
import com.langko.restaurantengine.auth.dto.RegisterRequest;
import com.langko.restaurantengine.exception.ResourceNotFoundException;
import com.langko.restaurantengine.staff.Staff;
import com.langko.restaurantengine.staff.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        Staff staff = staffRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
        String token = jwtUtil.generateToken(staff);
        log.info("Staff logged in: {}", staff.getEmail());
        return new AuthResponse(token, staff.getRole(), staff.getFirstName(), staff.getLastName());
    }

    public Staff register(RegisterRequest request) {
        if (staffRepository.existsByEmail(request.getEmail())) {
            throw new DataIntegrityViolationException("Email already in use");
        }
        Staff staff = Staff.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(request.getRole())
            .phone(request.getPhone())
            .build();
        Staff saved = staffRepository.save(staff);
        log.info("New staff registered: {}", saved.getEmail());
        return saved;
    }

    public boolean isFirstStaff() {
        return staffRepository.count() == 0;
    }
}
```

- [ ] **Step 5: Create StaffResponse DTO (created here, reused by Task 8)**

`src/main/java/com/langko/restaurantengine/staff/dto/StaffResponse.java`:
```java
package com.langko.restaurantengine.staff.dto;

import com.langko.restaurantengine.staff.Role;
import com.langko.restaurantengine.staff.Staff;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StaffResponse {
    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final Role role;
    private final String phone;
    private final LocalDateTime createdAt;

    public StaffResponse(Staff staff) {
        this.id = staff.getId();
        this.firstName = staff.getFirstName();
        this.lastName = staff.getLastName();
        this.email = staff.getEmail();
        this.role = staff.getRole();
        this.phone = staff.getPhone();
        this.createdAt = staff.getCreatedAt();
    }
}
```

- [ ] **Step 6: Create AuthController**

`src/main/java/com/langko/restaurantengine/auth/AuthController.java`:
```java
package com.langko.restaurantengine.auth;

import com.langko.restaurantengine.auth.dto.AuthResponse;
import com.langko.restaurantengine.auth.dto.LoginRequest;
import com.langko.restaurantengine.auth.dto.RegisterRequest;
import com.langko.restaurantengine.common.ApiResponse;
import com.langko.restaurantengine.staff.Staff;
import com.langko.restaurantengine.staff.dto.StaffResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<StaffResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            @AuthenticationPrincipal Staff currentUser) {
        boolean isFirst = authService.isFirstStaff();
        if (!isFirst) {
            if (currentUser == null || currentUser.getRole() != com.langko.restaurantengine.staff.Role.ADMIN) {
                throw new AccessDeniedException("Only ADMIN can register new staff");
            }
        }
        return ResponseEntity.ok(ApiResponse.success(new StaffResponse(authService.register(request))));
    }
}
```

- [ ] **Step 6: Write integration test**

`src/test/java/com/langko/restaurantengine/auth/AuthControllerTest.java`:
```java
package com.langko.restaurantengine.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langko.restaurantengine.auth.dto.LoginRequest;
import com.langko.restaurantengine.auth.dto.RegisterRequest;
import com.langko.restaurantengine.staff.Role;
import com.langko.restaurantengine.staff.Staff;
import com.langko.restaurantengine.staff.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired StaffRepository staffRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        staffRepository.deleteAll();
    }

    @Test
    void register_firstStaff_returnsOkAndNoPassword() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("John"); req.setLastName("Doe");
        req.setEmail("john@test.com"); req.setPassword("password123");
        req.setRole(Role.ADMIN);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value("john@test.com"))
            .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void login_validCredentials_returnsToken() throws Exception {
        staffRepository.save(Staff.builder()
            .firstName("Jane").lastName("Doe").email("jane@test.com")
            .password(passwordEncoder.encode("secret")).role(Role.ADMIN).build());

        LoginRequest req = new LoginRequest();
        req.setEmail("jane@test.com"); req.setPassword("secret");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").isNotEmpty())
            .andExpect(jsonPath("$.data.firstName").value("Jane"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        staffRepository.save(Staff.builder()
            .firstName("Jane").lastName("Doe").email("jane2@test.com")
            .password(passwordEncoder.encode("secret")).role(Role.ADMIN).build());

        LoginRequest req = new LoginRequest();
        req.setEmail("jane2@test.com"); req.setPassword("wrong");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }
}
```

- [ ] **Step 7: Run tests**

```bash
mvn test -Dtest=AuthControllerTest -Dspring.profiles.active=test
```
Expected: 3 tests pass.

- [ ] **Step 8: Commit**

```bash
git add src/
git commit -m "feat: add auth login and register endpoints with JWT"
```

---

### Task 5: Menu Module

**Files:**
- Create: `src/main/java/com/langko/restaurantengine/menu/MenuCategory.java`
- Create: `src/main/java/com/langko/restaurantengine/menu/MenuItem.java`
- Create: `src/main/java/com/langko/restaurantengine/menu/MenuCategoryRepository.java`
- Create: `src/main/java/com/langko/restaurantengine/menu/MenuItemRepository.java`
- Create: `src/main/java/com/langko/restaurantengine/menu/dto/MenuCategoryRequest.java`
- Create: `src/main/java/com/langko/restaurantengine/menu/dto/MenuItemRequest.java`
- Create: `src/main/java/com/langko/restaurantengine/menu/MenuService.java`
- Create: `src/main/java/com/langko/restaurantengine/menu/MenuController.java`
- Test: `src/test/java/com/langko/restaurantengine/menu/MenuControllerTest.java`

**Interfaces:**
- Consumes: `ApiResponse` (Task 2), `ResourceNotFoundException` (Task 2), `SecurityConfig` public GET `/api/menu/**` (Task 3)
- Produces:
  - `GET /api/menu/categories` → `List<MenuCategory>`
  - `POST /api/menu/categories` → `MenuCategory` (MANAGER, ADMIN)
  - `GET /api/menu/items` → `Page<MenuItem>` (`?categoryId=`, `?page=`, `?size=`)
  - `GET /api/menu/items/{id}` → `MenuItem`
  - `POST /api/menu/items` → `MenuItem` (MANAGER, ADMIN)
  - `PUT /api/menu/items/{id}` → `MenuItem` (MANAGER, ADMIN)
  - `DELETE /api/menu/items/{id}` → void (ADMIN)

- [ ] **Step 1: Create MenuCategory entity**

`src/main/java/com/langko/restaurantengine/menu/MenuCategory.java`:
```java
package com.langko.restaurantengine.menu;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MenuCategory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;
}
```

- [ ] **Step 2: Create MenuItem entity**

`src/main/java/com/langko/restaurantengine/menu/MenuItem.java`:
```java
package com.langko.restaurantengine.menu;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MenuItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Boolean available = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private MenuCategory category;
}
```

- [ ] **Step 3: Create repositories**

`src/main/java/com/langko/restaurantengine/menu/MenuCategoryRepository.java`:
```java
package com.langko.restaurantengine.menu;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
}
```

`src/main/java/com/langko/restaurantengine/menu/MenuItemRepository.java`:
```java
package com.langko.restaurantengine.menu;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    Page<MenuItem> findByCategoryId(Long categoryId, Pageable pageable);
}
```

- [ ] **Step 4: Create DTOs**

`src/main/java/com/langko/restaurantengine/menu/dto/MenuCategoryRequest.java`:
```java
package com.langko.restaurantengine.menu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class MenuCategoryRequest {
    @NotBlank private String name;
    private String description;
}
```

`src/main/java/com/langko/restaurantengine/menu/dto/MenuItemRequest.java`:
```java
package com.langko.restaurantengine.menu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter; import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class MenuItemRequest {
    @NotBlank private String name;
    private String description;
    @NotNull @DecimalMin("0.0") private BigDecimal price;
    private Boolean available = true;
    @NotNull private Long categoryId;
}
```

- [ ] **Step 5: Create MenuService**

`src/main/java/com/langko/restaurantengine/menu/MenuService.java`:
```java
package com.langko.restaurantengine.menu;

import com.langko.restaurantengine.exception.ResourceNotFoundException;
import com.langko.restaurantengine.menu.dto.MenuCategoryRequest;
import com.langko.restaurantengine.menu.dto.MenuItemRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;

    public List<MenuCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    public MenuCategory createCategory(MenuCategoryRequest request) {
        MenuCategory category = MenuCategory.builder()
            .name(request.getName()).description(request.getDescription()).build();
        MenuCategory saved = categoryRepository.save(category);
        log.info("Created menu category: {}", saved.getName());
        return saved;
    }

    public Page<MenuItem> getAllItems(Long categoryId, Pageable pageable) {
        if (categoryId != null) {
            return itemRepository.findByCategoryId(categoryId, pageable);
        }
        return itemRepository.findAll(pageable);
    }

    public MenuItem getItemById(Long id) {
        return itemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + id));
    }

    public MenuItem createItem(MenuItemRequest request) {
        MenuCategory category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));
        MenuItem item = MenuItem.builder()
            .name(request.getName()).description(request.getDescription())
            .price(request.getPrice()).available(request.getAvailable())
            .category(category).build();
        MenuItem saved = itemRepository.save(item);
        log.info("Created menu item: {}", saved.getName());
        return saved;
    }

    public MenuItem updateItem(Long id, MenuItemRequest request) {
        MenuItem item = getItemById(id);
        MenuCategory category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setAvailable(request.getAvailable());
        item.setCategory(category);
        return itemRepository.save(item);
    }

    public void deleteItem(Long id) {
        MenuItem item = getItemById(id);
        itemRepository.delete(item);
        log.info("Deleted menu item: {}", id);
    }
}
```

- [ ] **Step 6: Create MenuController**

`src/main/java/com/langko/restaurantengine/menu/MenuController.java`:
```java
package com.langko.restaurantengine.menu;

import com.langko.restaurantengine.common.ApiResponse;
import com.langko.restaurantengine.menu.dto.MenuCategoryRequest;
import com.langko.restaurantengine.menu.dto.MenuItemRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<MenuCategory>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(menuService.getAllCategories()));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<MenuCategory>> createCategory(
            @Valid @RequestBody MenuCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(menuService.createCategory(request)));
    }

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<Page<MenuItem>>> getItems(
            @RequestParam(required = false) Long categoryId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(menuService.getAllItems(categoryId, pageable)));
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<ApiResponse<MenuItem>> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(menuService.getItemById(id)));
    }

    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<MenuItem>> createItem(@Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(menuService.createItem(request)));
    }

    @PutMapping("/items/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<MenuItem>> updateItem(
            @PathVariable Long id, @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(menuService.updateItem(id, request)));
    }

    @DeleteMapping("/items/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        menuService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
```

- [ ] **Step 7: Write integration test**

`src/test/java/com/langko/restaurantengine/menu/MenuControllerTest.java`:
```java
package com.langko.restaurantengine.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langko.restaurantengine.menu.dto.MenuCategoryRequest;
import com.langko.restaurantengine.staff.Role;
import com.langko.restaurantengine.staff.Staff;
import com.langko.restaurantengine.staff.StaffRepository;
import com.langko.restaurantengine.auth.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MenuControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired StaffRepository staffRepository;
    @Autowired MenuCategoryRepository categoryRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtil jwtUtil;

    private String adminToken;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        staffRepository.deleteAll();
        Staff admin = staffRepository.save(Staff.builder()
            .firstName("Admin").lastName("User").email("admin@test.com")
            .password(passwordEncoder.encode("password")).role(Role.ADMIN).build());
        adminToken = "Bearer " + jwtUtil.generateToken(admin);
    }

    @Test
    void getCategories_public_returnsOk() throws Exception {
        mockMvc.perform(get("/api/menu/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createCategory_asAdmin_returnsOk() throws Exception {
        MenuCategoryRequest req = new MenuCategoryRequest();
        req.setName("Beverages"); req.setDescription("Drinks");

        mockMvc.perform(post("/api/menu/categories")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("Beverages"));
    }

    @Test
    void createCategory_unauthenticated_returns403() throws Exception {
        MenuCategoryRequest req = new MenuCategoryRequest();
        req.setName("Starters");

        mockMvc.perform(post("/api/menu/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());
    }
}
```

- [ ] **Step 8: Run tests**

```bash
mvn test -Dtest=MenuControllerTest -Dspring.profiles.active=test
```
Expected: 3 tests pass.

- [ ] **Step 9: Commit**

```bash
git add src/
git commit -m "feat: add menu module (categories and items CRUD)"
```

---

### Task 6: Table Module

**Files:**
- Create: `src/main/java/com/langko/restaurantengine/table/TableStatus.java`
- Create: `src/main/java/com/langko/restaurantengine/table/RestaurantTable.java`
- Create: `src/main/java/com/langko/restaurantengine/table/TableRepository.java`
- Create: `src/main/java/com/langko/restaurantengine/table/dto/TableRequest.java`
- Create: `src/main/java/com/langko/restaurantengine/table/TableService.java`
- Create: `src/main/java/com/langko/restaurantengine/table/TableController.java`
- Test: `src/test/java/com/langko/restaurantengine/table/TableControllerTest.java`

**Interfaces:**
- Consumes: `ApiResponse` (Task 2), `ResourceNotFoundException` (Task 2)
- Produces:
  - `RestaurantTable` entity; fields: `id`, `tableNumber`, `capacity`, `status`
  - `TableRepository extends JpaRepository<RestaurantTable, Long>`
  - `GET /api/tables` → `List<RestaurantTable>` (STAFF+)
  - `POST /api/tables` → `RestaurantTable` (MANAGER, ADMIN)
  - `PUT /api/tables/{id}` → `RestaurantTable` (MANAGER, ADMIN)
  - `DELETE /api/tables/{id}` → void (ADMIN)

- [ ] **Step 1: Create TableStatus enum**

`src/main/java/com/langko/restaurantengine/table/TableStatus.java`:
```java
package com.langko.restaurantengine.table;

public enum TableStatus {
    AVAILABLE, OCCUPIED, RESERVED
}
```

- [ ] **Step 2: Create RestaurantTable entity**

`src/main/java/com/langko/restaurantengine/table/RestaurantTable.java`:
```java
package com.langko.restaurantengine.table;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurant_tables")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RestaurantTable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tableNumber;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TableStatus status = TableStatus.AVAILABLE;
}
```

- [ ] **Step 3: Create TableRepository**

`src/main/java/com/langko/restaurantengine/table/TableRepository.java`:
```java
package com.langko.restaurantengine.table;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TableRepository extends JpaRepository<RestaurantTable, Long> {
}
```

- [ ] **Step 4: Create TableRequest DTO**

`src/main/java/com/langko/restaurantengine/table/dto/TableRequest.java`:
```java
package com.langko.restaurantengine.table.dto;

import com.langko.restaurantengine.table.TableStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class TableRequest {
    @NotBlank private String tableNumber;
    @NotNull @Min(1) private Integer capacity;
    private TableStatus status = TableStatus.AVAILABLE;
}
```

- [ ] **Step 5: Create TableService**

`src/main/java/com/langko/restaurantengine/table/TableService.java`:
```java
package com.langko.restaurantengine.table;

import com.langko.restaurantengine.exception.ResourceNotFoundException;
import com.langko.restaurantengine.table.dto.TableRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;

    public List<RestaurantTable> getAllTables() {
        return tableRepository.findAll();
    }

    public RestaurantTable getTableById(Long id) {
        return tableRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Table not found: " + id));
    }

    public RestaurantTable createTable(TableRequest request) {
        RestaurantTable table = RestaurantTable.builder()
            .tableNumber(request.getTableNumber())
            .capacity(request.getCapacity())
            .status(request.getStatus()).build();
        RestaurantTable saved = tableRepository.save(table);
        log.info("Created table: {}", saved.getTableNumber());
        return saved;
    }

    public RestaurantTable updateTable(Long id, TableRequest request) {
        RestaurantTable table = getTableById(id);
        table.setTableNumber(request.getTableNumber());
        table.setCapacity(request.getCapacity());
        table.setStatus(request.getStatus());
        return tableRepository.save(table);
    }

    public void deleteTable(Long id) {
        RestaurantTable table = getTableById(id);
        tableRepository.delete(table);
        log.info("Deleted table: {}", id);
    }
}
```

- [ ] **Step 6: Create TableController**

`src/main/java/com/langko/restaurantengine/table/TableController.java`:
```java
package com.langko.restaurantengine.table;

import com.langko.restaurantengine.common.ApiResponse;
import com.langko.restaurantengine.table.dto.TableRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<RestaurantTable>>> getTables() {
        return ResponseEntity.ok(ApiResponse.success(tableService.getAllTables()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantTable>> createTable(
            @Valid @RequestBody TableRequest request) {
        return ResponseEntity.ok(ApiResponse.success(tableService.createTable(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<RestaurantTable>> updateTable(
            @PathVariable Long id, @Valid @RequestBody TableRequest request) {
        return ResponseEntity.ok(ApiResponse.success(tableService.updateTable(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTable(@PathVariable Long id) {
        tableService.deleteTable(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
```

- [ ] **Step 7: Write integration test**

`src/test/java/com/langko/restaurantengine/table/TableControllerTest.java`:
```java
package com.langko.restaurantengine.table;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langko.restaurantengine.auth.JwtUtil;
import com.langko.restaurantengine.table.dto.TableRequest;
import com.langko.restaurantengine.staff.Role;
import com.langko.restaurantengine.staff.Staff;
import com.langko.restaurantengine.staff.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TableControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired StaffRepository staffRepository;
    @Autowired TableRepository tableRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtil jwtUtil;

    private String managerToken;

    @BeforeEach
    void setUp() {
        tableRepository.deleteAll();
        staffRepository.deleteAll();
        Staff manager = staffRepository.save(Staff.builder()
            .firstName("Mgr").lastName("User").email("mgr@test.com")
            .password(passwordEncoder.encode("pass")).role(Role.MANAGER).build());
        managerToken = "Bearer " + jwtUtil.generateToken(manager);
    }

    @Test
    void createTable_asManager_returnsOk() throws Exception {
        TableRequest req = new TableRequest();
        req.setTableNumber("T1"); req.setCapacity(4);

        mockMvc.perform(post("/api/tables")
                .header("Authorization", managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.tableNumber").value("T1"));
    }

    @Test
    void getTables_authenticated_returnsOk() throws Exception {
        mockMvc.perform(get("/api/tables")
                .header("Authorization", managerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getTables_unauthenticated_returns401or403() throws Exception {
        mockMvc.perform(get("/api/tables"))
            .andExpect(result ->
                org.assertj.core.api.Assertions.assertThat(
                    result.getResponse().getStatus()).isIn(401, 403));
    }
}
```

- [ ] **Step 8: Run tests**

```bash
mvn test -Dtest=TableControllerTest -Dspring.profiles.active=test
```
Expected: 3 tests pass.

- [ ] **Step 9: Commit**

```bash
git add src/
git commit -m "feat: add table module with CRUD and role-based access"
```

---

### Task 7: Order Module

**Files:**
- Create: `src/main/java/com/langko/restaurantengine/order/OrderStatus.java`
- Create: `src/main/java/com/langko/restaurantengine/order/Order.java`
- Create: `src/main/java/com/langko/restaurantengine/order/OrderItem.java`
- Create: `src/main/java/com/langko/restaurantengine/order/OrderRepository.java`
- Create: `src/main/java/com/langko/restaurantengine/order/OrderItemRepository.java`
- Create: `src/main/java/com/langko/restaurantengine/order/dto/CreateOrderRequest.java`
- Create: `src/main/java/com/langko/restaurantengine/order/dto/AddOrderItemRequest.java`
- Create: `src/main/java/com/langko/restaurantengine/order/dto/UpdateOrderStatusRequest.java`
- Create: `src/main/java/com/langko/restaurantengine/order/OrderService.java`
- Create: `src/main/java/com/langko/restaurantengine/order/OrderController.java`
- Test: `src/test/java/com/langko/restaurantengine/order/OrderControllerTest.java`

**Interfaces:**
- Consumes:
  - `TableRepository.findById(Long)` → `RestaurantTable` (Task 6)
  - `MenuItemRepository.findById(Long)` → `MenuItem` (Task 5)
  - `StaffRepository` (Task 3)
- Produces:
  - `GET /api/orders` (MANAGER+), `GET /api/orders/{id}` (STAFF+)
  - `POST /api/orders` → creates order + sets table OCCUPIED (STAFF+)
  - `PUT /api/orders/{id}/status` (STAFF+)
  - `POST /api/orders/{id}/items`, `DELETE /api/orders/{id}/items/{itemId}` (STAFF+)

- [ ] **Step 1: Create OrderStatus enum**

`src/main/java/com/langko/restaurantengine/order/OrderStatus.java`:
```java
package com.langko.restaurantengine.order;

public enum OrderStatus {
    PENDING, IN_PROGRESS, COMPLETED, CANCELLED
}
```

- [ ] **Step 2: Create Order entity**

`src/main/java/com/langko/restaurantengine/order/Order.java`:
```java
package com.langko.restaurantengine.order;

import com.langko.restaurantengine.staff.Staff;
import com.langko.restaurantengine.table.RestaurantTable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false)
    private RestaurantTable table;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

- [ ] **Step 3: Create OrderItem entity**

`src/main/java/com/langko/restaurantengine/order/OrderItem.java`:
```java
package com.langko.restaurantengine.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.langko.restaurantengine.menu.MenuItem;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    private String notes;
}
```

- [ ] **Step 4: Create repositories**

`src/main/java/com/langko/restaurantengine/order/OrderRepository.java`:
```java
package com.langko.restaurantengine.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Page<Order> findByTableId(Long tableId, Pageable pageable);
    Page<Order> findByStatusAndTableId(OrderStatus status, Long tableId, Pageable pageable);
}
```

`src/main/java/com/langko/restaurantengine/order/OrderItemRepository.java`:
```java
package com.langko.restaurantengine.order;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
```

- [ ] **Step 5: Create DTOs**

`src/main/java/com/langko/restaurantengine/order/dto/CreateOrderRequest.java`:
```java
package com.langko.restaurantengine.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class CreateOrderRequest {
    @NotNull private Long tableId;
}
```

`src/main/java/com/langko/restaurantengine/order/dto/AddOrderItemRequest.java`:
```java
package com.langko.restaurantengine.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class AddOrderItemRequest {
    @NotNull private Long menuItemId;
    @NotNull @Min(1) private Integer quantity;
    private String notes;
}
```

`src/main/java/com/langko/restaurantengine/order/dto/UpdateOrderStatusRequest.java`:
```java
package com.langko.restaurantengine.order.dto;

import com.langko.restaurantengine.order.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class UpdateOrderStatusRequest {
    @NotNull private OrderStatus status;
}
```

- [ ] **Step 6: Create OrderService**

`src/main/java/com/langko/restaurantengine/order/OrderService.java`:
```java
package com.langko.restaurantengine.order;

import com.langko.restaurantengine.exception.ResourceNotFoundException;
import com.langko.restaurantengine.menu.MenuItem;
import com.langko.restaurantengine.menu.MenuItemRepository;
import com.langko.restaurantengine.order.dto.AddOrderItemRequest;
import com.langko.restaurantengine.order.dto.CreateOrderRequest;
import com.langko.restaurantengine.order.dto.UpdateOrderStatusRequest;
import com.langko.restaurantengine.staff.Staff;
import com.langko.restaurantengine.table.RestaurantTable;
import com.langko.restaurantengine.table.TableRepository;
import com.langko.restaurantengine.table.TableStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TableRepository tableRepository;
    private final MenuItemRepository menuItemRepository;

    public Page<Order> getAllOrders(OrderStatus status, Long tableId, Pageable pageable) {
        if (status != null && tableId != null) {
            return orderRepository.findByStatusAndTableId(status, tableId, pageable);
        } else if (status != null) {
            return orderRepository.findByStatus(status, pageable);
        } else if (tableId != null) {
            return orderRepository.findByTableId(tableId, pageable);
        }
        return orderRepository.findAll(pageable);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request, Staff staff) {
        RestaurantTable table = tableRepository.findById(request.getTableId())
            .orElseThrow(() -> new ResourceNotFoundException("Table not found: " + request.getTableId()));
        table.setStatus(TableStatus.OCCUPIED);
        tableRepository.save(table);

        Order order = Order.builder()
            .table(table).staff(staff).status(OrderStatus.PENDING).build();
        Order saved = orderRepository.save(order);
        log.info("Created order {} for table {}", saved.getId(), table.getTableNumber());
        return saved;
    }

    @Transactional
    public Order updateStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = getOrderById(id);
        order.setStatus(request.getStatus());
        if (request.getStatus() == OrderStatus.COMPLETED || request.getStatus() == OrderStatus.CANCELLED) {
            order.getTable().setStatus(TableStatus.AVAILABLE);
            tableRepository.save(order.getTable());
        }
        return orderRepository.save(order);
    }

    @Transactional
    public Order addItem(Long orderId, AddOrderItemRequest request) {
        Order order = getOrderById(orderId);
        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
            .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + request.getMenuItemId()));
        OrderItem item = OrderItem.builder()
            .order(order).menuItem(menuItem)
            .quantity(request.getQuantity())
            .unitPrice(menuItem.getPrice())
            .notes(request.getNotes()).build();
        order.getItems().add(item);
        return orderRepository.save(order);
    }

    @Transactional
    public Order removeItem(Long orderId, Long itemId) {
        Order order = getOrderById(orderId);
        OrderItem item = orderItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Order item not found: " + itemId));
        order.getItems().remove(item);
        return orderRepository.save(order);
    }
}
```

- [ ] **Step 7: Create OrderController**

`src/main/java/com/langko/restaurantengine/order/OrderController.java`:
```java
package com.langko.restaurantengine.order;

import com.langko.restaurantengine.common.ApiResponse;
import com.langko.restaurantengine.order.dto.AddOrderItemRequest;
import com.langko.restaurantengine.order.dto.CreateOrderRequest;
import com.langko.restaurantengine.order.dto.UpdateOrderStatusRequest;
import com.langko.restaurantengine.staff.Staff;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Page<Order>>> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long tableId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            orderService.getAllOrders(status, tableId, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Order>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Order>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal Staff staff) {
        return ResponseEntity.ok(ApiResponse.success(orderService.createOrder(request, staff)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Order>> updateStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.updateStatus(id, request)));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Order>> addItem(
            @PathVariable Long id, @Valid @RequestBody AddOrderItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.addItem(id, request)));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Order>> removeItem(
            @PathVariable Long id, @PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.removeItem(id, itemId)));
    }
}
```

- [ ] **Step 8: Write integration test**

`src/test/java/com/langko/restaurantengine/order/OrderControllerTest.java`:
```java
package com.langko.restaurantengine.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langko.restaurantengine.auth.JwtUtil;
import com.langko.restaurantengine.order.dto.CreateOrderRequest;
import com.langko.restaurantengine.staff.Role;
import com.langko.restaurantengine.staff.Staff;
import com.langko.restaurantengine.staff.StaffRepository;
import com.langko.restaurantengine.table.RestaurantTable;
import com.langko.restaurantengine.table.TableRepository;
import com.langko.restaurantengine.table.TableStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired StaffRepository staffRepository;
    @Autowired TableRepository tableRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtil jwtUtil;

    private String staffToken;
    private RestaurantTable table;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        tableRepository.deleteAll();
        staffRepository.deleteAll();

        Staff staff = staffRepository.save(Staff.builder()
            .firstName("Waiter").lastName("One").email("waiter@test.com")
            .password(passwordEncoder.encode("pass")).role(Role.STAFF).build());
        staffToken = "Bearer " + jwtUtil.generateToken(staff);

        table = tableRepository.save(RestaurantTable.builder()
            .tableNumber("T1").capacity(4).status(TableStatus.AVAILABLE).build());
    }

    @Test
    void createOrder_asStaff_returnsOk() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setTableId(table.getId());

        mockMvc.perform(post("/api/orders")
                .header("Authorization", staffToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void getOrders_asStaff_returns403() throws Exception {
        mockMvc.perform(get("/api/orders")
                .header("Authorization", staffToken))
            .andExpect(status().isForbidden());
    }

    @Test
    void createOrder_unauthenticated_returns401or403() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setTableId(table.getId());

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(result ->
                org.assertj.core.api.Assertions.assertThat(
                    result.getResponse().getStatus()).isIn(401, 403));
    }
}
```

- [ ] **Step 9: Run tests**

```bash
mvn test -Dtest=OrderControllerTest -Dspring.profiles.active=test
```
Expected: 3 tests pass.

- [ ] **Step 10: Commit**

```bash
git add src/
git commit -m "feat: add order module with full CRUD and table status sync"
```

---

### Task 8: Staff CRUD Module

**Files:**
- Create: `src/main/java/com/langko/restaurantengine/staff/dto/StaffRequest.java`
- Reuse: `src/main/java/com/langko/restaurantengine/staff/dto/StaffResponse.java` *(created in Task 4 — do NOT recreate)*
- Create: `src/main/java/com/langko/restaurantengine/staff/StaffService.java`
- Create: `src/main/java/com/langko/restaurantengine/staff/StaffController.java`
- Test: `src/test/java/com/langko/restaurantengine/staff/StaffControllerTest.java`

**Interfaces:**
- Consumes: `StaffRepository` (Task 3), `PasswordEncoder` (Task 3), `StaffResponse` (Task 4)
- Produces:
  - `GET /api/staff` → `Page<StaffResponse>` (MANAGER, ADMIN)
  - `GET /api/staff/{id}` → `StaffResponse` (MANAGER, ADMIN)
  - `POST /api/staff` → `StaffResponse` (ADMIN)
  - `PUT /api/staff/{id}` → `StaffResponse` (ADMIN)
  - `DELETE /api/staff/{id}` → void (ADMIN)
  - `StaffResponse` never exposes password field (guaranteed by Task 4's implementation)

- [ ] **Step 1: Create StaffRequest DTO**

`src/main/java/com/langko/restaurantengine/staff/dto/StaffRequest.java`:
```java
package com.langko.restaurantengine.staff.dto;

import com.langko.restaurantengine.staff.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class StaffRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotBlank @Email private String email;
    @NotBlank private String password;
    @NotNull private Role role;
    private String phone;
}
```

*Note: `StaffResponse` already exists at `src/main/java/com/langko/restaurantengine/staff/dto/StaffResponse.java` — created in Task 4. Do not recreate it.*

- [ ] **Step 2: Create StaffService**

`src/main/java/com/langko/restaurantengine/staff/StaffService.java`:
```java
package com.langko.restaurantengine.staff;

import com.langko.restaurantengine.exception.ResourceNotFoundException;
import com.langko.restaurantengine.staff.dto.StaffRequest;
import com.langko.restaurantengine.staff.dto.StaffResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<StaffResponse> getAllStaff(Pageable pageable) {
        return staffRepository.findAll(pageable).map(StaffResponse::new);
    }

    public StaffResponse getStaffById(Long id) {
        return new StaffResponse(staffRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + id)));
    }

    public StaffResponse createStaff(StaffRequest request) {
        Staff staff = Staff.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(request.getRole())
            .phone(request.getPhone()).build();
        Staff saved = staffRepository.save(staff);
        log.info("Created staff: {}", saved.getEmail());
        return new StaffResponse(saved);
    }

    public StaffResponse updateStaff(Long id, StaffRequest request) {
        Staff staff = staffRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + id));
        staff.setFirstName(request.getFirstName());
        staff.setLastName(request.getLastName());
        staff.setEmail(request.getEmail());
        staff.setPassword(passwordEncoder.encode(request.getPassword()));
        staff.setRole(request.getRole());
        staff.setPhone(request.getPhone());
        return new StaffResponse(staffRepository.save(staff));
    }

    public void deleteStaff(Long id) {
        Staff staff = staffRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Staff not found: " + id));
        staffRepository.delete(staff);
        log.info("Deleted staff: {}", id);
    }
}
```

- [ ] **Step 4: Create StaffController**

`src/main/java/com/langko/restaurantengine/staff/StaffController.java`:
```java
package com.langko.restaurantengine.staff;

import com.langko.restaurantengine.common.ApiResponse;
import com.langko.restaurantengine.staff.dto.StaffRequest;
import com.langko.restaurantengine.staff.dto.StaffResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Page<StaffResponse>>> getAllStaff(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(staffService.getAllStaff(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<StaffResponse>> getStaff(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(staffService.getStaffById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StaffResponse>> createStaff(
            @Valid @RequestBody StaffRequest request) {
        return ResponseEntity.ok(ApiResponse.success(staffService.createStaff(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StaffResponse>> updateStaff(
            @PathVariable Long id, @Valid @RequestBody StaffRequest request) {
        return ResponseEntity.ok(ApiResponse.success(staffService.updateStaff(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(@PathVariable Long id) {
        staffService.deleteStaff(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deleted"));
    }
}
```

- [ ] **Step 5: Write integration test**

`src/test/java/com/langko/restaurantengine/staff/StaffControllerTest.java`:
```java
package com.langko.restaurantengine.staff;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langko.restaurantengine.auth.JwtUtil;
import com.langko.restaurantengine.staff.dto.StaffRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StaffControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired StaffRepository staffRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtil jwtUtil;

    private String adminToken;
    private String managerToken;

    @BeforeEach
    void setUp() {
        staffRepository.deleteAll();
        Staff admin = staffRepository.save(Staff.builder()
            .firstName("Admin").lastName("User").email("admin@test.com")
            .password(passwordEncoder.encode("pass")).role(Role.ADMIN).build());
        Staff manager = staffRepository.save(Staff.builder()
            .firstName("Mgr").lastName("User").email("mgr@test.com")
            .password(passwordEncoder.encode("pass")).role(Role.MANAGER).build());
        adminToken = "Bearer " + jwtUtil.generateToken(admin);
        managerToken = "Bearer " + jwtUtil.generateToken(manager);
    }

    @Test
    void getAllStaff_asManager_returnsOk() throws Exception {
        mockMvc.perform(get("/api/staff").header("Authorization", managerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createStaff_asAdmin_returnsOk() throws Exception {
        StaffRequest req = new StaffRequest();
        req.setFirstName("New"); req.setLastName("Waiter");
        req.setEmail("waiter@test.com"); req.setPassword("pass123");
        req.setRole(Role.STAFF);

        mockMvc.perform(post("/api/staff")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("waiter@test.com"))
            .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void createStaff_asManager_returns403() throws Exception {
        StaffRequest req = new StaffRequest();
        req.setFirstName("X"); req.setLastName("Y");
        req.setEmail("x@test.com"); req.setPassword("pass"); req.setRole(Role.STAFF);

        mockMvc.perform(post("/api/staff")
                .header("Authorization", managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());
    }
}
```

- [ ] **Step 6: Run tests**

```bash
mvn test -Dtest=StaffControllerTest -Dspring.profiles.active=test
```
Expected: 3 tests pass.

- [ ] **Step 7: Run full test suite**

```bash
mvn test -Dspring.profiles.active=test
```
Expected: All tests pass, BUILD SUCCESS.

- [ ] **Step 8: Commit**

```bash
git add src/
git commit -m "feat: add staff CRUD module (ADMIN-only write, MANAGER read)"
```

---

## Self-Review Checklist

| Spec Requirement | Covered By |
|---|---|
| Spring Boot 3.x, Java 17, MySQL 8, Lombok, JJWT 0.12.x | Task 1 pom.xml |
| Package `com.langko.restaurantengine` | All tasks |
| Staff entity: firstName, lastName, email, password, role, phone | Task 3 |
| JWT login + bootstrap register | Task 4 |
| Menu categories + items CRUD | Task 5 |
| Tables CRUD + status | Task 6 |
| Orders + OrderItems with table status sync | Task 7 |
| Staff CRUD, password never in response | Task 8 |
| Role-based access (ADMIN/MANAGER/STAFF) | Tasks 3–8 via @PreAuthorize |
| ApiResponse envelope | Task 2 |
| GlobalExceptionHandler (404/400/403/401/409/500) | Task 2 |
| Pagination on list endpoints | Tasks 5, 7, 8 |
| Env vars for sensitive config | Task 1 |
| H2 for tests | Task 1 test properties |
