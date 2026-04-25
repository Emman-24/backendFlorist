# 🌸 Akasia Floristería — Backend API

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-1.9.25-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.10-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white"/>
  <img src="https://img.shields.io/badge/Railway-Deployed-0B0D0E?style=for-the-badge&logo=railway&logoColor=white"/>
  <img src="https://img.shields.io/badge/Cloudinary-CDN-3448C5?style=for-the-badge&logo=cloudinary&logoColor=white"/>
</p>

<p align="center">
  Production-grade REST API for <strong>Akasia Floristería</strong> — a Colombian flower shop platform.<br/>
  Built with Hexagonal Architecture, JWT security, rate limiting, and Cloudinary-powered image management.
</p>

---

## 📋 Table of Contents

- [Architecture Overview](#-architecture-overview)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Domain Model](#-domain-model)
- [API Reference](#-api-reference)
- [Security](#-security)
- [Database](#-database)
- [Configuration](#-configuration)
- [Running Locally](#-running-locally)
- [Running Tests](#-running-tests)
- [Deployment](#-deployment-railway)
- [Environment Variables](#-environment-variables)

---

## 🏛️ Architecture Overview

This backend follows **Hexagonal Architecture** (Ports & Adapters) combined with the **Use Case pattern**, keeping business logic completely decoupled from infrastructure.

```
┌─────────────────────────────────────────────────────────────┐
│                        Frontend                             │
│                  floristeriaakasia.com.co                   │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTPS / CORS
┌───────────────────────────▼─────────────────────────────────┐
│                     API Gateway Layer                        │
│         Rate Limit Filter → JWT Auth Filter                 │
│              Security Config (Spring Security)              │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                   Controller Layer                           │
│    REST Controllers (HTTP in/out, DTO mapping only)         │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                 Application Layer (Use Cases)                │
│   CreateFloralArrangementUseCase                            │
│   GetFloralArrangementsUseCase                              │
│   AddImageToFloralArrangementUseCase                        │
└──────────┬────────────────────────────────┬─────────────────┘
           │                                │
┌──────────▼──────────┐         ┌───────────▼─────────────────┐
│   Domain Layer      │         │     Infrastructure Layer     │
│   Entities          │         │   JPA Repositories           │
│   Business Rules    │         │   Cloudinary Adapter         │
│   Domain Events     │         │   Persistence Adapters       │
└─────────────────────┘         └─────────────────────────────┘
```

### Key Design Decisions

| Decision | Choice | Reason |
|---|---|---|
| Architecture | Hexagonal (Ports & Adapters) | Testability, infrastructure independence |
| Category Hierarchy | **Materialized Path** | Fast subtree queries, no recursive CTEs needed |
| Auth | Stateless JWT (access + refresh) | Scalable, no server-side session state |
| Image Storage | Cloudinary CDN | Automatic optimization, multiple resolutions |
| Rate Limiting | In-process Caffeine + Guava RateLimiter | No Redis dependency, per-IP per-endpoint |
| DB Migrations | Liquibase | Version-controlled, auditable schema changes |
| Pagination | Spring Data `Page<T>` with separate N+1-safe fetches | Avoids HHH90003004 Hibernate warning |

---

## 🔧 Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 1.9.25 |
| Framework | Spring Boot 3.5.10 |
| Security | Spring Security + JJWT 0.12.6 |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8.0 |
| Migrations | Liquibase |
| Images | Cloudinary |
| Caching | Caffeine |
| Rate Limiting | Guava RateLimiter |
| Validation | Jakarta Validation |
| Testing | JUnit 5 + Testcontainers + MockMVC |
| Documentation | SpringDoc OpenAPI (Swagger UI) |
| Deployment | Railway |

---

## 📁 Project Structure

```
src/main/kotlin/com/floristeriaakasia/backend/
│
├── BackendFloristApplication.kt          # Entry point
│
├── feature/                              # Feature slices (vertical modules)
│   ├── floralArrangment/
│   │   ├── application/                  # Use cases (business logic)
│   │   │   ├── CreateFloralArrangementUseCase.kt
│   │   │   ├── CreateFloralArrangementUseCaseImpl.kt
│   │   │   ├── GetFloralArrangementsUseCase.kt
│   │   │   ├── GetFloralArrangementsUseCaseImpl.kt
│   │   │   ├── AddImageToFloralArrangementUseCase.kt
│   │   │   ├── AddImageToFloralArrangementUseCaseImpl.kt
│   │   │   ├── CreateFloralArrangementCommand.kt
│   │   │   └── ImageStoragePort.kt       # Output port (interface)
│   │   ├── domain/                       # Entities, repository interface
│   │   │   ├── FloralArrangement.kt
│   │   │   ├── ProductGallery.kt
│   │   │   └── FloralArrangementRepository.kt
│   │   └── infrastructure/               # Adapters, controllers, DTOs
│   │       ├── FloralArrangementController.kt
│   │       ├── FloralArrangmentDTO.kt
│   │       ├── SaveFloralArrangementPort.kt
│   │       ├── Adapter.kt
│   │       └── CloudinaryImageAdapter.kt  # Output adapter
│   │
│   ├── category/                         # Materialized Path hierarchy
│   ├── tag/
│   ├── faq/
│   ├── flowers/
│   ├── price/
│   ├── productDescription/
│   ├── role/
│   └── user/
│
├── global/
│   ├── config/
│   │   ├── SecurityConfig.kt             # CORS + JWT + authorization rules
│   │   ├── RateLimitFilter.kt            # Per-IP rate limiting
│   │   ├── FilterConfig.kt
│   │   ├── AsyncConfig.kt
│   │   ├── CloudinaryConfig.kt
│   │   ├── ActuatorSecurityConfig.kt
│   │   └── DatabaseMonitoringConfig.kt
│   ├── security/
│   │   ├── JwtService.kt
│   │   ├── JWTAuthenticationFilter.kt
│   │   ├── JwtTokenBlacklist.kt          # Caffeine-backed revocation
│   │   └── UserDetailService.kt
│   └── exeption/
│       ├── GlobalExceptionHandler.kt
│       └── ...
│
└── util/
    ├── ApiResponse.kt                    # Sealed response wrapper
    └── HtmlSanitizer.kt
```

---

## 🗃️ Domain Model

### Entity Relationships

```
FloralArrangement
    │── Price (OneToOne)
    │── ProductDescription (OneToOne)
    │── ProductGallery[] (OneToMany)  ← ordered by position, primary flag
    │── Flowers[] (OneToMany)
    │── Category[] (ManyToMany)       ← Materialized Path hierarchy
    └── Tag[] (ManyToMany)

User
    └── Role[] (ManyToMany)           ← ADMIN | MANAGER | USER

Faq                                   ← position-ordered, view counter
```

### Category — Materialized Path

Categories support unlimited nesting using the **Materialized Path** pattern. A path like `/1/4/12/` means the category with id=12 is a grandchild of id=1.

```
Root: Flores Frescas    path=/1/
  Child: Rosas          path=/1/4/
    Grandchild: Rojas   path=/1/4/12/
```

Querying an entire subtree is a single indexed `LIKE` query:

```sql
SELECT * FROM categories WHERE path LIKE '/1/%'
```

---

## 📡 API Reference

Base URL (production): `https://backendflorist-production.up.railway.app`

### 🔓 Public Endpoints (no auth required)

#### Floral Arrangements

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/floral-arrangement` | Paginated list with filters |
| `GET` | `/api/floral-arrangement/{id}` | Detail by ID (increments views) |
| `GET` | `/api/floral-arrangement/slug/{slug}` | Detail by SEO slug |
| `GET` | `/api/floral-arrangement/seo-name/{seoName}` | Detail by SEO name |

**Query Parameters** (`GET /api/floral-arrangement`):

| Param | Type | Default | Description |
|---|---|---|---|
| `page` | int | 0 | Zero-based page number |
| `size` | int | 12 | Results per page (max 50) |
| `sortBy` | string | `views` | `name`, `price`, `views`, `createdAt`, `updatedAt` |
| `sortDir` | string | `desc` | `asc` or `desc` |
| `categoryId` | long | — | Filter by category |
| `tagId` | long | — | Filter by tag |
| `featured` | boolean | — | Featured products only |
| `seasonal` | boolean | — | Seasonal products only |
| `available` | boolean | — | Available products only |
| `minPrice` | decimal | — | Minimum price (COP) |
| `maxPrice` | decimal | — | Maximum price (COP) |

#### Categories

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/categories` | All categories (flat) |
| `GET` | `/api/categories/tree` | Full hierarchy as nested tree |
| `GET` | `/api/categories/{id}` | Single category |

#### Tags & FAQs

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/tags` | All active tags |
| `GET` | `/api/faqs` | All active FAQs (ordered by position) |
| `GET` | `/api/faqs/{id}` | Single FAQ (increments views) |

### 🔐 Protected Endpoints

#### Auth

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/login` | None | Returns access + refresh tokens |

**Login Request:**
```json
{
  "username": "admin",
  "password": "securepassword"
}
```

**Login Response:**
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 3600000,
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@akasia.com",
    "roles": ["ADMIN"]
  }
}
```

#### Admin / Manager

| Method | Endpoint | Role | Description |
|---|---|---|---|
| `POST` | `/api/floral-arrangement` | ADMIN, MANAGER | Create new arrangement |
| `POST` | `/api/floral-arrangement/{id}/image` | ADMIN, MANAGER | Upload image to gallery |
| `POST` | `/api/categories/root` | ADMIN | Create root category |
| `POST` | `/api/categories/{parentId}/children` | ADMIN | Create child category |
| `POST` | `/api/tags` | ADMIN, MANAGER | Create tag |
| `POST` | `/api/faqs` | ADMIN, MANAGER | Create FAQ |
| `PUT` | `/api/faqs/{id}` | ADMIN, MANAGER | Update FAQ |
| `PATCH` | `/api/faqs/{id}/status` | ADMIN, MANAGER | Toggle FAQ status |
| `PUT` | `/api/faqs/reorder` | ADMIN, MANAGER | Reorder FAQs |
| `DELETE` | `/api/faqs/{id}` | ADMIN | Delete FAQ |

### Response Format

All endpoints return a consistent `ApiResponse<T>` envelope:

```json
// Success
{
  "data": { ... },
  "message": "Optional message"
}

// Error
{
  "message": "Human-readable error",
  "code": "MACHINE_CODE",
  "errors": { "field": "reason" }  // validation errors only
}
```

### HTTP Status Codes

| Code | Meaning |
|---|---|
| `200` | OK |
| `201` | Created |
| `400` | Bad Request / Business Rule Violation |
| `401` | Unauthorized (missing or invalid token) |
| `403` | Forbidden (insufficient role) |
| `404` | Resource not found |
| `409` | Conflict (duplicate slug, email, etc.) |
| `422` | Validation failed (field errors) |
| `429` | Rate limit exceeded |
| `500` | Internal Server Error |

---

## 🔒 Security

### JWT Authentication

- **Access token**: 1 hour TTL, carries `roles` and `typ=access` claims
- **Refresh token**: 24 hour TTL, carries `typ=refresh` claim
- **Token blacklist**: Caffeine in-memory cache (50k entries, 24h TTL) for revocation
- **User cache**: 5-minute Caffeine cache per username to avoid repeated DB lookups

### Rate Limiting (per IP, per endpoint category)

| Endpoint Category | Limit |
|---|---|
| `/api/auth/**` | 5 req/s |
| Image upload | 2 req/s |
| `/api/admin/**` | 30 req/s |
| Write methods (POST/PUT/PATCH/DELETE) | 20 req/s |
| Public GET | 100 req/s |

Rate limit headers returned on every response:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 99
X-RateLimit-Reset: 10
```

### CORS

Configured via `CORS_ALLOWED_ORIGINS` environment variable (comma-separated). Both `www` and non-`www` variants must be listed explicitly.

```
CORS_ALLOWED_ORIGINS=https://floristeriaakasia.com.co,https://www.floristeriaakasia.com.co
```

### Security Headers

Every response includes:

- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Strict-Transport-Security: max-age=31536000; includeSubDomains; preload`
- `Referrer-Policy: strict-origin-when-cross-origin`
- `Content-Security-Policy` (dynamically built, stricter in production)

### Input Sanitization

All user input passes through `HtmlSanitizer` before persistence:
- HTML escaping via Spring's `HtmlUtils`
- XSS pattern removal (`<script>`, `javascript:`, event handlers)
- Slug normalization (lowercase, alphanumeric + hyphens only)
- URL validation (only `http://` and `https://` allowed)

---

## 🗄️ Database

### Schema (Liquibase-managed)

Migrations live in `src/main/resources/db/changelog/changes/` and are applied in order via `liquibase.xml`.

```
liquibase.xml
  ├── create-table-categories.xml
  ├── create-table-roles.xml
  ├── create-table-users.xml
  ├── create-table-faqs.xml
  ├── create-table-tag.xml
  ├── create-table-prices.xml
  ├── create-table-product_descriptions.xml
  ├── create-table-floralArrangement.xml
  ├── create-table-flowers.xml
  └── create-table-product_gallery.xml
```

### Key Indexes

| Table | Index | Type |
|---|---|---|
| `users` | `username` | UNIQUE |
| `users` | `email` | UNIQUE |
| `categories` | `slug` | UNIQUE |
| `categories` | `path` | Used for Materialized Path `LIKE` queries |
| `floral_arrangements` | `slug` | UNIQUE |
| `product_gallery` | `public_id` | UNIQUE |

### Connection Pool (HikariCP)

Production is tuned conservatively for Railway's free tier:

```properties
spring.datasource.hikari.maximum-pool-size=3
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=600000
```

### N+1 Prevention

The paginated arrangement list uses three targeted queries instead of a single `JOIN FETCH` with pagination (which triggers HHH90003004):

1. `findAllWithFilters(...)` — scalar fields + price
2. `findWithCategoriesByIds(ids)` — categories batch
3. `findWithTagsByIds(ids)` — tags batch
4. `findPrimaryImagesByArrangementIds(ids)` — primary image per arrangement

---

## ⚙️ Configuration

### application.properties (base)

The base config applies to all environments. Secrets are **never** hardcoded — they are injected via environment variables.

### Profile Hierarchy

| Profile | File | When |
|---|---|---|
| (none) | `application.properties` | Always loaded |
| `local` | `application-local.properties` | Local development |
| `prod` | `application-prod.properties` | Railway production |
| `test` | `application-test.properties` | Integration tests |

Activate a profile:
```bash
# Local dev
-Dspring.profiles.active=local

# Railway sets APP_ENVIRONMENT=production automatically
```

---

## 🚀 Running Locally

### Prerequisites

- JDK 21+
- Maven 3.9+
- MySQL 8.0 (local instance or Docker)
- A Cloudinary account (free tier works)

### 1. Clone and configure

```bash
git clone https://github.com/your-org/akasia-backend.git
cd akasia-backend
```

Create a `.env` file or export the following variables (see [Environment Variables](#-environment-variables) below).

### 2. Set up local database

```bash
mysql -u root -p
CREATE DATABASE akasia_local CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'akasia'@'localhost' IDENTIFIED BY 'yourpassword';
GRANT ALL PRIVILEGES ON akasia_local.* TO 'akasia'@'localhost';
```

### 3. Run

```bash
# Using Maven wrapper
./mvnw spring-boot:run -Dspring-boot.run.profiles=local \
  -Dspring-boot.run.jvmArguments="\
    -DDB_NAME=akasia_local \
    -DDB_USERNAME=akasia \
    -DDB_PASSWORD=yourpassword \
    -DJWT_SECRET_KEY=your-32-byte-minimum-secret-key-here \
    -DCLOUDINARY_CLOUD_NAME=your-cloud \
    -DCLOUDINARY_API_KEY=your-key \
    -DCLOUDINARY_API_SECRET=your-secret"
```

The API will be available at `http://localhost:8080`.

Swagger UI (local only): `http://localhost:8080/swagger-ui/index.html`

---

## 🧪 Running Tests

Tests use **Testcontainers** — a real MySQL 8 container is spun up automatically. Docker must be running.

```bash
# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=FaqControllerTest

# Run with verbose output
./mvnw test -Dsurefire.useFile=false
```

### Test Coverage

| Test | Type | What it covers |
|---|---|---|
| `FaqControllerTest` | Integration | CRUD endpoints, auth enforcement |
| `RateLimitFilterTest` | Integration | Rate-limit headers present on every route |
| `AddImageToFloralArrangementUseCaseImplTest` | Unit | Gallery upload logic, primary flag |
| `FloralArrangementTest` | Unit | `addImage`, `hashCode`, no StackOverflow |

---

## 🚂 Deployment (Railway)

### Build

Railway auto-detects the Maven project and runs:

```bash
./mvnw package -DskipTests
java -jar target/backend-1.0.0-SNAPSHOT.jar
```

### Health Checks

Railway uses the `/actuator/health` endpoint (public, no auth):

```
GET https://backendflorist-production.up.railway.app/actuator/health
```

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP", "details": { "database": "MySQL" } },
    "cloudinary": { "status": "UP" }
  }
}
```

---

## 🌍 Environment Variables

| Variable | Required | Example | Description |
|---|---|---|---|
| `JWT_SECRET_KEY` | ✅ | `404E635266...` | Min 32 bytes. Generate: `openssl rand -hex 32` |
| `SPRING_DATASOURCE_URL` | ✅ | `jdbc:mysql://host:3306/db` | Full JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | ✅ | `akasia` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | ✅ | `***` | DB password |
| `CLOUDINARY_CLOUD_NAME` | ✅ | `my-cloud` | Cloudinary cloud name |
| `CLOUDINARY_API_KEY` | ✅ | `123456789` | Cloudinary API key |
| `CLOUDINARY_API_SECRET` | ✅ | `abc123...` | Cloudinary API secret |
| `CORS_ALLOWED_ORIGINS` | ✅ | `https://floristeriaakasia.com.co,https://www.floristeriaakasia.com.co` | Comma-separated allowed origins |
| `PORT` | Railway sets this | `8080` | Server port |
| `APP_ENVIRONMENT` | No | `production` | Set to `local` to enable Swagger + relaxed CSP |
| `LOG_LEVEL_ROOT` | No | `WARN` | Root log level |
| `LOG_LEVEL_APP` | No | `INFO` | App-specific log level |

> ⚠️ **Never commit real secrets.** All sensitive values must be injected at runtime via environment variables or a secrets manager.

---

## 📊 Monitoring

### Actuator Endpoints

| Endpoint | Access | Description |
|---|---|---|
| `/actuator/health` | Public | Liveness + DB + Cloudinary |
| `/actuator/health/liveness` | Public | Kubernetes-style liveness |
| `/actuator/info` | Public | Build info, git commit |
| `/actuator/metrics` | ADMIN | Micrometer metrics |
| `/actuator/prometheus` | ADMIN | Prometheus scrape endpoint |
| `/actuator/beans` | ADMIN | Spring bean graph |

### Pool Monitoring

`DatabaseMonitoringConfig` logs a `WARN` when:
- HikariCP utilization exceeds **80%**
- Threads are waiting for a connection

---

## 📐 Naming Conventions

| Artifact | Convention | Example |
|---|---|---|
| REST endpoints | kebab-case | `/api/floral-arrangement` |
| JSON fields | camelCase | `isAvailable`, `seoName` |
| DB tables | snake_case | `floral_arrangements` |
| DB columns | snake_case | `is_available` |
| Kotlin files | PascalCase | `FloralArrangement.kt` |
| Use cases | Verb + Noun + UseCase | `CreateFloralArrangementUseCase` |

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feat/my-feature`)
3. Write tests for your changes
4. Ensure all tests pass (`./mvnw test`)
5. Commit with a conventional commit message (`feat:`, `fix:`, `chore:`)
6. Open a Pull Request

---

## 👨‍💻 Author

**Emmanuel Serna Yanes**
Software Engineer — Akasia Floristería
📧 emmanuel240158@gmail.com

---

<p align="center">
  Built with ❤️ in Colombia 🇨🇴
</p>
