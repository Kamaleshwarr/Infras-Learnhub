# v0.8.0 F17 — Roadmap Viewer Implementation Report

**Phase:** F17 — Roadmap Viewer & Catalog Roadmaps  
**Branch:** `cursor/f17-roadmap-viewer-59d6`  
**Status:** Complete  
**F18:** Not started

---

## 1. Impact Analysis

F17 delivers the first read-only Roadmap Viewer for the Learn module. Employees can navigate from a Technology to its catalog-imported roadmap, see ordered stages, and open trusted external learning and practice resources. Administrators can preview roadmaps and see roadmap import status — no CRUD or editors.

The implementation is **framework-first**: new roadmaps require only JSON files under `catalog/roadmaps/` and a manifest/catalog version bump — no application code changes.

**In scope:** V14 schema, catalog roadmaps package, import pipeline extension, roadmap APIs, RoadmapPage UI, admin catalog status extension, tests.  
**Out of scope:** Progress tracking (F18), career paths, certifications, editors, persistence, AI.

---

## 2. Files Changed

### Backend (new)
- `V14__learn_roadmap_catalog.sql`
- Domain: `LearnRoadmap`, `LearnRoadmapStage`, `LearnRoadmapStageResource`, resource enums
- Repositories: roadmap, stage, stage resource
- Catalog DTOs: `CatalogRoadmapRecord`, `CatalogRoadmapStageRecord`, `CatalogRoadmapResourceRecord`
- `CatalogSchemaValidator` — roadmap validation
- `CatalogImportService` — roadmaps package import
- `LearnRoadmapService`, `LearnRoadmapMapper`, DTOs, `LearnRoadmapController`
- `LearnCatalogService` / `LearnCatalogManageController` — roadmap import status

### Catalog (new/updated)
- `catalog/manifest.json` → **1.1.0**
- `catalog/schemas/roadmap.schema.json`
- `catalog/roadmaps/{java,spring-boot,react,docker,aws}.json`

### Frontend (new/updated)
- `RoadmapPage.tsx`, `RoadmapStageCard.tsx`
- `types/roadmap.ts`, `learnApi` roadmap methods
- Routes, `TechnologyDetailPage` CTA enabled
- Tests: `RoadmapPage.test.tsx`, updated detail/routes tests

---

## 3. Database Changes

**Flyway V14** creates:
- `learn_roadmaps` — FK `technology_slug` → `learn_technologies.slug`
- `learn_roadmap_stages` — ordered stages per roadmap
- `learn_roadmap_stage_resources` — external LEARNING/PRACTICE links

No admin-authored rows; all data imported from catalog JSON at startup.

---

## 4. Catalog Changes

| Item | Value |
|------|-------|
| Catalog version | **1.1.0** |
| New package | `roadmaps` (`catalog/roadmaps/*.json`) |
| Schema | `catalog/schemas/roadmap.schema.json` |
| Initial roadmaps | 5 (Java, Spring Boot, React, Docker, AWS) |
| Total stages | 30 (7+6+6+5+6) |

---

