# Development Workflow

Mandatory workflow for every feature phase in Engineering Learning Hub.

**A feature is NOT complete until all 11 steps pass.**

---

## The 11-step completion checklist

| Step | Verification | Command / action |
|------|--------------|----------------|
| **1. Backend compile** | Java compiles without errors | `mvn -f backend/pom.xml compile -DskipTests` |
| **2. Frontend build** | TypeScript + Vite production build | `cd frontend && npm run build` |
| **3. Docker build** | Container image builds | `docker compose build` |
| **4. Flyway migration** | Migrations apply on fresh or existing DB | `docker compose up` — check logs for `Schema is up to date` |
| **5. Backend startup** | Application starts, no exceptions | `curl http://localhost:8080/api/v1/health` → `UP` |
| **6. Frontend startup** | Dev server runs | `cd frontend && npm run dev` → `http://localhost:5173` |
| **7. Automated tests** | Backend + frontend tests pass | See [testing-guide.md](testing-guide.md) |
| **8. End-to-end smoke test** | Exercise the feature in running app | Manual or scripted (see below) |
| **9. Regression checklist** | Prior features still work | See [testing-guide.md#regression-checklist](testing-guide.md#regression-checklist) |
| **10. Documentation update** | README, roadmap, architecture, affected docs | This step |
| **11. Final implementation report** | Release report in `docs/releases/` | 17-section report for major phases |

---

## Step details

### 1. Backend compile

```bash
mvn -f backend/pom.xml compile -DskipTests
```

Fix all compilation errors before proceeding.

### 2. Frontend build

```bash
cd frontend && npm run build
```

Resolves TypeScript errors and validates production bundle.

### 3. Docker build

```bash
docker compose build
```

Ensures Dockerfile and dependencies are current.

### 4. Flyway migration

When adding schema changes:

1. Create `V{N}__description.sql` in `backend/src/main/resources/db/migration/`
2. Never modify applied migrations
3. Start application and verify:

```text
Successfully validated N migrations
Current version of schema "public": N
```

For the current schema, Flyway version is **V17** (`projects.status`).

### 5. Backend startup

```bash
docker compose up
```

Verify:
- Flyway succeeds
- Catalog import succeeds (if Learn): `Imported 30 technologies`, `Imported 5 roadmaps`
- Health endpoint: `{"status":"UP"}`

### 6. Frontend startup

```bash
cd frontend
VITE_API_BASE_URL=http://localhost:8080/api/v1 npm run dev
```

Open `http://localhost:5173` — no console errors on login.

### 7. Automated tests

**Backend (feature scope):**

```bash
mvn -f backend/pom.xml test -Dtest="<RelevantTestClasses>"
```

**Frontend (feature scope):**

```bash
cd frontend && npm test -- --run <relevant paths>
```

Record pass/fail counts in implementation report.

### 8. End-to-end smoke test

Exercise the full feature path in the running application.

**Learn module example (F18):**

1. Login as employee
2. Enroll in a technology
3. Verify Continue Learning on home
4. Open roadmap — progress hero visible
5. Complete a stage — progress updates
6. Restart application — progress persists

**Scripted option:**

```bash
cd frontend && node scripts/roadmap-ui-smoke.mjs
```

### 9. Regression checklist

Run regression checks for all previously shipped modules affected by the change. For Learn changes, verify F16, F16-R, F17, F18, and core platform (auth, initiatives).

Full checklist: [testing-guide.md](testing-guide.md)

### 10. Documentation update

Minimum updates per feature:

| Document | When to update |
|----------|----------------|
| `README.md` | Setup, modules, or stack changes |
| `docs/project-roadmap.md` | Phase status changes |
| `.cursor/project-context.md` | Architecture or phase completion |
| `.cursor/architecture.md` | New modules, APIs, or patterns |
| `docs/learn/*` | Learn-specific changes |
| `docs/contributing.md` | New contributor workflows |

### 11. Final implementation report

For major phases, create `docs/releases/release-v{version}-{phase}-implementation-report.md` with:

1. Impact analysis
2. Files changed
3. Database changes
4. APIs added
5. Business rules
6. Tests added
7. Automated test results
8. Backend startup verification
9. Flyway verification
10. Catalog import verification
11. Frontend startup verification
12. E2E smoke test results
13. Regression results
14. Documentation updated
15. Manual QA checklist
16. Risks
17. Confirmation next phase not started

---

## Branch and PR workflow

1. Create branch: `cursor/<descriptive-name>-59d6`
2. Implement feature following engineering standards
3. Complete all 11 steps
4. Commit with descriptive messages
5. Push: `git push -u origin <branch>`
6. Open PR against `main`
7. Include implementation report in PR description

---

## Learn module v1 — completed phases

| Phase | Report | Status |
|-------|--------|--------|
| F16 | `release-v0.8.0-f16-implementation-report.md` | ✓ Complete |
| F16-R | `release-v0.8.0-f16-r-implementation-report.md` | ✓ Complete |
| F17 | `release-v0.8.0-f17-implementation-report.md` | ✓ Complete |
| F18 | `release-v0.8.0-f18-implementation-report.md` | ✓ Complete |

**F19 has NOT been started.** Do not begin F19 without explicit approval.

---

## Quick reference commands

```bash
# Full local stack
docker compose up --build
cd frontend && VITE_API_BASE_URL=http://localhost:8080/api/v1 npm run dev

# Verify
curl http://localhost:8080/api/v1/health
mvn -f backend/pom.xml test -Dtest="*Learn*"
cd frontend && npm test -- --run src/pages/learn && npm run build
```
