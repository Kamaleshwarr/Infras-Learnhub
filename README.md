# Engineering Learning Hub

Enterprise internal learning platform for engineering teams. The application combines **learning initiatives and certification workflows** with a **Learn module** that guides employees through a curated technology catalog, structured roadmaps, and personal learning progress.

**Current release focus:** v0.8.0 **Learn module v1** (F16–F18 complete; Learn v1.1 resource overrides)  
**Philosophy:** *Engineering Learning Hub owns guidance, not knowledge.*

---

## Project overview

| Area | Description |
|------|-------------|
| **Learn** | Catalog-first technology discovery, roadmap viewer, employee-owned learning progress (F16–F18) |
| **Initiatives** | Admin-managed learning initiatives with lifecycle and employee visibility (v0.7.x) |
| **Certificates** | Submit, review, approve/reject; file preview and download (v0.6.x) |
| **Platform** | Auth, users, profile, notifications, leaderboards, study materials, project knowledge |

Employees browse technologies, follow catalog roadmaps, track stage completion, and resume via **Continue Learning**. Administrators curate catalog visibility — they do not author roadmap content or edit employee progress.

---

## Architecture (high level)

```text
┌─────────────────────────────────────────────────────────────────┐
│                     React + TypeScript (MUI)                     │
│  api/ → auth/ → routes/ → pages/ → components/                  │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTPS /api/v1  (JWT Bearer)
┌────────────────────────────▼────────────────────────────────────┐
│              Spring Boot 3 — Controller → Service → Repository     │
│  learn · initiative · certificate · user · profile · notification  │
└────────────────────────────┬────────────────────────────────────┘
                             │ JDBC
┌────────────────────────────▼────────────────────────────────────┐
│                    PostgreSQL 16 (Flyway V1–V17)                   │
│  Catalog tables (read-only content) + progress overlay (F18)       │
└─────────────────────────────────────────────────────────────────┘

Startup: CatalogImportService reads backend/src/main/resources/catalog/
```

Detailed architecture: [`.cursor/architecture.md`](.cursor/architecture.md) · Learn-specific: [`docs/learn/`](docs/learn/)

---

## Technology stack

### Backend

| Technology | Version / notes |
|------------|-----------------|
| Java | 21 |
| Spring Boot | 3.x |
| Spring Security | JWT bearer authentication |
| Spring Data JPA | PostgreSQL |
| Flyway | Schema migrations V1–V17 |
| Maven | Build and test |
| OpenAPI / Swagger | API docs at `/swagger-ui.html` |
| Testcontainers | Integration tests |

### Frontend

| Technology | Version / notes |
|------------|-----------------|
| React | 18 |
| TypeScript | Strict |
| Material UI | 9.x |
| React Router | 7.x |
| Axios | HTTP client with JWT interceptor |
| Vite | Dev server and build |
| Vitest + Testing Library | Unit and component tests |

### Infrastructure

| Technology | Purpose |
|------------|---------|
| Docker Compose | PostgreSQL + backend container |
| PostgreSQL 16 | Primary database |

---

## Folder structure

```text
/
├── backend/                          # Spring Boot API
│   ├── src/main/java/.../learninghub/
│   │   ├── learn/                    # Learn module (F16–F18)
│   │   │   ├── catalog/              # Catalog import pipeline
│   │   │   ├── controller/
│   │   │   ├── domain/
│   │   │   ├── dto/
│   │   │   ├── mapper/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   ├── auth/ · initiative/ · certificate/ · user/ · profile/ · notification/ …
│   └── src/main/resources/
│       ├── catalog/                  # Platform catalog (manifest, technologies, roadmaps)
│       └── db/migration/             # Flyway SQL (V1–V15)
├── frontend/
│   └── src/
│       ├── api/learnApi.ts           # Learn HTTP client
│       ├── pages/learn/              # Learn pages
│       ├── components/learn/         # Learn UI components
│       ├── types/                    # learn.ts, roadmap.ts, progress.ts
│       └── routes/AppRoutes.tsx
├── docs/
│   ├── learn/                        # Learn module documentation (v1)
│   ├── project-roadmap.md            # Release and phase roadmap
│   ├── development-workflow.md       # Mandatory feature workflow
│   ├── testing-guide.md              # Test strategy and commands
│   ├── contributing.md                 # Contributor guide
│   └── releases/                     # Per-release reports
├── .cursor/
│   ├── architecture.md               # System architecture
│   └── project-context.md            # Agent and developer context
└── docker-compose.yml
```

