# v0.8.0 Learn Module — Implementation Plan v2

**Status:** **DRAFT — pending approval** (supersedes v1 admin-authoring model)  
**Feature numbering:** F16-R, F17–F22 (F16 shipped; original F19 eliminated)  
**Depends on:** F16 complete; architecture revision `08-navigation-platform-revision.md` approved  
**Classification:** Implementation planning — **no production code until approved**

---

## Document purpose

This plan replaces the admin-authoring phases in `07-implementation-plan.md` with a **catalog-first, curation-only** model aligned with the Learning Navigation Platform vision.

**Reference:** [08-navigation-platform-revision.md](./08-navigation-platform-revision.md)

---

## Revised principles

| Principle | v1 (frozen) | v2 (this plan) |
|-----------|-------------|----------------|
| Admin–employee parity | Admin CRUD paired with each slice | Admin **curation** paired with employee **navigation** |
| Technology source | Admin creates | **Platform catalog** imports |
| Roadmap source | Admin authors | **Platform catalog** imports |
| Resources | Admin CRUD library | **Catalog references** only |
| Admin role | Content author | **Curator** |

**Unchanged:** Vertical slices, four-questions UX, progress employee-owned, Projects independence, external links only.

---

## Phase overview

| Phase | Name | Complexity |
|-------|------|------------|
| F16 | Technology Discovery *(shipped)* | Done |
| **F16-R** | **Catalog Foundation Refactor** | **M** |
| **F17** | **Roadmap Viewer & Catalog Roadmaps** | **M** |
| F18 | Progress & Learning Journey | L |
| F19 | Career Path Catalog | M |
| F20 | Certification Catalog | S |
| F21 | Optional Initiative Association | S |
| F22 | Dashboard, Unified Search & Release | M |

**Eliminated:** Original F19 (Practice Resource CRUD) — practice links live in roadmap catalog JSON.

---

# F16-R — Catalog Foundation Refactor

## Objective

Refactor F16 from Technology CRUD to **catalog import + org curation** without regressing employee browse/search/detail.

## In scope

- Catalog package structure (`catalog/technologies.v1.json`, `manifest.json`)
- `CatalogImportService` — idempotent upsert by `slug`
- Schema migration `V13__learn_catalog_foundation.sql`:
  - Add `slug`, `estimated_duration`, `official_website`, `official_documentation`, `tags`, `org_notes`, `catalog_version`, `source`
  - Migrate existing F16 rows to slugs
  - Unique index on `slug`
- Replace admin create/edit with **curation endpoints**:
  - `PATCH .../manage/technologies/{slug}/curation` — featured, status, orgNotes
  - `POST .../publish`, `/archive`, `/hide` (org visibility)
- Remove `POST` create, `PUT` metadata update
- Admin UI: remove Create/Edit dialogs; add **CurationPanel** (read-only metadata + curation controls)
- Import Wave 1 catalog (~30 technologies)
- `GET .../manage/catalog/status` — catalog version, last import, counts

## Out of scope

- Roadmaps (F17)
- Progress (F18)

## Acceptance criteria

- [ ] ~30 technologies imported from JSON on deploy
- [ ] Admin cannot create/edit technology metadata via UI or API
- [ ] Admin can feature, publish, hide, archive, add org notes
- [ ] Employee browse/search/detail unchanged
- [ ] Project links still manageable
- [ ] Reimport is idempotent (org curation preserved)

## Complexity: **M**

---

# F17 — Roadmap Viewer & Catalog Roadmaps

## Objective

Employees view read-only Roadmaps with Stages and curated external links. Platform ships roadmap catalog data — no admin editors.

## Admin–employee model

| Employee | Admin |
|----------|-------|
| View Roadmap, Stages, Learning + Practice links | Review catalog version; no content editors |
| Start with Stage 1 / Next up | — |

## In scope

