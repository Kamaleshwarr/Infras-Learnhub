# Engineering Learning Hub

Enterprise-grade internal web application for managing learning initiatives, certification
programs, study materials, project knowledge, KT documents, leaderboards, user administration,
and in-app notifications.

**Current release:** [v0.6.2](docs/releases/release-v0.6.2.md) — Certificate Preview, Download & Pending Reviews Drilldown (shipped)

## Quick start

Run the backend and PostgreSQL with Docker Compose:

```bash
docker compose up --build
```

Run the frontend dev server (separate terminal):

```bash
cd frontend
npm install
npm run dev
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

## Technology stack

### Backend

- Java 21
- Spring Boot 3
- Maven
- PostgreSQL
- Flyway
- Spring Security with JWT
- OpenAPI / Swagger

### Frontend

- React 18
- TypeScript
- Material UI
- React Router
- Axios
- Vitest / Testing Library

## Run tests

Backend:

```bash
mvn -f backend/pom.xml test
```

Frontend:

```bash
cd frontend && npm test
```

## Important configuration

The default JWT secret is only for local development. Set `JWT_SECRET` to a strong Base64-encoded
secret in shared or production environments.

## Project status (v0.6.2)

High-level snapshot. Detailed release notes, validation history, and backlog live under `docs/`.

### Shipped platform modules (backend)

- Authentication & password management
- Learning initiatives
- Certificate submissions (submit, approve, reject, **file streaming**)
- Leaderboards
- Study materials repository
- Project knowledge repository
- User management (API + bulk import)
- Profile management (including avatar storage)
- In-app notifications (persistence, inbox APIs, certificate producers)

### Shipped frontend modules

- Authentication, role-aware dashboard, password management
- User management UI — list, CRUD, activate/deactivate, reset password, bulk import (v0.3–v0.4)
- Profile management UI — view, edit, change-password entry, avatar upload (v0.5)
- Notifications UI — bell, dropdown, inbox page, badge sync (v0.6)
- Certificate workflow UI — Submit Certificate (`/submissions/new`), My Submissions
  (`/submissions`), Admin Review (`/submissions/review`) with notification E2E validation (v0.6.1)
- **Certificate review enhancements** — admin preview/download in review queue; Pending Reviews
  dashboard drilldown to `/submissions/review` (v0.6.2)

### Recent release highlights (v0.6.2)

- Admin previews and downloads submitted certificates (PDF, PNG, JPEG) before approve/reject
- Admin Dashboard **Pending Reviews** metric links directly to Certificate Review queue
- Backend `GET /submissions/{id}/certificate?disposition=inline|attachment`

### Post-v0.6.2 backlog (summary)

- Global search
- Employee self-service certificate download (My Submissions)
- Dashboard drilldowns for other metrics; status chips and filtering
- Email notification channel (account lifecycle)
- Initiative / study materials / projects full UI surfaces
- AI features

## Documentation

| Topic | Location |
| --- | --- |
| **Roadmap & release overview** | [`docs/project-roadmap.md`](docs/project-roadmap.md) |
| **v0.6.2 release notes** | [`docs/releases/release-v0.6.2.md`](docs/releases/release-v0.6.2.md) |
| **v0.6.1 release notes** | [`docs/releases/release-v0.6.1.md`](docs/releases/release-v0.6.1.md) |
| **Testing & defect history** | [`docs/testing-and-defect-history.md`](docs/testing-and-defect-history.md) |
| **Backend architecture** | [`docs/backend-architecture-and-roadmap.md`](docs/backend-architecture-and-roadmap.md) |
| **Prior releases** | [`docs/releases/`](docs/releases/) |

The README is an entry point for setup and orientation. Authoritative project status, roadmap
detail, validation results, and per-release scope are maintained in `docs/` and updated with each
release.