---

## Quick start

### Prerequisites

- Docker and Docker Compose
- Node.js 20+ and npm (frontend)
- Java 21 and Maven (optional — backend runs in Docker)

### 1. Start backend and database

```bash
docker compose up --build
```

Backend: `http://localhost:8080`  
Health: `curl http://localhost:8080/api/v1/health`  
Swagger: `http://localhost:8080/swagger-ui.html`

On startup, Flyway applies migrations and **CatalogImportService** imports catalog v1.1.1 (30 technologies, 5 roadmaps).

### 2. Start frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend: `http://localhost:5173`

For API calls from the dev server, set the backend base URL:

```bash
VITE_API_BASE_URL=http://localhost:8080/api/v1 npm run dev
```

### Default bootstrap accounts

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@learninghub.local` | `Admin@12345` |
| Employee | `employee@learninghub.local` | `Employee@12345` |

Change or remove these before any shared or production environment.

### Login example

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"employee@learninghub.local","password":"Employee@12345"}'
```

Use the returned `accessToken` for authenticated requests:

```bash
TOKEN="<accessToken>"
curl -s http://localhost:8080/api/v1/learn/technologies?size=5 \
  -H "Authorization: Bearer ${TOKEN}"
```

---

## Flyway

Migrations live in `backend/src/main/resources/db/migration/`.

| Version | Learn-related content |
|---------|----------------------|
| V12 | `learn_technologies`, `learn_technology_project_links` |
| V13 | Catalog foundation — slug, catalog columns, `learn_catalog_imports` |
| V14 | `learn_roadmaps`, `learn_roadmap_stages`, `learn_roadmap_stage_resources` |
| V15 | `learn_learning_enrollments`, `learn_stage_progress` |

Never modify applied migrations after merge. Add new versioned files only.

---

## Catalog

Platform-owned learning content ships as JSON under `backend/src/main/resources/catalog/`:

- `manifest.json` — catalog version and package manifest
- `technologies/wave-1.json` — 30 technology records
- `roadmaps/*.json` — 5 seed roadmaps (Java, Spring Boot, React, Docker, AWS)
- `schemas/` — JSON Schema validation

Import runs on application startup. Roadmaps are **read-only** at runtime; employee progress overlays catalog stage IDs without mutating catalog data.

Full catalog documentation: [`docs/learn/catalog.md`](docs/learn/catalog.md)

---

## Running tests

### Backend

```bash
mvn -f backend/pom.xml test
```

Learn-focused:

```bash
mvn -f backend/pom.xml test -Dtest="*Learn*,*Catalog*,*LearningProgress*"
```

### Frontend

```bash
cd frontend && npm test
```

Learn-focused:

```bash
cd frontend && npm test -- --run src/pages/learn src/components/learn src/utils/roadmapEffort.test.ts
```

### Build verification

```bash
mvn -f backend/pom.xml compile -DskipTests
cd frontend && npm run build
```

See [`docs/testing-guide.md`](docs/testing-guide.md) for integration tests, Testcontainers, and regression checklists.

---

## Development workflow

Every feature must pass the **11-step completion checklist** before it is considered done:

1. Backend compile · 2. Frontend build · 3. Docker build · 4. Flyway migration · 5. Backend startup · 6. Frontend startup · 7. Automated tests · 8. E2E smoke test · 9. Regression checklist · 10. Documentation update · 11. Implementation report

