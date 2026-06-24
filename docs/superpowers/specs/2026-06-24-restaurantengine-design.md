# RestaurantEngine — Design Spec

**Date:** 2026-06-24
**Stack:** Spring Boot 3.x · Java 17 · MySQL 8 · Lombok · Spring Security 6 · JWT
**Build:** Maven

---

## 1. Overview

RestaurantEngine is a production-quality REST API for restaurant management. It covers four core domains — menu, orders, tables, and staff — with JWT-based authentication and role-based access control. The codebase is structured to be both clean enough to learn from and robust enough to serve as a production starter.

---

## 2. Architecture

**Pattern:** Layered monolith, package-by-feature.

Each feature package owns its controller, service, repository, entities, and DTOs. Cross-cutting concerns (security config, exception handling) live in dedicated top-level packages.

```
com.langko.restaurantengine/
├── auth/
│   ├── AuthController.java
│   ├── AuthService.java
│   ├── JwtUtil.java
│   ├── JwtAuthFilter.java
│   └── dto/  (LoginRequest, AuthResponse)
├── menu/
│   ├── MenuItemController.java
│   ├── MenuItemService.java
│   ├── MenuItemRepository.java
│   ├── MenuCategory.java
│   ├── MenuItem.java
│   └── dto/
├── order/
│   ├── OrderController.java
│   ├── OrderService.java
│   ├── OrderRepository.java
│   ├── Order.java
│   ├── OrderItem.java
│   └── dto/
├── table/
│   ├── TableController.java
│   ├── TableService.java
│   ├── TableRepository.java
│   ├── RestaurantTable.java
│   └── dto/
├── staff/
│   ├── StaffController.java
│   ├── StaffService.java
│   ├── StaffRepository.java
│   ├── Staff.java
│   └── dto/
├── config/
│   └── SecurityConfig.java
└── exception/
    ├── GlobalExceptionHandler.java
    └── ResourceNotFoundException.java
```

---

## 3. Data Model

### Staff
| Field | Type | Notes |
|---|---|---|
| id | Long | PK, auto-generated |
| firstName | String | Not null |
| lastName | String | Not null |
| email | String | Unique, used for login |
| password | String | BCrypt hashed |
| role | Enum | ADMIN, MANAGER, STAFF |
| phone | String | Optional |
| createdAt | LocalDateTime | Auto-set |

### MenuCategory
| Field | Type | Notes |
|---|---|---|
| id | Long | PK |
| name | String | Not null, unique |
| description | String | Optional |

### MenuItem
| Field | Type | Notes |
|---|---|---|
| id | Long | PK |
| name | String | Not null |
| description | String | Optional |
| price | BigDecimal | Not null, >= 0 |
| available | Boolean | Default true |
| category | MenuCategory | ManyToOne |

### RestaurantTable
| Field | Type | Notes |
|---|---|---|
| id | Long | PK |
| tableNumber | String | Unique (e.g. "T1") |
| capacity | Integer | Not null |
| status | Enum | AVAILABLE, OCCUPIED, RESERVED |

### Order
| Field | Type | Notes |
|---|---|---|
| id | Long | PK |
| table | RestaurantTable | ManyToOne |
| staff | Staff | ManyToOne (who took the order) |
| status | Enum | PENDING, IN_PROGRESS, COMPLETED, CANCELLED |
| createdAt | LocalDateTime | Auto-set |
| updatedAt | LocalDateTime | Auto-updated |

### OrderItem
| Field | Type | Notes |
|---|---|---|
| id | Long | PK |
| order | Order | ManyToOne |
| menuItem | MenuItem | ManyToOne |
| quantity | Integer | Not null, >= 1 |
| unitPrice | BigDecimal | Captured at order time |
| notes | String | Optional (e.g. "no onions") |

---

## 4. API Endpoints

All endpoints are prefixed with `/api`.

### Auth (public)
```
POST  /api/auth/login       Body: {email, password}  → {token, role, firstName, lastName}
POST  /api/auth/register    Body: {firstName, lastName, email, password, role, phone}  → Staff
```
`/api/auth/register` is **public when no Staff records exist** (bootstrap the first ADMIN), and **ADMIN-only** once any staff member exists.

