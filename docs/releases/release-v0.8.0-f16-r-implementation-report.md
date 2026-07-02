# v0.8.0 F16-R — Catalog Foundation Refactor — Implementation Report

**Phase:** F16-R  
**Branch:** `cursor/f16-r-catalog-foundation-59d6`  
**Status:** Implementation complete — ready for manual QA  
**F17:** Not started

---

## 1. Impact Analysis

F16-R refactors the Learn module from administrator-authored Technology CRUD to a **catalog-first, curation-only** model aligned with the frozen Learning Navigation Platform architecture (v2.1).

**Employee impact:** None functionally. Browse, search, filters, detail pages, and project cross-navigation behave as in F16. Published catalog technologies remain visible; hidden/archived technologies remain inaccessible.

**Administrator impact:** Create/Edit metadata authoring removed. Admins now curate catalog-imported technologies: feature, publish, hide, archive, organization notes, and project links. Catalog version and import counts visible on Manage Technologies.

**Platform impact:** ~30 technologies imported from `catalog/technologies/wave-1.json` on startup. Idempotent reimport preserves organization overrides (`featured_override`, `status`, `org_notes`, project links).

No F17+ scope implemented (no roadmaps, progress, career paths, certifications, initiatives, dashboard, unified search, or AI).

---

## 2. Files Changed

### Backend (new)

| Path | Purpose |
|------|---------|
| `backend/src/main/resources/db/migration/V13__learn_catalog_foundation.sql` | Catalog columns, slug, import tracking, status migration |
| `backend/src/main/resources/catalog/manifest.json` | Catalog version manifest |
| `backend/src/main/resources/catalog/schemas/technology.schema.json` | Technology JSON schema |
| `backend/src/main/resources/catalog/technologies/wave-1.json` | Wave 1 catalog (30 technologies) |
| `backend/src/main/java/com/company/learninghub/learn/catalog/*` | Import service, schema validator, properties, DTOs |
| `backend/src/main/java/com/company/learninghub/learn/domain/LearnCatalogImport.java` | Import tracking entity |
| `backend/src/main/java/com/company/learninghub/learn/domain/CatalogImportStatus.java` | Import status enum |
| `backend/src/main/java/com/company/learninghub/learn/repository/LearnCatalogImportRepository.java` | Import repository |
| `backend/src/main/java/com/company/learninghub/learn/dto/TechnologyCurationRequest.java` | Curation PATCH body |
| `backend/src/main/java/com/company/learninghub/learn/dto/CatalogImportStatusResponse.java` | Catalog status DTO |
| `backend/src/main/java/com/company/learninghub/learn/service/LearnCatalogService.java` | Catalog status query |
| `backend/src/main/java/com/company/learninghub/learn/controller/LearnCatalogManageController.java` | `GET .../manage/catalog/status` |
| `backend/src/test/resources/application.yml` | Disables catalog import in unit tests |
| `backend/src/test/java/com/company/learninghub/learn/catalog/CatalogImportServiceTest.java` | Import lifecycle tests |

### Backend (modified)

| Path | Change |
|------|--------|
| `LearnTechnology.java` | Catalog fields, slug, featured override resolution |
| `TechnologyCategory.java` | 11 catalog categories |
| `TechnologyStatus.java` | `HIDDEN` replaces `DRAFT` |
| `LearnTechnologyRepository.java` | Slug lookup; removed name uniqueness |
| `LearnTechnologyService.java` | Removed create/update; added curation, hide |
| `LearnTechnologyManageController.java` | Curation endpoints; removed POST/PUT |
| `LearnTechnologyMapper.java` | Extended response mapping |
| `TechnologyResponse.java` | Catalog + curation fields |
| `pom.xml` | `json-schema-validator` dependency |
| `application.yml` | `app.catalog.import` configuration |
| `LearningHubApplication.java` | `CatalogImportProperties` registration |
| Learn tests | Updated for catalog-first model |

### Backend (removed)

| Path | Reason |
|------|--------|
| `TechnologyCreateRequest.java` | Admin authoring removed |
| `TechnologyUpdateRequest.java` | Admin authoring removed |

### Frontend (new)

| Path | Purpose |
|------|---------|
| `frontend/src/components/learn/TechnologyCurationPanel.tsx` | Read-only metadata + curation controls |

### Frontend (modified)