## 5. APIs Added

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/learn/technologies/{slug}/roadmap` | Roadmap by technology slug |
| GET | `/api/v1/learn/technologies/id/{technologyId}/roadmap` | Roadmap by technology UUID |

**Extended:** `GET /api/v1/learn/manage/catalog/status` — adds `roadmapImportStatus`, `roadmapRecordsUpserted`, `roadmapCount`.

---

## 6. Business Rules

- Roadmaps are catalog-imported only; administrators cannot edit content
- Roadmap `technologySlug` must reference an existing catalog technology
- Stages: 3–20, contiguous order from 1, ≥1 learning resource per stage
- All resource URLs must be HTTPS
- Employees see roadmaps only for **published** technologies with `catalog_present`
- Admins can preview roadmaps for hidden/archived technologies
- Missing roadmap → 404 (empty state in UI)
- Import is idempotent per package type + catalog version
- Removed roadmap JSON files → `catalog_present = false` on reimport

---

## 7. Tests Added

### Backend
- `CatalogRoadmapSchemaValidatorTest` — stage order validation
- `CatalogImportServiceTest` — updated for per-package import + roadmaps skip
- `LearnRoadmapServiceTest` — employee/admin visibility, stage assembly
- `LearnRoadmapControllerTest` — slug and ID endpoints

### Frontend
- `RoadmapPage.test.tsx` — overview, stages, empty, not-found, stepper
- `TechnologyDetailPage.test.tsx` — View Roadmap link
- `AppRoutes.test.tsx` — roadmap route

---

## 8. Automated Test Results

| Suite | Result |
|-------|--------|
| Backend `com.company.learninghub.learn.**` | **44 passed**, 6 skipped (Testcontainers) |
| Frontend F17 tests | **10 passed** |
| Frontend build | **Success** |
| Backend package | **Success** |

Pre-existing failures in unrelated modules (`NotificationControllerTest`, `UserManagementServiceTest`) unchanged.

---

## 9. Build Results

```bash
cd backend && mvn package -DskipTests   # SUCCESS
cd frontend && npm run build            # SUCCESS
```

---

## 10. Self QA

| Check | Status |
|-------|--------|
| Roadmaps imported at startup | ✓ Framework + 5 JSON files |
| Technology → Roadmap navigation | ✓ CTA + route |
| Ordered stages | ✓ `stage_order` + stepper |
| External links (new tab) | ✓ `target="_blank"` |
| Responsive layout | ✓ MUI Stack/Card |
| Empty state | ✓ No stages / no roadmap |
| Invalid technology handling | ✓ 404 |
| Existing search unaffected | ✓ No search changes |

---

## 11. Manual QA Checklist

- [ ] Start backend with fresh DB; confirm Flyway V14 applied
- [ ] Confirm catalog 1.1.0 imports 30 technologies + 5 roadmaps
- [ ] Publish Java; open Technology → View Roadmap
- [ ] Verify 7 ordered stages with external links
- [ ] Repeat for Spring Boot, React, Docker, AWS
- [ ] Open unpublished technology as employee → roadmap 404
- [ ] Admin: preview hidden technology roadmap
- [ ] Admin: Learn Manage catalog status shows roadmap counts
- [ ] Technology search (`java`) still ranks Java above JavaScript
- [ ] Mobile viewport: roadmap stepper and cards readable

---

## 12. Risks

| Risk | Mitigation |
|------|------------|
| Only 5 of 30 Wave 1 technologies have roadmaps | By design for F17; framework supports adding JSON only |
| `recommendedStageOrder` is static (stage 1) until F18 | Documented; F18 adds progress |
| Per-package import on catalog bump | Technologies reimport on 1.1.0 first boot after upgrade |

---

## 13. Documentation Updates

- This report: `docs/releases/release-v0.8.0-f17-implementation-report.md`
- `.cursor/project-context.md` — F17 shipped status
- `docs/project-roadmap.md` — F17 complete, F18 next

---

## 14. F18 Confirmation

**F18 has NOT been started.** No progress tracking, enrollment, or persistence was implemented.

---

## Implementation Report (Operations)

| Field | Value |
|-------|-------|
| **Branch Name** | `cursor/f17-roadmap-viewer-59d6` |
| **Latest Commit** | *(see git log after push)* |
| **How to Pull** | `git fetch origin && git checkout cursor/f17-roadmap-viewer-59d6` |
| **Backend Start** | `cd backend && mvn spring-boot:run` |
| **Frontend Start** | `cd frontend && npm run dev` |
| **Expected Flyway Version** | **V14** |
| **Expected Catalog Version** | **1.1.0** |
| **Expected Technologies** | **30** (wave-1) |
| **Expected Roadmaps** | **5** (java, spring-boot, react, docker, aws) |
| **Expected Stages** | **30** total |

### Known Limitations

- No learner progress or “current stage” persistence (F18)
- `estimatedTotalEffort` is a joined display of per-stage strings, not computed calendar math
- Technologies without a roadmap JSON file show the empty/not-found state

### Manual QA Checklist

See section 11 above.
