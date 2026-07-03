# v0.8.0 F18 — Learning Journey & Progress — Implementation Report

**Phase:** F18  
**Status:** Implemented and verified  
**Catalog version:** 1.1.1 (unchanged)  
**Flyway:** V15

## 1. Impact Analysis

F18 introduces **employee-owned learning progress** on top of the read-only F17 roadmap catalog. Each employee can enroll in a technology roadmap, complete stages in sequential order, and resume via **Continue Learning**. Progress is stored in normalized tables referencing catalog `learn_roadmap_stages` — no roadmap content is duplicated or mutated.

**Out of scope (confirmed not started):** F19 Career Paths, certifications, initiatives overlay, dashboard widgets, gamification.

## 2. Files Changed

### Backend (new)
- `V15__learn_progress.sql`
- `LearningEnrollmentStatus`, `LearnLearningEnrollment`, `LearnStageProgress`
- `LearnLearningEnrollmentRepository`, `LearnStageProgressRepository`
- `LearningProgressService`, `LearnProgressMapper`, `LearningProgressController`
- DTOs: `CreateEnrollmentRequest`, `CompleteStageRequest`, `EnrollmentResponse`, `ContinueLearningResponse`, `JourneyResponse`, `TechnologyProgressResponse`

### Backend (modified)
- `RoadmapStageResponse` — added `id` for progress API linkage
- `LearnRoadmapMapper` — maps stage UUID to response

### Frontend (new)
- `types/progress.ts`
- `components/learn/ContinueLearningCard.tsx` (+ test)

### Frontend (modified)
- `api/learnApi.ts` — progress endpoints
- `LearnHomePage.tsx` — Continue Learning section
- `RoadmapPage.tsx` — progress bar, stage states, Complete Stage
- `RoadmapStageCard.tsx` — completed/current/upcoming visuals
- `TechnologyDetailPage.tsx` — Start / Continue Learning CTAs
- `learnMessages.ts` — progress copy
- Tests updated for progress mocks

### Documentation
- `README.md`, `docs/project-roadmap.md`, `.cursor/project-context.md`, this report

## 3. Database Changes (V15)

| Table | Purpose |
|-------|---------|
| `learn_learning_enrollments` | Per-user technology enrollment, status, timestamps, current stage |
| `learn_stage_progress` | Completed stage records per enrollment |

Partial unique index prevents duplicate active enrollments per `(user_id, technology_slug)`.

## 4. APIs Added

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/learn/enrollments` | Enroll + start learning at stage 1 |
| `POST` | `/api/v1/learn/enrollments/{id}/start` | Start a NOT_STARTED enrollment |
| `POST` | `/api/v1/learn/enrollments/{id}/complete-stage` | Complete next sequential stage |
| `DELETE` | `/api/v1/learn/enrollments/{id}` | Leave enrollment (preserve history) |
| `GET` | `/api/v1/learn/journey` | Continue learning + active/completed/left lists |
| `GET` | `/api/v1/learn/progress/technologies/{technologyId}` | Roadmap progress overlay |
| `GET` | `/api/v1/learn/enrollments/technologies/{technologyId}` | Active enrollment for technology |

## 5. Business Rules

| Rule | Implementation |
|------|----------------|
| Sequential progression | Only the next incomplete stage may be completed |
| Employee-owned progress | All APIs use `@AuthenticationPrincipal`; no admin write endpoints |
| Duplicate enrollment | 409 when active enrollment exists for same technology |
| Roadmap read-only | Progress references stage IDs; catalog import unchanged |
| Status lifecycle | NOT_STARTED → IN_PROGRESS → COMPLETED; LEFT on abandon |
| BR-PR10 | No admin progress editing |

## 6. Tests Added

| Layer | Tests |
|-------|-------|
| Backend unit | `LearningProgressServiceTest` — enroll, duplicate, sequential validation, completion % |
| Backend integration | `LearningProgressIntegrationTest` (Testcontainers) |
| Frontend | `ContinueLearningCard.test.tsx`, `LearnHomePage` continue section, `RoadmapPage` progress/complete |

## 7. Automated Test Results

- **Backend (learn scope):** `LearningProgressServiceTest`, `LearnRoadmapServiceTest`, `LearnRoadmapControllerTest`, `CatalogImportServiceTest` — **PASS**
- **Frontend (learn scope):** 17 tests across `LearnHomePage`, `RoadmapPage`, `TechnologyDetailPage`, `ContinueLearningCard`, `AppRoutes` — **PASS**
- **Frontend build:** `npm run build` — **PASS**
- **Full backend suite:** 3 pre-existing failures in notification/user modules (unrelated to F18)

## 8. Backend Startup Verification

```
Started LearningHubApplication in 4.944 seconds
Imported 30 technologies for catalog 1.1.1
Imported 5 roadmaps for catalog 1.1.1
GET /api/v1/health → 200 UP
```

## 9. Flyway Verification

```
Successfully validated 15 migrations
Current version of schema "public": 15
Schema is up to date
```

## 10. Catalog Import Verification

Startup catalog import succeeded with no exceptions after V15 migration.

## 11. Frontend Startup Verification

`npm run dev` started successfully on port 5173.

## 12. End-to-End Smoke Test Results

Executed against live backend (PostgreSQL 16, employee login):

1. **Enroll** — `POST /learn/enrollments` for Spring Boot → `IN_PROGRESS`, stage 1
2. **Complete Stage** — stage 1 complete → 17% progress, current stage 2
3. **Continue Learning** — `GET /learn/journey` → `continueLearning` populated
4. **Roadmap** — stage `id` returned in roadmap response for progress linkage

## 13. Regression Results

- F16 technology list/detail/manage — unchanged APIs
- F16-R catalog import — startup import verified post-V15
- F17 roadmap viewer — extended with optional progress overlay; read-only catalog preserved

## 14. Documentation Updated

- `README.md`
- `docs/project-roadmap.md`
- `.cursor/project-context.md`
- This implementation report

## 15. Manual QA Checklist

- [ ] Employee enrolls via Technology detail **Start Learning**
- [ ] Roadmap shows progress bar and **Complete Stage** on current stage only
- [ ] Completing stage 1 advances to stage 2; cannot skip to stage 5
- [ ] Learn home shows **Continue Learning** with progress %
- [ ] Restart app — progress persists
- [ ] Admin cannot edit employee progress (no admin endpoints)
- [ ] Featured technologies and search unchanged

## 16. Risks

| Risk | Mitigation |
|------|------------|
| Catalog stage ID changes on full roadmap replace | Reimport creates new stage UUIDs; existing progress may reference deleted stages — acceptable for v1; document for catalog major updates |
| Concurrent complete requests | Transactional service; pessimistic refresh on UI after complete |

## 17. F19 Status

**F19 (Career Path Catalog) has NOT been started.**
