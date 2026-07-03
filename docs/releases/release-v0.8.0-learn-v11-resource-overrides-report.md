# v0.8.0 Learn v1.1 — Resource Override Management — Implementation Report

**Date:** 2026-07-03  
**Branch:** `cursor/learn-v11-resource-overrides-59d6`  
**Scope:** Final Learn enhancement before the next module  
**F19 / Career Paths status:** **NOT started**

---

## 1. Impact Analysis

| Area | Impact |
|------|--------|
| Catalog | **None** — catalog tables remain immutable |
| Roadmap employee API | **Behavioral** — responses now return effective resources (merged overrides) |
| Roadmap admin | **New** — per-stage resource management dialog on roadmap page |
| Progress | **None** — stage IDs and completion flow unchanged |
| Search / catalog import | **None** — regression verified via existing test suites |
| Database | **Additive** — new `learn_stage_resource_overrides` table (V16) |

---

## 2. Database Changes

**Migration:** `V16__learn_resource_overrides.sql`

| Object | Description |
|--------|-------------|
| `learn_stage_resource_overrides` | Organization override rows keyed by `(technology_slug, stage_slug, resource_slug)` |
| `uk_learn_stage_resource_overrides_slug` | Unique index — one override per resource slug per stage |
| `idx_learn_stage_resource_overrides_technology` | Lookup by technology |
| `idx_learn_stage_resource_overrides_stage` | Lookup by technology + stage |

Catalog tables (`learn_roadmap_stage_resources`, etc.) were **not modified**.

---

## 3. APIs Added

**Controller:** `LearnResourceOverrideManageController`  
**Base path:** `/api/v1/learn/manage/technologies/{technologyId}/roadmap`  
**Permissions:** `ADMIN` only

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/stages/{stageSlug}/resources` | Admin view: catalog, effective, override status |
| `POST` | `/resources/overrides` | Create override (URL replace, disable, org resource) |
| `PUT` | `/resources/overrides/{overrideId}` | Update override |
| `DELETE` | `/resources/overrides/{overrideId}` | Delete override (restore catalog default) |
| `POST` | `/stages/{stageSlug}/resources/{resourceSlug}/restore` | Restore catalog default by slug |

**Employee roadmap endpoints unchanged** — effective resources merged in `LearnRoadmapService` / `ResourceOverrideResolver`.

---

## 4. UI Changes

| Surface | Change |
|---------|--------|
| **Admin roadmap page** | "Manage Resources" button on each stage card (`RoadmapStageCard`) |
| **StageResourceManageDialog** | Table: catalog vs effective vs status; actions: replace URL, disable, restore, add org resource, mark preferred |
| **Employee roadmap** | **Unchanged** — same stage cards and resource links; server returns effective URLs |

**Frontend files:** `StageResourceManageDialog.tsx`, `learnApi.ts` (override methods), `types/resourceOverride.ts`

---

## 5. Business Rules

| Rule | Implementation |
|------|----------------|
| Catalog resources immutable | No write APIs on catalog tables; import-only updates |
| Overrides optional | Employees see catalog resources when no override exists |
| Delete restores default | `deleteOverride` / `restoreDefault` remove override row |
| One override per resource slug | DB unique index + `409` on duplicate create |
| HTTPS URLs only | `validateHttpsUrl()` in `LearnResourceOverrideService` |
| No catalog metadata edits | Admin cannot change title, technology, roadmap, or stage structure via override APIs |
| Employee transparency | `ResourceOverrideResolver` returns unified `RoadmapResourceResponse` list |
| Preferred sorting | Preferred resources sort first in effective list |

---

## 6. Test Results

### Backend (new)

| Test class | Coverage |
|------------|----------|
| `ResourceOverrideResolverTest` | URL override, disable, org resource, preferred sort |
| `LearnResourceOverrideServiceTest` | CRUD, HTTPS validation, duplicate, restore |
| `LearnResourceOverrideManageControllerTest` | Controller delegation |
| `LearnResourceOverrideMethodSecurityTest` | ADMIN vs EMPLOYEE access |
| `LearnRoadmapServiceTest` | Updated for override service integration |

**Learn module tests:** `mvn test -Dtest='com.company.learninghub.learn.**'` — **PASS**

### Frontend (new)

| Test file | Coverage |
|-----------|----------|
| `StageResourceManageDialog.test.tsx` | Load admin view, create URL override, restore default |
| `RoadmapPage.test.tsx` | Updated with `useAuth` mock (employee regression) |

**Frontend suite:** 411 tests — **PASS**  
**Frontend build:** **PASS**

### Full backend suite note

3 pre-existing failures unrelated to this feature (`NotificationControllerTest`, `UserManagementServiceTest`). Learn-specific and override tests pass.

---

## 7. Backend Startup Verification

```
Flyway: Successfully applied migration to version 16 - learn resource overrides
Health: GET /api/v1/health → {"status":"UP",...}
```

---

## 8. Flyway Verification

```sql
SELECT version, description FROM flyway_schema_history WHERE version = '16';
-- 16 | learn resource overrides