| Path | Change |
|------|--------|
| `frontend/src/types/learn.ts` | Catalog categories, `HIDDEN` status, curation types |
| `frontend/src/api/learnApi.ts` | Curation/hide/catalog status APIs |
| `TechnologyListPage.tsx` | Curation panel; catalog status toolbar |
| `TechnologyDetailPage.tsx` | Curation panel; official links |
| `TechnologyLifecycleActions.tsx` | Hide action; `HIDDEN` publish |
| `TechnologyStatusChip.tsx` | `HIDDEN` label |
| `TechnologyListToolbar.tsx` | Catalog version display |
| `TechnologyListViews.tsx` | Curate action |
| `learnListParams.ts` / tests | New categories and status |
| `learnMessages.ts` | Curation messaging |

### Frontend (removed)

| Path | Reason |
|------|--------|
| `CreateTechnologyDialog.tsx` | Admin authoring removed |
| `EditTechnologyDialog.tsx` | Replaced by curation panel |
| `TechnologyFormFields.tsx` | Metadata authoring removed |
| `learnFormState.ts` / test | Metadata authoring removed |

### Documentation

| Path | Change |
|------|--------|
| `.cursor/project-context.md` | F16-R shipped context |
| `docs/project-roadmap.md` | F16-R status |
| `docs/releases/release-v0.8.0-f16-r-implementation-report.md` | This report |

---

## 3. Catalog Architecture Implemented

```text
backend/src/main/resources/catalog/
├── manifest.json                    # catalogVersion 1.0.0
├── schemas/
│   └── technology.schema.json       # CI/startup validation
└── technologies/
    └── wave-1.json                  # 30 technologies, 11 categories
```

**Import lifecycle (per frozen spec §3):**

1. Application startup → `CatalogImportService` (conditional on `app.catalog.import.enabled`)
2. Read `manifest.json` → compare `catalogVersion` with `learn_catalog_imports`
3. Same version → no-op
4. New version → validate JSON schema → upsert by `slug` → preserve org overrides → record import
5. Removed catalog entries → soft-hide (`catalog_present = false`) when org curation exists

**Override resolution:**

| Field | Owner | Preserved on reimport |
|-------|-------|----------------------|
| `catalog_featured` | Catalog | Updated unless `featured_override` set |
| `featured_override` | Organization | Yes |
| `status` | Organization | Yes |
| `org_notes` | Organization | Yes |
| Project links | Organization | Never touched |

---

## 4. Business Rules

| Rule | Implementation |
|------|----------------|
| BR-LC01 | Status `HIDDEN` / `PUBLISHED` / `ARCHIVED` |
| BR-LC02 | Employees 404 on non-published technologies |
| BR-LC03 | Admin cannot create/edit catalog metadata via API or UI |
| BR-LC04 | Admin can feature (override), publish, hide, archive |
| BR-LC05 | Admin can set organization notes (max 2000 chars) |
| BR-LC06 | Project links manageable via curation panel |
| BR-LC07 | Catalog import idempotent by `catalogVersion` |
| BR-LC08 | Org overrides preserved on catalog refresh |
| BR-LR01 | HTTPS URLs enforced in catalog and import validation |
| BR-CAT01 | First import sets `status = HIDDEN` for new technologies |
| BR-CAT02 | Slug is stable business key for upsert |

**Status transitions:**

```text
HIDDEN → PUBLISHED (publish)
PUBLISHED → HIDDEN (hide)
PUBLISHED → ARCHIVED (archive)
```

---

## 5. Tests

### Backend (learn package)

| Test class | Coverage |
|------------|----------|
| `LearnTechnologyServiceTest` | Curation, publish, hide, archive, project links, employee visibility |
| `LearnTechnologyMethodSecurityTest` | Admin-only curation and list |
| `LearnTechnologyControllerTest` | Employee list/detail wrapping |
| `TechnologyRequestValidationTest` | Curation request validation |
| `CatalogImportServiceTest` | Idempotent skip, override preservation, duplicate slug rejection |

### Frontend

| Test file | Coverage |
|-----------|----------|
| `learnListParams.test.ts` | `HIDDEN` status, `BACKEND` category, admin filter params |

---

## 6. Automated Test Results

| Suite | Result |
|-------|--------|
| Backend learn tests (`com.company.learninghub.learn.**`) | **19/19 passed** |
| Backend full suite | 260 passed, 4 pre-existing failures (notifications, user management — unrelated) |
| Frontend (`npm test -- --run`) | **391/391 passed** |

---

## 7. Build Results

| Build | Result |
|-------|--------|
| `mvn package -DskipTests` (backend) | **Success** |
| `npm run build` (frontend) | **Success** |

---

## 8. Self QA

| Check | Result |
|-------|--------|
| V13 migration applies cleanly after V12 | Verified via compile + learn tests |
| Wave 1 JSON validates against schema | Verified in `CatalogImportServiceTest` import run (30 records) |
| Slug upsert preserves published status + featured override | Verified in unit test |
| Create/Edit dialogs removed from admin UI | Verified via file removal + page updates |
| Curation panel shows read-only metadata | Implemented |
| Employee API shape backward compatible (`description`, `featured`) | `TechnologyResponse` retains core F16 fields |
| Catalog import disabled in test profile | `backend/src/test/resources/application.yml` |

