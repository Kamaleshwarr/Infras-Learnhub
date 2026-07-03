# Contributor Guide

Guide for developers contributing to Engineering Learning Hub, with emphasis on the **Learn module v1** (F16–F18).

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21 |
| Maven | 3.9+ |
| Node.js | 20+ |
| Docker | Latest stable |
| Git | Latest |

---

## Clone and first run

```bash
git clone https://github.com/Kamaleshwarr/Infras-Learnhub.git
cd Infras-Learnhub

# Backend + PostgreSQL
docker compose up --build

# Frontend (separate terminal)
cd frontend
npm install
VITE_API_BASE_URL=http://localhost:8080/api/v1 npm run dev
```

Login: `employee@learninghub.local` / `Employee@12345`

---

## Project conventions

- **Backend:** Controller → Service → Repository → Domain (see `.cursor/architecture.md`)
- **Frontend:** `api/` → `pages/` → `components/` → `types/`
- **Migrations:** Sequential Flyway files only — never edit applied migrations
- **Catalog:** JSON in `backend/src/main/resources/catalog/` — validate before commit
- **Tests:** Required for business rules; run before PR
- **Workflow:** Follow the [11-step development workflow](development-workflow.md)

Read `.cursor/engineering-standards.md` before starting feature work.

---

## Add a technology to the catalog

1. Edit `backend/src/main/resources/catalog/technologies/wave-1.json` (or create a new wave file and update manifest).
2. Add a record with required fields per `catalog/schemas/technology.schema.json`:
   - `slug` (unique, lowercase, hyphenated)
   - `name`, `shortName`, `description`
   - `category`, `difficulty`, `featured`, `tags`
3. Validate JSON against schema (import will validate on startup).
4. Bump `catalogVersion` in `manifest.json` if shipping a new catalog release.
5. Start backend — `CatalogImportService` upserts by slug.
6. Verify: `GET /api/v1/learn/technologies?search=<slug>`
7. Admin: publish via `/learn/manage` if status is `HIDDEN`.

**Do not** create technologies via admin UI — catalog import is the source of truth.

---

## Add a roadmap

1. Create `backend/src/main/resources/catalog/roadmaps/<slug>.json`.
2. Validate against `catalog/schemas/roadmap.schema.json`.
3. Ensure `technologySlug` matches an existing catalog technology.
4. Define ordered `stages[]` with `learningResources` and `practiceResources`.
5. Bump roadmap package version in manifest if needed.
6. Restart backend — import upserts roadmap, stages, and resources.
7. Verify: `GET /api/v1/learn/technologies/id/{id}/roadmap`

**Rules:**
- Stage `order` starts at 1 and is sequential
- Each stage needs at least one resource (learning or practice)
- Resource `url` must be a valid external link

---

## Update catalog version

1. Make JSON changes in `catalog/`.
2. Increment `catalogVersion` in `manifest.json`.
3. Update package `version` fields if package content changed materially.
4. Run backend startup — confirm Flyway unchanged, import succeeds.
5. Check `GET /api/v1/learn/manage/catalog/status` (admin).
6. Update documentation if API behavior or record counts change.

---

## Create a Flyway migration

1. Determine next version: check `backend/src/main/resources/db/migration/` (current: V15).
2. Create `V16__descriptive_name.sql`.
3. Use `TIMESTAMPTZ` for timestamps, UUID for PKs, explicit FK constraints.
4. Never modify V1–V15 after merge.
5. Test:

```bash
docker compose down -v   # fresh DB (local only)
docker compose up --build
```

6. Verify: logs show `Successfully applied N migrations` and no startup errors.

---

## Run locally (without Docker backend)

```bash
# PostgreSQL only
docker compose up postgres -d

# Backend
cd backend
mvn spring-boot:run \
  -Dspring.datasource.url=jdbc:postgresql://localhost:5432/learning_hub \
  -Dspring.datasource.username=learning_hub \
  -Dspring.datasource.password=learning_hub

# Frontend
cd frontend
VITE_API_BASE_URL=http://localhost:8080/api/v1 npm run dev
```

---

## Run with Docker

```bash
docker compose up --build
```

Services:
- PostgreSQL: `localhost:5432`
- Backend: `localhost:8080`
- Frontend: run separately on `localhost:5173`

---

## Run tests

### Backend (all)

```bash
mvn -f backend/pom.xml test
```

### Backend (Learn scope)

```bash
mvn -f backend/pom.xml test \
  -Dtest="LearnTechnologyServiceTest,LearnRoadmapServiceTest,LearningProgressServiceTest,CatalogImportServiceTest,LearnTechnologyControllerTest,LearnRoadmapControllerTest,LearningProgressIntegrationTest"
```

### Frontend (all)

```bash
cd frontend && npm test
```

### Frontend (Learn scope)

```bash
cd frontend && npm test -- --run src/pages/learn src/components/learn src/utils/roadmapEffort.test.ts
```

### Build verification

```bash
mvn -f backend/pom.xml compile -DskipTests
cd frontend && npm run build
```

---

## Learn module code map

| Concern | Backend | Frontend |
|---------|---------|----------|
| Technologies | `LearnTechnologyService` | `TechnologyListPage`, `TechnologyDetailPage` |
| Roadmaps | `LearnRoadmapService` | `RoadmapPage` |
| Progress | `LearningProgressService` | `ContinueLearningCard`, progress APIs |
| Catalog import | `CatalogImportService` | Admin catalog status |
| Admin curation | `LearnTechnologyManageController` | `TechnologyCurationPanel` |

---

## Pull request checklist

- [ ] No unrelated changes
- [ ] Backend compile passes
- [ ] Frontend build passes
- [ ] Tests pass (scope + regression)
- [ ] Flyway migration tested (if applicable)
- [ ] Startup + smoke test completed
- [ ] Documentation updated
- [ ] Implementation report (for features)

See [development-workflow.md](development-workflow.md) for the full 11-step process.

---

## Get help

| Topic | Document |
|-------|----------|
| Architecture | `.cursor/architecture.md` |
| Learn APIs | `docs/learn/api-reference.md` |
| Database | `docs/learn/database-overview.md` |
| Catalog | `docs/learn/catalog.md` |
| Testing | `docs/testing-guide.md` |
| Roadmap | `docs/project-roadmap.md` |