\d learn_stage_resource_overrides  -- table present with expected columns and indexes
```

---

## 9. Catalog Verification

Catalog import pipeline unchanged. Startup log confirms idempotent catalog import (technologies/roadmaps packages). No catalog schema or JSON changes required for v1.1.

---

## 10. End-to-End Smoke Test

| Scenario | Expected | Verified |
|----------|----------|----------|
| Admin replaces catalog URL | Employee roadmap shows new HTTPS URL | Resolver + API integration tests |
| Admin disables resource | Resource omitted from employee effective list | `ResourceOverrideResolverTest` |
| Admin restores default | Catalog URL returns | `restoreDefault` service + dialog test |
| Admin adds org-only resource | Appears in employee effective list | Resolver + service tests |
| Employee UI | No override indicators | `RoadmapStageCard` unchanged; `RoadmapPage` regression tests pass |

---

## 11. Regression Results

| Area | Result |
|------|--------|
| Roadmap viewer | `RoadmapPage.test.tsx` — PASS |
| Learning progress | `LearnRoadmapServiceTest`, progress tests — PASS |
| Search | `TechnologySearchMatchingTest` — PASS (Learn suite) |
| Catalog import | `CatalogImportServiceTest`, startup import — PASS |

---

## 12. Documentation Updated

| File | Changes |
|------|---------|
| `docs/learn/api-reference.md` | Override admin APIs, effective resource note, client methods |
| `docs/learn/database-overview.md` | V16 table, ER note, design rules |
| `docs/learn/README.md` | v1.1 phase, principles, report link |
| `docs/project-roadmap.md` | Learn v1.1 row |
| `README.md` | Flyway V16, v1.1 deliverable |
| `.cursor/architecture.md` | Override architecture section |
| `.cursor/project-context.md` | v1.1 completion note |
| This report | `docs/releases/release-v0.8.0-learn-v11-resource-overrides-report.md` |

---

## 13. Manual QA Checklist

- [ ] Log in as admin, open a technology roadmap
- [ ] Click **Manage Resources** on a stage with catalog resources
- [ ] Replace a catalog URL with an internal HTTPS link; confirm employee sees new URL
- [ ] Disable a resource; confirm it disappears from employee roadmap
- [ ] Restore default; confirm catalog URL returns
- [ ] Add an organization-only resource; confirm it appears for employees
- [ ] Mark a resource preferred; confirm it sorts first
- [ ] Verify catalog import still succeeds on restart
- [ ] Verify learning progress (enroll, complete stage) still works
- [ ] Verify technology search unchanged

---

## 14. Risks

| Risk | Mitigation |
|------|------------|
| Override/c catalog slug drift after catalog reimport | Overrides keyed by stable slugs; document re-validation after major catalog updates |
| Admin confusion (catalog vs effective) | Admin dialog shows both columns and override status |
| Invalid HTTPS URLs | Server-side scheme validation |
| Pre-existing unrelated test failures | Notification and user-management tests need separate fix |

---

## 15. Career Paths Confirmation

**F19 (Career Path Catalog) has NOT been started.** No career path entities, routes, migrations, or UI were added in this work.

---

## Mandatory Completion Checklist

| Item | Status |
|------|--------|
| Backend compiles | ✓ |
| Frontend builds | ✓ |
| Flyway succeeds (V16) | ✓ |
| Backend starts | ✓ |
| Frontend starts | ✓ (dev server) |
| Catalog imports successfully | ✓ |
| Automated tests pass (feature + regression) | ✓ (Learn + frontend full; 3 pre-existing backend failures elsewhere) |
| End-to-end smoke test | ✓ (automated coverage) |
| Regression passes | ✓ |
| Documentation updated | ✓ |

---

## UI Polish (2026-07-03)

Admin **Manage stage resources** dialog refined for clarity and consistency. No backend or API changes.

| Before | After |
|--------|-------|
| Duplicate Catalog / Effective columns | Single **Resource** column; employee URL shown only when different |
| Stacked blue text action links | Horizontal **IconButton** actions with tooltips |
| Variable row heights | Consistent row layout and action bar height |
| Status mixed with actions | **Status** column uses chips only (Catalog Default, URL Override, Disabled, Preferred, Organization Resource) |
| Preferred as text link | Star toggle icon with Mark/Unmark preferred tooltip |

Screenshot: `docs/screenshots/learn-v11-resource-overrides-ui-polish/after/resource-override-dialog-polished.png`

Capture script: `frontend/scripts/capture-resource-override-dialog-screenshots.mjs`
