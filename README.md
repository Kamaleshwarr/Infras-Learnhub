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

### Run backend tests

```bash
mvn -f backend/pom.xml test
```

### Important configuration

The default JWT secret is only for local development. Set `JWT_SECRET` to a strong Base64-encoded
secret in shared or production environments.
