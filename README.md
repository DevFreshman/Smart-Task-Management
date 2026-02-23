# 📋 Smart Task Management System

A RESTful backend API for personal and team task management, built with **Hexagonal Architecture** (Ports & Adapters).

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.3-brightgreen?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)

---

## 🏗️ Architecture

This project follows **Hexagonal Architecture** (Ports & Adapters) — the domain layer is pure Java with zero framework dependencies.

```
src/main/java/com/github/hoangducmanh/smarttaskmanagement/
│
├── domain/             # Pure Java — NO Spring, NO JPA imports
│   ├── model/          # Task, User, Comment (domain entities)
│   ├── service/        # Domain services & business rules
│   └── repository/     # Repository interfaces (ports)
│
├── application/        # Use cases — imports domain only
│   ├── usecase/        # CreateTaskUseCase, UpdateTaskUseCase, ...
│   └── port/           # Input & output port interfaces
│
├── infrastructure/     # Framework code — Spring Data JPA, Redis, AOP
│   ├── persistence/    # JPA entities, repository implementations
│   ├── cache/          # Redis adapter
│   └── scheduler/      # Deadline reminder scheduler
│
└── web/                # HTTP layer — Controllers, Security, DTOs
    ├── controller/
    └── security/       # JWT filter, Spring Security config
```

**Dependency rule:** `web → application → domain ← infrastructure`

---

## ✨ Features

| Feature | Description | Status |
|---|---|---|
| JWT Authentication | Access token (15m) + Refresh token (7d) with revoke | ✅ |
| Task CRUD | Create, update, soft-delete tasks | ✅ |
| State Machine | TODO → IN\_PROGRESS → COMPLETED (no skipping) | ✅ |
| Role-based Access | USER and ADMIN roles | ✅ |
| Filter & Pagination | Filter by status, priority, deadline; sort & paginate | ✅ |
| Redis Cache | Cache task list with `@Cacheable` / `@CacheEvict` | ✅ |
| Rate Limiting | Max 5 failed login attempts / 15 min / IP | ✅ |
| Audit Log | Auto-log every create/update/delete via Spring AOP | ✅ |
| Deadline Reminder | Daily scheduler at 8:00 AM for upcoming deadlines | ✅ |
| Comments | Add/soft-delete comments with author permission check | ✅ |

---

## 🚀 Getting Started

### Prerequisites

- Docker & Docker Compose
- Java 21+ (only needed if running without Docker)

### Run with Docker (recommended)

```bash
# 1. Clone the repo
git clone https://github.com/yourname/smart-task-management.git
cd smart-task-management

# 2. Copy environment file and fill in your values
cp .env.example .env

# 3. Start everything (App + PostgreSQL + Redis)
docker-compose up
```

App will be available at `http://localhost:8080`

### Run locally (without Docker)

```bash
# Start only the databases
docker-compose up postgres redis

# Run the app
./mvnw spring-boot:run
```

---

## 🔑 Environment Variables

Copy `.env.example` to `.env` and configure:

```env
DB_URL=jdbc:postgresql://localhost:5432/taskmanagement
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_SECRET=your_jwt_secret_minimum_32_characters
JWT_EXPIRATION=900000
REDIS_HOST=localhost
REDIS_PORT=6379
```

> ⚠️ Never commit `.env` to Git. Only `.env.example` is committed.

---

## 📡 API Endpoints

Full interactive docs available at **`/swagger-ui.html`** after starting the app.

### Auth
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Public | Register new account |
| POST | `/api/v1/auth/login` | Public | Login, returns access + refresh token |
| POST | `/api/v1/auth/refresh` | Public | Get new access token |
| POST | `/api/v1/auth/logout` | JWT | Revoke refresh token |

### Tasks
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/tasks` | JWT | List tasks (filter + pagination) |
| POST | `/api/v1/tasks` | JWT | Create task |
| GET | `/api/v1/tasks/{id}` | JWT | Get task detail |
| PUT | `/api/v1/tasks/{id}` | JWT | Update task |
| DELETE | `/api/v1/tasks/{id}` | JWT | Soft delete task |
| PATCH | `/api/v1/tasks/{id}/status` | JWT | Change task status |

### Comments
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/tasks/{id}/comments` | JWT | List comments |
| POST | `/api/v1/tasks/{id}/comments` | JWT | Add comment |
| DELETE | `/api/v1/tasks/{id}/comments/{cid}` | JWT | Delete comment (author or ADMIN only) |

---

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run with coverage report
./mvnw test jacoco:report
# Report at: target/site/jacoco/index.html
```

| Type | Tool | Target |
|---|---|---|
| Unit Test | JUnit 5 + Mockito | ≥ 60% application layer |
| Integration Test | Testcontainers + PostgreSQL | All repositories |
| Security Test | MockMvc + Spring Security Test | All endpoints |

> Integration tests use **real PostgreSQL** via Testcontainers — no H2 in-memory database.

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.3 |
| ORM | Spring Data JPA + Hibernate 6 |
| Database | PostgreSQL 15 |
| Cache | Redis 7 |
| Migration | Flyway |
| Security | Spring Security 6 + JJWT |
| Testing | JUnit 5 + Mockito + Testcontainers |
| Docs | SpringDoc OpenAPI 3 (Swagger UI) |
| CI | GitHub Actions |
| Container | Docker + Docker Compose |

---

## 📐 Design Decisions

**Why Hexagonal Architecture?**
The domain layer has zero Spring imports — business logic is testable without starting the Spring context. This also makes it easy to swap infrastructure (e.g., replace PostgreSQL with another DB) without touching domain code.

**Why UUID instead of SERIAL for IDs?**
UUIDs are safe to expose in URLs (no enumeration attacks), work across distributed systems, and can be generated client-side if needed.

**Why store refresh token as hash?**
The raw token is only returned once (at login). Only the hash is stored in the DB — if the DB is compromised, attackers can't reuse tokens.

**Why Testcontainers over H2?**
H2 has SQL dialect differences from PostgreSQL. Testcontainers runs real PostgreSQL in Docker, so integration tests catch real-world query bugs.

---

## 🏥 Health Check

```
GET /actuator/health
```