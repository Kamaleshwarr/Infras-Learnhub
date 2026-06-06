# Engineering Learning Hub

Enterprise-grade internal web application for managing learning initiatives, certification programs,
study materials, project knowledge, KT documents, and leaderboards.

This repository currently contains the Phase 1 backend foundation.

## Backend

Technology stack:

- Java 21
- Spring Boot 3
- Maven
- PostgreSQL
- Flyway
- Spring Security with JWT
- OpenAPI / Swagger

### Run locally with Docker Compose

```bash
docker compose up --build
```

Backend health check:

```bash
curl http://localhost:8080/api/v1/health
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

### Default bootstrap login credentials

These accounts are seeded by Flyway for local validation and bootstrap access. Change or remove
them before using a shared or production environment.

| Role | Email | Password |
| --- | --- | --- |
| Admin | `admin@learninghub.local` | `Admin@12345` |
| Employee | `employee@learninghub.local` | `Employee@12345` |

### Test authentication APIs

Login:

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@learninghub.local","password":"Admin@12345"}'
```

Use the returned `accessToken` to call the authenticated user endpoint:

```bash
TOKEN="<accessToken-from-login-response>"

curl -s http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer ${TOKEN}"
```

### Run backend tests

```bash
mvn -f backend/pom.xml test
```

### Important configuration

The default JWT secret is only for local development. Set `JWT_SECRET` to a strong Base64-encoded
secret in shared or production environments.

## Project Status

### Version 0.1.0 - Backend Foundation Completed

Completed Features:

* Spring Boot 3
* Java 21
* PostgreSQL
* Flyway Database Migrations
* JWT Authentication
* Spring Security
* Swagger/OpenAPI
* Docker & Docker Compose
* Health Check Endpoint
* User / Role / UserRole Management
* Global Exception Handling
* Unit Tests

### Roadmap

Phase 1 ✅ Backend Foundation

Phase 2 🚀 Learning Initiatives

Phase 3 Certificate Submission

Phase 4 Leaderboard

Phase 5 Study Material Repository

Phase 6 Project Knowledge Repository

Phase 7 Global Search

Phase 8 Notifications

Phase 9 AI Features