---

## 9. Manual QA Checklist

| # | Scenario | Expected | Status |
|---|----------|----------|--------|
| 1 | Deploy app — catalog imports 30 technologies | Admin list shows ~30 HIDDEN technologies | Ready for QA |
| 2 | Employee browse `/learn/technologies` | Only PUBLISHED technologies visible | Ready for QA |
| 3 | Admin publish HIDDEN technology | Employees see it in browse/search | Ready for QA |
| 4 | Admin hide PUBLISHED technology | Removed from employee view | Ready for QA |
| 5 | Admin feature technology | Appears on Learn home featured section | Ready for QA |
| 6 | Admin set org notes + project links | Saved; preserved after restart/reimport | Ready for QA |
| 7 | Admin cannot create technology (no UI, POST returns 404/405) | Authoring blocked | Ready for QA |
| 8 | Technology detail — official website/docs links | Links open in new tab | Ready for QA |
| 9 | Project detail related technologies | Unchanged from F16 | Ready for QA |
| 10 | Catalog status on Manage Technologies | Shows version 1.0.0 and count | Ready for QA |
| 11 | Restart app — same catalog version | No duplicate import (idempotent) | Ready for QA |
| 12 | Search/filter/sort on employee list | Same behavior as F16 | Ready for QA |

---

## 10. Risks

| Risk | Mitigation |
|------|------------|
| Startup failure on invalid catalog | Fail-fast default; schema validation at import |
| F16 migrated rows lose featured distinction | `featured_override` null → catalog default applied on reimport (per spec) |
| Admin confusion without Create button | Toolbar explains catalog-first model; curation panel labels read-only metadata |
| Large catalog JSON in classpath | Wave 1 only (~30); Wave 2 deferred |
| Pre-existing unrelated test failures | Documented; learn tests all pass |

---

## 11. Documentation Updates

| Document | Update |
|----------|--------|
| `.cursor/project-context.md` | F16-R shipped; F17 unblocked after merge |
| `docs/project-roadmap.md` | F16-R shipped; F17 ready |
| `docs/releases/release-v0.8.0-f16-r-implementation-report.md` | This report |

Frozen architecture docs (`08`, `09`, `10`) unchanged — implementation follows them.

---

## 12. F17 Confirmation

**F17 has NOT been started.**

F17 scope (roadmap viewer, `catalog/roadmaps/*.json`, roadmap APIs, RoadmapPage) remains out of this delivery per authorization boundary.

---

## API Summary

| Method | Path | Role | Purpose |
|--------|------|------|---------|
| `GET` | `/api/v1/learn/technologies` | Employee | List published (unchanged) |
| `GET` | `/api/v1/learn/technologies/{id}` | Employee/Admin | Detail (unchanged) |
| `GET` | `/api/v1/learn/manage/technologies` | Admin | List for curation |
| `PATCH` | `/api/v1/learn/manage/technologies/{id}/curation` | Admin | Feature, notes, status |
| `POST` | `/api/v1/learn/manage/technologies/{id}/publish` | Admin | HIDDEN → PUBLISHED |
| `POST` | `/api/v1/learn/manage/technologies/{id}/hide` | Admin | PUBLISHED → HIDDEN |
| `POST` | `/api/v1/learn/manage/technologies/{id}/archive` | Admin | PUBLISHED → ARCHIVED |
| `POST/DELETE` | `/api/v1/learn/manage/technologies/{id}/project-links` | Admin | Project links |
| `GET` | `/api/v1/learn/manage/catalog/status` | Admin | Catalog version/count |

**Removed:** `POST /api/v1/learn/manage/technologies`, `PUT /api/v1/learn/manage/technologies/{id}`

---

## Wave 1 Catalog Coverage

| Category | Technologies |
|----------|-------------|
| BACKEND | spring-boot, java, nodejs, graphql |
| FRONTEND | react, angular, typescript, vite |
| CLOUD | aws, azure, google-cloud |
| DEVOPS | kubernetes, docker, terraform |
| DATABASE | postgresql, mongodb, redis |
| AI_AND_GENAI | python-ml, langchain, openai-api |
| TESTING | junit, playwright |
| SECURITY | owasp-top-10, oauth2 |
| MOBILE | kotlin-android, flutter |
| ARCHITECTURE | microservices, event-driven-architecture |
| DATA_ENGINEERING | apache-spark, apache-kafka |

**Total:** 30 technologies · **catalogVersion:** 1.0.0
