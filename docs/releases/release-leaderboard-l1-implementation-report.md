# Leaderboard L1 Implementation Report

**Phase:** L1 — Global + Initiative Leaderboards (certification-based)  
**Date:** 2026-07-10  
**Branch:** `cursor/leaderboard-l1-81ad`  
**Base commit:** `9c7b28c` (includes architecture review docs from PR #60)  
**Status:** Implementation complete — awaiting manual QA approval

---

## 1. Impact analysis

| Area | Impact |
|------|--------|
| Backend | Initiative visibility enforcement on initiative leaderboard endpoint |
| Frontend | Replaced placeholder leaderboard pages with functional UI |
| Database | **None** — no migration |
| Dashboard | Label alignment + navigation links to global leaderboard |
| Initiative Detail | View Leaderboard action on TopLearnerCard |
| APIs | Initiative leaderboard now requires `AuthenticatedUser`; OpenAPI ranking docs clarified |

## 2. Ranking semantic decision

**Chosen: Option A — Unique sequential positions using `ROW_NUMBER()` with deterministic tie-breakers.**

Production SQL already used `ROW_NUMBER()`, not `RANK()`. L1 preserves this behavior and documents it correctly in `docs/leaderboard/l1-ranking-rules.md`.

## 3. Files changed

### Backend
- `leaderboard/service/LeaderboardService.java` — initiative visibility check
- `leaderboard/controller/LeaderboardController.java` — pass principal, OpenAPI text
- `leaderboard/service/LeaderboardServiceTest.java` — visibility tests
- `leaderboard/service/LeaderboardMethodSecurityTest.java` — updated wiring
- `leaderboard/controller/LeaderboardIntegrationTest.java` — **new** integration tests

### Frontend
- `types/leaderboards.ts` — **new** aligned types
- `api/leaderboardsApi.ts` — type alignment
- `api/dashboardApi.ts` — type alignment
- `components/leaderboards/*` — **new** shared components
- `components/initiatives/TopLearnerCard.tsx` — View Leaderboard link
- `pages/leaderboards/GlobalLeaderboardPage.tsx` — full implementation
- `pages/leaderboards/InitiativeLeaderboardPage.tsx` — full implementation
- `pages/dashboard/DashboardPage.tsx` — label/link alignment
- `pages/initiatives/InitiativeDetailPage.tsx` — pass initiativeId to TopLearnerCard
- `routes/AppRoutes.tsx` — initiative picker route
- Tests + `scripts/leaderboard-l1-smoke.mjs`

### Documentation
- `docs/leaderboard/l1-ranking-rules.md` — **new**
- `docs/leaderboard/README.md`, `architecture-review.md` — L1 status updates
- `docs/releases/release-leaderboard-l1-implementation-report.md` — this file
- `README.md`, `docs/project-roadmap.md`, `.cursor/architecture.md`, `.cursor/project-context.md`

## 4. Database changes

**None.** No Flyway migration. Rankings remain dynamic SQL over `certificate_submissions`.

## 5. Automated test results

### Backend unit tests (pass)
```
LeaderboardServiceTest
LeaderboardMethodSecurityTest
LeaderboardControllerTest
LeaderboardQueryRepositoryTest
```

### Backend integration tests
`LeaderboardIntegrationTest` — requires Docker/Testcontainers. **Not executed in cloud agent environment** (no Docker socket). Tests are included and will run in CI/local with Docker.

### Frontend tests (pass)
```
leaderboardsApi.test.ts
GlobalLeaderboardPage.test.tsx
InitiativeLeaderboardPage.test.tsx
TopLearnerCard.test.tsx
DashboardPage.test.tsx (existing)
InitiativeDetailPage.test.tsx (updated)
```

### Build verification
- `mvn -f backend/pom.xml compile -DskipTests` — **PASS**
- `cd frontend && npm run build` — **PASS**

## 6. E2E smoke

Script: `frontend/scripts/leaderboard-l1-smoke.mjs`

**Not executed in cloud agent environment** — requires running backend (`:8080`) and frontend (`:5173`). Run locally:

```bash
docker compose up --build
cd frontend && VITE_API_BASE_URL=http://localhost:8080/api/v1 npm run dev
# separate terminal:
cd frontend && API_URL=http://localhost:8080/api/v1 APP_URL=http://localhost:5173 node scripts/leaderboard-l1-smoke.mjs
```

## 7. Deferred (not implemented)

- Learn stage/roadmap points
- Score ledger / `leaderboard_score_events`
- Monthly/weekly/quarterly periods
- Manager contribution points
- Project activity scoring
- Gamification (badges, streaks, XP, coins)
- Email, AI Assistant, Learn v2

## 8. Manual QA checklist

- [ ] Global leaderboard loads with top 3, my rank strip, table, pagination
- [ ] Initiative leaderboard loads via tab + initiative selector
- [ ] Initiative Detail → View Leaderboard navigation
- [ ] Dashboard preview links to global leaderboard
- [ ] Employee receives 404 for draft initiative leaderboard
- [ ] Admin can view draft initiative leaderboard
- [ ] Pending/rejected certs do not affect ranking
- [ ] Responsive layouts at 1280px / 834px / 390px
- [ ] Restart backend — rankings persist

## 9. Confirmation

- **L1 only** — certification-based ranking
- No Learn points, no score ledger, no migration
- No gamification expansion
- Email not started · AI Assistant not started · Learn v2 not started
