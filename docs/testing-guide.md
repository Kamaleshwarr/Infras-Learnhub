# Testing Guide

Testing strategy and commands for Engineering Learning Hub, including the Learn module v1.

---

## Test pyramid

```text
                    ┌─────────────┐
                    │  Manual QA  │
                    └──────┬──────┘
               ┌────────────┴────────────┐
               │  Integration (Testcontainers) │
               └────────────┬────────────┘
          ┌──────────────────┴──────────────────┐
          │  Controller + Service unit tests     │
          └──────────────────┬──────────────────┘
     ┌───────────────────────┴───────────────────────┐
     │  Frontend component + page tests (Vitest)      │
     └───────────────────────────────────────────────┘
```

---

## Backend tests

**Location:** `backend/src/test/java/`  
**Runner:** JUnit 5 + Mockito + Spring Boot Test

### Run all backend tests

```bash
mvn -f backend/pom.xml test
```

### Run Learn module tests

```bash
mvn -f backend/pom.xml test \
  -Dtest="LearnTechnologyServiceTest,LearnRoadmapServiceTest,LearningProgressServiceTest,CatalogImportServiceTest,LearnTechnologyControllerTest,LearnRoadmapControllerTest,LearningProgressIntegrationTest"
```

### Test types

| Type | Location pattern | Purpose |
|------|------------------|---------|
| Service unit | `*ServiceTest.java` | Business rules, validation |
| Controller unit | `*ControllerTest.java` | API contract, status codes |
| Integration | `*IntegrationTest.java` | Full stack with real PostgreSQL |

### Learn module test coverage

| Test class | Covers |
|------------|--------|
| `LearnTechnologyServiceTest` | Search, filters, visibility |
| `LearnRoadmapServiceTest` | Roadmap loading, stage ordering |
| `LearningProgressServiceTest` | Enroll, sequential completion, duplicate |
| `CatalogImportServiceTest` | Import idempotency, validation |
| `LearnTechnologyControllerTest` | Technology API contract |
| `LearnRoadmapControllerTest` | Roadmap API contract |
| `LearningProgressIntegrationTest` | Progress E2E with Testcontainers |

---

## Integration tests (Testcontainers)

**Example:** `LearningProgressIntegrationTest`

- Spins up PostgreSQL container
- Runs Flyway migrations
- Tests enrollment and stage completion against real DB

**Requirements:** Docker running locally

```bash
mvn -f backend/pom.xml test -Dtest=LearningProgressIntegrationTest
```

---

## Frontend tests

**Location:** `frontend/src/**/*.test.tsx`  
**Runner:** Vitest + Testing Library + jsdom

### Run all frontend tests

```bash
cd frontend && npm test
```

### Run Learn module tests

```bash
cd frontend && npm test -- --run \
  src/pages/learn \
  src/components/learn \
  src/utils/roadmapEffort.test.ts \
  src/routes/AppRoutes.test.tsx
```

### Learn frontend test files

| File | Covers |
|------|--------|
| `LearnHomePage.test.tsx` | Featured section, Continue Learning |
| `TechnologyDetailPage.test.tsx` | Detail, enroll CTA |
| `RoadmapPage.test.tsx` | Hero, progress, Complete Stage |
| `ContinueLearningCard.test.tsx` | Journey card |
| `FeaturedTechnologyCard.test.tsx` | Featured tiles |
| `roadmapEffort.test.ts` | Effort display formatting |
| `AppRoutes.test.tsx` | Learn route rendering |

### Frontend test patterns

- Mock `learnApi` with `vi.mock`
- Use `MemoryRouter` for route tests
- `userEvent` for button clicks
- `findByRole` / `findByText` for async UI

---

## Build verification

Always run before declaring a feature complete:

```bash
mvn -f backend/pom.xml compile -DskipTests
cd frontend && npm run build
```

---

## Manual QA

### Learn module smoke test (employee)

1. Login as `employee@learninghub.local`
2. Navigate to `/learn` — featured technologies and search visible
3. Open a technology — detail page loads
4. Click **Start Learning** or open roadmap
5. Roadmap hero shows progress (if enrolled)
6. Click **Complete Stage** on current stage — progress updates
7. Learn home shows **Continue Learning**
8. Restart app — progress persists

### Learn module smoke test (admin)

1. Login as `admin@learninghub.local`
2. Navigate to `/learn/manage`
3. Catalog status shows version `1.1.1`, 30 technologies, 5 roadmaps
4. Publish/hide/archive a technology
5. Link/unlink organizational project

### API smoke (curl)

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"employee@learninghub.local","password":"Employee@12345"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")

curl -s http://localhost:8080/api/v1/learn/journey -H "Authorization: Bearer $TOKEN"
curl -s "http://localhost:8080/api/v1/learn/technologies?size=3" -H "Authorization: Bearer $TOKEN"
```

### UI smoke script

```bash
cd frontend && node scripts/roadmap-ui-smoke.mjs
```

---

## Regression checklist

After Learn module changes, verify prior platform functionality:

| Area | Check |
|------|-------|
| **F16** | Technology list, search, filters, detail, project links |
| **F16-R** | Catalog import on startup, admin curation, catalog status |
| **F17** | Roadmap viewer, stage resources, read-only catalog |
| **F18** | Enroll, Continue Learning, sequential completion, journey API |
| **Auth** | Login, logout, password change |
| **Initiatives** | List, detail, submit certificate link |
| **Certificates** | Submit, review, preview |
| **Notifications** | Bell badge, inbox |
| **Profile** | View, edit, avatar |

---

## Known test limitations

- Full backend suite may include pre-existing failures in notification/user modules unrelated to Learn
- Frontend dev server requires `VITE_API_BASE_URL` for live API smoke tests
- Testcontainers tests require Docker daemon

---

## CI recommendations

Minimum pipeline for Learn changes:

```bash
mvn -f backend/pom.xml test -Dtest="*Learn*,*Catalog*,*LearningProgress*"
cd frontend && npm test -- --run src/pages/learn src/components/learn
cd frontend && npm run build
```

See [development-workflow.md](development-workflow.md) for the complete 11-step gate.