Full workflow: [`docs/development-workflow.md`](docs/development-workflow.md)

Contributor guide: [`docs/contributing.md`](docs/contributing.md)

---

## Implemented modules

### Learn module v1 (v0.8.0 — F16–F18) ✓

| Phase | Deliverable |
|-------|-------------|
| **F16** | Technology discovery, search, detail, Projects cross-navigation |
| **F16-R** | Catalog foundation — manifest import, admin curation, org overrides |
| **F17** | Roadmap viewer — catalog roadmaps, stages, external resources |
| **F18** | Learning journey — enroll, sequential stage completion, Continue Learning |
| **Learn v1.1** | Resource override management — org URL overrides, disable/restore, org-only resources |

Routes: `/learn`, `/learn/technologies`, `/learn/technologies/:id`, `/learn/technologies/:id/roadmap`, `/learn/manage` (admin). Admins manage per-stage resource overrides from the roadmap page.

### Platform modules (shipped)

- Authentication and password management (v0.2)
- User management UI — list, CRUD, bulk import (v0.3–v0.4)
- Profile management — view, edit, avatar (v0.5)
- Notifications — bell, inbox, certificate producers (v0.6)
- Certificate workflow — submit, review, preview/download (v0.6.1–v0.6.2)
- Initiatives experience and management (v0.7.0–v0.7.1)

---

## Upcoming roadmap

| Phase | Deliverable | Status |
|-------|-------------|--------|
| **Project Module** | Engineering project portal (P1–P5) | **P1 + P2 + P3 complete** — Knowledge Base, Environments, Repositories; see [`docs/project/`](docs/project/) |
| F19 | Career Path Catalog | Not started |
| F20 | Certification Catalog | Not started |
| F21 | Optional Initiative Association | Not started |
| F22 | Dashboard, Unified Search & Release | Not started |

Authoritative roadmap: [`docs/project-roadmap.md`](docs/project-roadmap.md)

---

## Screenshots

| Feature | Location |
|---------|----------|
| F17 Featured technologies | `docs/screenshots/f17-featured-section/` |
| F18 Roadmap UI polish | `docs/screenshots/f18-roadmap-ui-polish/` |
| F18 Roadmap hero redesign | `docs/screenshots/f18-roadmap-hero-redesign/` |

---

## Documentation index

| Topic | Location |
|-------|----------|
| Project roadmap | [`docs/project-roadmap.md`](docs/project-roadmap.md) |
| Architecture | [`.cursor/architecture.md`](.cursor/architecture.md) |
| Learn module docs | [`docs/learn/README.md`](docs/learn/README.md) |
| Learn database ER | [`docs/learn/database-overview.md`](docs/learn/database-overview.md) |
| Learn API reference | [`docs/learn/api-reference.md`](docs/learn/api-reference.md) |
| Catalog specification | [`docs/learn/catalog.md`](docs/learn/catalog.md) |
| Contributor guide | [`docs/contributing.md`](docs/contributing.md) |
| Testing guide | [`docs/testing-guide.md`](docs/testing-guide.md) |
| Development workflow | [`docs/development-workflow.md`](docs/development-workflow.md) |
| Project Module (planning) | [`docs/project/README.md`](docs/project/README.md) |
| v0.8.0 product design | [`docs/v0.8.0/`](docs/v0.8.0/) |
| Release reports | [`docs/releases/`](docs/releases/) |

---

## Configuration

| Variable | Purpose |
|----------|---------|
| `JWT_SECRET` | Base64-encoded JWT signing secret (required in shared environments) |
| `SPRING_DATASOURCE_*` | Database connection (set in `docker-compose.yml` for local) |
| `VITE_API_BASE_URL` | Frontend API base (default `/api/v1`; use `http://localhost:8080/api/v1` for local dev) |
| `app.catalog.import.enabled` | Enable startup catalog import (default `true`) |

The default JWT secret is for local development only.
