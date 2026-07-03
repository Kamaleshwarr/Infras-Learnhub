# Learn Module — Database Overview

**Flyway versions:** V12–V16  
**Database:** PostgreSQL 16

This document describes every Learn-related table, its purpose, and how tables relate. Progress data overlays catalog data — roadmap content is never duplicated in progress tables.

---

## Entity-relationship overview

```mermaid
erDiagram
    users ||--o{ learn_learning_enrollments : "owns"
    learn_technologies ||--o{ learn_technology_project_links : "links"
    projects ||--o{ learn_technology_project_links : "linked from"
    learn_technologies ||--o| learn_roadmaps : "slug FK"
    learn_roadmaps ||--|{ learn_roadmap_stages : "contains"
    learn_roadmap_stages ||--|{ learn_roadmap_stage_resources : "has"
    learn_learning_enrollments ||--|{ learn_stage_progress : "tracks"
    learn_roadmap_stages ||--o{ learn_stage_progress : "completed stage"
    learn_roadmap_stages ||--o| learn_learning_enrollments : "current stage"
    learn_catalog_imports }o--|| catalog_packages : "records import of"

    learn_technologies {
        uuid id PK
        varchar slug UK
        varchar name
        varchar status
        boolean catalog_present
    }

    learn_roadmaps {
        uuid id PK
        varchar technology_slug FK UK
        varchar version
        boolean catalog_present
    }

    learn_roadmap_stages {
        uuid id PK
        uuid roadmap_id FK
        int stage_order
        varchar slug
        varchar title
    }

    learn_roadmap_stage_resources {
        uuid id PK
        uuid stage_id FK
        varchar resource_kind
        varchar url
    }

    learn_learning_enrollments {
        uuid id PK
        uuid user_id FK
        varchar technology_slug FK
        varchar status
        uuid current_stage_id FK
    }

    learn_stage_progress {
        uuid id PK
        uuid enrollment_id FK
        uuid stage_id FK
        timestamptz completed_at
    }

    learn_catalog_imports {
        uuid id PK
        varchar catalog_version
        varchar package_type
        varchar status
    }

    learn_technology_project_links {
        uuid id PK
        uuid technology_id FK
        uuid project_id FK
    }
```

---

## Table reference

### `learn_technologies`

**Purpose:** Technology catalog records — metadata for browse, search, and curation.

**Migration:** V12 (created), V13 (catalog columns)

| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID PK | Surrogate key |
| `slug` | VARCHAR(100) UNIQUE | Stable catalog identifier (e.g. `spring-boot`) |
| `name` | VARCHAR(100) | Display name |
| `short_name` | VARCHAR(30) | Abbreviated label |
| `description` | TEXT | Long description |
| `category` | VARCHAR(30) | BACKEND, FRONTEND, CLOUD, … |
| `difficulty` | VARCHAR(20) | BEGINNER, INTERMEDIATE, ADVANCED |
| `status` | VARCHAR(20) | HIDDEN, PUBLISHED, ARCHIVED |
| `catalog_featured` | BOOLEAN | Catalog default featured flag |
| `featured_override` | BOOLEAN | Admin override for featured |
| `estimated_duration` | VARCHAR(50) | Human-readable duration |
| `official_website` | VARCHAR(2048) | External URL |
| `official_documentation` | VARCHAR(2048) | External URL |
| `tags` | JSONB | Search tags array |
| `org_notes` | TEXT | Admin-only organizational notes |
| `catalog_version` | VARCHAR(20) | Last imported catalog version |
| `catalog_source` | VARCHAR(100) | Source attribution |
| `catalog_source_url` | VARCHAR(2048) | Source URL |
| `catalog_updated_at` | TIMESTAMPTZ | Catalog record timestamp |
| `catalog_present` | BOOLEAN | `false` when removed from catalog |
| `created_by` | UUID FK → `users` | Bootstrap user reference |
| `created_at`, `updated_at` | TIMESTAMPTZ | Audit |

**Indexes:** `slug`, `status`, `category`, `difficulty`, `catalog_featured`

---

### `learn_technology_project_links`

**Purpose:** Many-to-many link between technologies and organizational projects (cross-navigation).

**Migration:** V12

| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID PK | |
| `technology_id` | UUID FK → `learn_technologies` ON DELETE CASCADE | |
| `project_id` | UUID FK → `projects` ON DELETE CASCADE | |
| `created_at` | TIMESTAMPTZ | |

**Unique:** `(technology_id, project_id)`

---

### `learn_catalog_imports`

**Purpose:** Audit log of catalog package imports (idempotent per version + package type).

**Migration:** V13

| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID PK | |
| `catalog_version` | VARCHAR(20) | From `manifest.json` |
| `imported_at` | TIMESTAMPTZ | Import timestamp |
| `package_type` | VARCHAR(50) | `technologies` or `roadmaps` |
| `records_upserted` | INT | Count of records processed |
| `status` | VARCHAR(20) | SUCCESS or FAILED |

---

### `learn_roadmaps`

**Purpose:** One roadmap per technology (catalog-imported, read-only at runtime).

**Migration:** V14

| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID PK | |
| `technology_slug` | VARCHAR(100) UNIQUE FK → `learn_technologies(slug)` ON DELETE CASCADE | |
| `version` | VARCHAR(20) | Roadmap package version |
| `description` | TEXT | Roadmap summary |
| `source` | VARCHAR(100) | Attribution |
| `source_url` | VARCHAR(2048) | External reference |
| `catalog_updated_at` | TIMESTAMPTZ | |
| `catalog_present` | BOOLEAN | Soft-hide when removed from catalog |
| `created_at`, `updated_at` | TIMESTAMPTZ | |

---

### `learn_roadmap_stages`

**Purpose:** Ordered stages within a roadmap.

**Migration:** V14

| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID PK | Referenced by progress tables |
| `roadmap_id` | UUID FK → `learn_roadmaps` ON DELETE CASCADE | |
| `stage_order` | INTEGER | 1-based sequence (unique per roadmap) |
| `slug` | VARCHAR(100) | Stage identifier (unique per roadmap) |
| `title` | VARCHAR(150) | |
| `description` | TEXT | |
| `estimated_effort` | VARCHAR(50) | e.g. `1 week`, `1-2 weeks` |
| `notes` | TEXT | Optional guidance |
| `created_at`, `updated_at` | TIMESTAMPTZ | |

**Unique:** `(roadmap_id, stage_order)`, `(roadmap_id, slug)`

---

### `learn_roadmap_stage_resources`

**Purpose:** External learning and practice resources attached to a stage.

**Migration:** V14

| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID PK | |
| `stage_id` | UUID FK → `learn_roadmap_stages` ON DELETE CASCADE | |
| `resource_kind` | VARCHAR(20) | LEARNING or PRACTICE |
| `resource_order` | INTEGER | Display order |
| `slug` | VARCHAR(100) | Unique per stage |
| `title` | VARCHAR(200) | |
| `url` | VARCHAR(2048) | External link |
| `resource_type` | VARCHAR(50) | OFFICIAL_DOCUMENTATION, VIDEO, … |
| `provider` | VARCHAR(100) | e.g. Oracle Docs |
| `free_paid` | VARCHAR(20) | FREE, PAID, FREEMIUM |
| `version` | VARCHAR(20) | Optional |
| `source` | VARCHAR(100) | Optional |
| `updated_at` | TIMESTAMPTZ | |
| `created_at` | TIMESTAMPTZ | |

---

### `learn_learning_enrollments`

**Purpose:** Per-user technology enrollment and journey state (employee-owned).

**Migration:** V15

| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID PK | |
| `user_id` | UUID FK → `users` ON DELETE CASCADE | |
| `technology_slug` | VARCHAR(100) FK → `learn_technologies(slug)` | |
| `status` | VARCHAR(20) | NOT_STARTED, IN_PROGRESS, COMPLETED, LEFT |
| `enrolled_at` | TIMESTAMPTZ | |
| `started_at` | TIMESTAMPTZ | First learning activity |
| `last_activity_at` | TIMESTAMPTZ | Last progress update |
| `current_stage_id` | UUID FK → `learn_roadmap_stages` ON DELETE SET NULL | |
| `completed_at` | TIMESTAMPTZ | Roadmap completion timestamp |
| `created_at`, `updated_at` | TIMESTAMPTZ | |