### Menu
```
GET    /api/menu/categories           List categories          (public)
POST   /api/menu/categories           Create category          (MANAGER, ADMIN)

GET    /api/menu/items                List items (?category=)  (public)
GET    /api/menu/items/{id}           Get item                 (public)
POST   /api/menu/items                Create item              (MANAGER, ADMIN)
PUT    /api/menu/items/{id}           Update item              (MANAGER, ADMIN)
DELETE /api/menu/items/{id}           Delete item              (ADMIN)
```

### Tables
```
GET    /api/tables                    List tables with status  (STAFF+)
POST   /api/tables                    Create table             (MANAGER, ADMIN)
PUT    /api/tables/{id}               Update / change status   (MANAGER, ADMIN)
DELETE /api/tables/{id}               Delete table             (ADMIN)
```

### Orders
```
GET    /api/orders                    List orders (?status=, ?tableId=)  (MANAGER, ADMIN)
GET    /api/orders/{id}               Get order with items               (STAFF+)
POST   /api/orders                    Create order for a table           (STAFF+)
PUT    /api/orders/{id}/status        Update order status                (STAFF+)
POST   /api/orders/{id}/items         Add item to order                  (STAFF+)
DELETE /api/orders/{id}/items/{itemId} Remove item from order            (STAFF+)
```

### Staff
```
GET    /api/staff                     List staff               (MANAGER, ADMIN)
GET    /api/staff/{id}                Get staff member         (MANAGER, ADMIN)
POST   /api/staff                     Create staff             (ADMIN)
PUT    /api/staff/{id}                Update staff             (ADMIN)
DELETE /api/staff/{id}                Delete staff             (ADMIN)
```

### Response Envelope
```json
// Success
{ "success": true, "data": { ... }, "message": "OK" }

// Error
{ "success": false, "error": "Resource not found", "status": 404 }
```

List endpoints support pagination: `?page=0&size=20&sort=createdAt,desc`

---

## 5. Security

**JWT Flow:**
1. Client POSTs credentials to `/api/auth/login`
2. `AuthService` validates email + BCrypt password
3. `JwtUtil` generates a signed HS256 token (24h expiry)
4. Client sends `Authorization: Bearer <token>` on subsequent requests
5. `JwtAuthFilter` (`OncePerRequestFilter`) validates token, loads `Staff` as `UserDetails`, sets `SecurityContextHolder`
6. `@PreAuthorize` annotations on controllers enforce role checks

**Role Matrix:**
| Action | STAFF | MANAGER | ADMIN |
|---|---|---|---|
| View menu / tables | ✓ | ✓ | ✓ |
| Manage orders | ✓ | ✓ | ✓ |
| Manage menu / tables | ✗ | ✓ | ✓ |
| Manage staff | ✗ | view only | ✓ |
| Delete anything | ✗ | ✗ | ✓ |

---

## 6. Cross-Cutting Concerns

### Validation
- Request DTOs use Jakarta Bean Validation (`@NotBlank`, `@NotNull`, `@Min`, `@Email`, `@DecimalMin`)
- Controllers use `@Valid` on `@RequestBody`
- Field-level errors returned as structured JSON on 400

### Error Handling (`GlobalExceptionHandler`)
| Exception | HTTP |
|---|---|
| `ResourceNotFoundException` | 404 |
| `MethodArgumentNotValidException` | 400 |
| `AccessDeniedException` | 403 |
| `AuthenticationException` | 401 |
| `DataIntegrityViolationException` | 409 |
| `Exception` (catch-all) | 500 |

### Logging
- SLF4J + Logback (Spring Boot default)
- `INFO` for key service operations
- `ERROR` for all exceptions in `GlobalExceptionHandler`

---

## 7. Configuration

```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/restaurant_engine
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000
```

Sensitive values (`DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`) are injected via environment variables — never hardcoded.

---

## 8. Dependencies (pom.xml)

```xml
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-security
spring-boot-starter-validation
mysql-connector-j
lombok
jjwt-api / jjwt-impl / jjwt-jackson  (io.jsonwebtoken, 0.12.x)
```