- `catalog/roadmaps/*.v1.json` — stages, learning resources, practice resources
- Migration `V14__learn_roadmap_catalog.sql` — roadmaps, stages, resource refs (imported, not admin-created)
- `CatalogImportService` extends to roadmaps
- `GET /api/v1/learn/technologies/{slug}/roadmap`
- `RoadmapPage` — stepper/timeline, external link cards, new tab
- View Roadmap CTA active on Technology detail
- Import validation: ≥3 stages per roadmap, ≥1 resource per stage, HTTPS URLs
- Roadmaps for all Wave 1 technologies (30)

## Out of scope

- Roadmap editor, Stage CRUD, Resource library admin
- Enrollment / progress (F18)
- Career Paths (F19)

## Acceptance criteria

- [ ] Employee views full roadmap with external links
- [ ] No admin UI to create/edit stages or resources
- [ ] Catalog reimport updates roadmaps without breaking org curation
- [ ] Stage 1 guidance visible

## Complexity: **M** (reduced from L — no editor)

---

# F18 — Progress & Learning Journey

**Unchanged from v1 plan** with these clarifications:

- Enrollment references `technology_slug`
- Progress tracks catalog stage IDs (stable across import)
- Admin still cannot edit progress (BR-PR10)
- Migration `V15__learn_progress.sql`

See `07-implementation-plan.md` F18 section for detail — scope identical.

## Complexity: **L**

---

# F19 — Career Path Catalog

## Objective

Ship curated Career Paths as catalog data. Employees browse and start paths. Admins curate visibility only.

## Replaces

Original F20 (Career Path CRUD)

## In scope

- `catalog/career-paths.v1.json`
- Import service extension
- Employee: browse, search, detail, start path
- Admin: feature, hide, publish path visibility
- Migration `V16__learn_career_paths.sql`

## Out of scope

- Admin authoring of path content or technology ordering

## Complexity: **M**

---

# F20 — Certification Catalog

## Objective

Industry certification references as catalog data. Readiness guidance and provider CTAs.

## Replaces

Original F21 (Certification CRUD)

## In scope

- `catalog/certifications.v1.json`
- Employee browse, readiness views, outbound provider links
- Admin: curate visibility, link to technologies
- Migration `V17__learn_certifications.sql`

## Complexity: **S**

---

# F21 — Optional Initiative Association

**Unchanged from v1 F22.**

## Complexity: **S**

---

# F22 — Dashboard, Unified Search & Release

**Mostly unchanged from v1 F23.**

Adjustments:

- Learn home shows catalog-driven featured technologies
- Unified search across catalog entities
- Catalog version in release notes
- Full manual QA across F16-R–F21

## Complexity: **M**

---

## Flyway migration map (revised)

| Migration | Phase | Contents |
|-----------|-------|----------|
| `V12` | F16 *(shipped)* | `learn_technologies`, project links |
| `V13` | F16-R | Catalog columns, import tracking |
| `V14` | F17 | Roadmap schema (import-populated) |
| `V15` | F18 | Progress tables |
| `V16` | F19 | Career path schema |
| `V17` | F20 | Certification schema |

**Catalog data** is not in Flyway SQL — it lives in `catalog/*.json` and is imported by `CatalogImportService`.

---

## Admin capability matrix (v2)

| Capability | F16 | F16-R | F17 | F18 | F19 | F20 | F21 |
|------------|-----|-------|-----|-----|-----|-----|-----|
| Browse catalog technologies | — | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Create technology | ✓ | **✗** | ✗ | ✗ | ✗ | ✗ | ✗ |
| Edit technology metadata | ✓ | **✗** | ✗ | ✗ | ✗ | ✗ | ✗ |
| Feature / hide / publish | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Org notes | — | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Project links | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| Roadmap editor | — | — | **✗** | ✗ | ✗ | ✗ | ✗ |
| Resource CRUD | — | — | **✗** | ✗ | ✗ | ✗ | ✗ |
| Edit employee progress | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ |
| Initiative cert link | — | — | — | — | — | — | ✓ |

---

## Approval gate

| Gate | Required before |
|------|-----------------|
| Architecture revision v2 approved | F16-R start |
| F16-R merged | Revised F17 start |
| Each subsequent phase | Prior phase merged |

**Original F17 (admin CRUD) must not be implemented.**