**Partial unique index:** `(user_id, technology_slug) WHERE status IN ('NOT_STARTED','IN_PROGRESS','COMPLETED')` — one active enrollment per user per technology.

---

### `learn_stage_progress`

**Purpose:** Individual completed stage records per enrollment.

**Migration:** V15

| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID PK | |
| `enrollment_id` | UUID FK → `learn_learning_enrollments` ON DELETE CASCADE | |
| `stage_id` | UUID FK → `learn_roadmap_stages` ON DELETE CASCADE | |
| `completed_at` | TIMESTAMPTZ | |
| `created_at`, `updated_at` | TIMESTAMPTZ | |

**Unique:** `(enrollment_id, stage_id)`

---

## Relationship summary

| From | To | Cardinality | Notes |
|------|-----|-------------|-------|
| `learn_technologies` | `learn_roadmaps` | 1:0..1 | Via `technology_slug` |
| `learn_roadmaps` | `learn_roadmap_stages` | 1:N | Ordered stages |
| `learn_roadmap_stages` | `learn_roadmap_stage_resources` | 1:N | Learning + practice |
| `learn_technologies` | `learn_stage_resource_overrides` | 1:N | Via `technology_slug` (org overrides) |
| `users` | `learn_learning_enrollments` | 1:N | Employee journeys |
| `learn_technologies` | `learn_learning_enrollments` | 1:N | Via slug |
| `learn_learning_enrollments` | `learn_stage_progress` | 1:N | Completed stages |
| `learn_roadmap_stages` | `learn_stage_progress` | 1:N | Progress references stage ID |
| `learn_technologies` | `projects` | N:M | Via `learn_technology_project_links` |

---

## Design rules

1. **No roadmap duplication in progress** — Progress stores stage IDs and completion timestamps only.
2. **Catalog immutability at runtime** — Roadmap content changes only via catalog reimport.
3. **Slug as stable key** — Enrollments reference `technology_slug`; technology UUIDs are for API convenience.
4. **Soft catalog removal** — `catalog_present = false` hides records without deleting progress history.
5. **Resource overrides** — Organization overrides live in `learn_stage_resource_overrides`; catalog resources are never updated in place.
6. **BR-PR10** — No admin API to edit employee progress.

---

## Migration files

| File | Content |
|------|---------|
| `V12__learn_technologies.sql` | Initial technology tables + seed |
| `V13__learn_catalog_foundation.sql` | Catalog columns, imports table, status migration |
| `V14__learn_roadmap_catalog.sql` | Roadmap, stages, resources |
| `V15__learn_progress.sql` | Enrollments, stage progress |
| `V16__learn_resource_overrides.sql` | Organization resource overrides |

---

### `learn_stage_resource_overrides`

**Purpose:** Organization-owned overrides for catalog stage resources. Catalog tables remain immutable.

**Migration:** V16

| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID PK | |
| `technology_slug` | VARCHAR(100) | Stable technology reference |
| `stage_slug` | VARCHAR(100) | Stage within roadmap |
| `resource_slug` | VARCHAR(100) | Unique override identifier per stage |
| `catalog_resource_slug` | VARCHAR(100) NULL | Catalog resource slug when overriding; NULL for org-only resources |
| `resource_kind` | VARCHAR(20) | LEARNING or PRACTICE |
| `disabled` | BOOLEAN | Hide catalog resource from employees |
| `override_url` | VARCHAR(2048) | Replacement HTTPS URL |
| `preferred` | BOOLEAN | Sort preferred resources first |
| `enabled` | BOOLEAN | Active override flag |
| `reason` | TEXT | Admin reason/notes |
| `title` | VARCHAR(200) | Required for org-only resources |
| `resource_type` | VARCHAR(50) | For org-only resources |
| `provider` | VARCHAR(100) | Optional provider |
| `free_paid` | VARCHAR(20) | FREE, PAID, FREEMIUM |
| `resource_order` | INTEGER | Display order hint |
| `created_at`, `updated_at` | TIMESTAMPTZ | Audit |

**Unique:** `(technology_slug, stage_slug, resource_slug)`

**Design rules:**
- One override per resource slug per stage
- Deleting an override restores catalog default
- Override URLs must be HTTPS
- Employees receive merged effective resources via API; override metadata is admin-only
