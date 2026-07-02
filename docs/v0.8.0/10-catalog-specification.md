# v0.8.0 — Catalog Specification

**Version:** 1.0  
**Status:** **FROZEN — approved** (final documentation refinement before F16-R)  
**Depends on:** [08-navigation-platform-revision.md](./08-navigation-platform-revision.md) (approved v2.0)  
**Classification:** Catalog architecture specification — authoritative for F16-R implementation

---

## Product philosophy

> **Engineering Learning Hub owns guidance, not knowledge.**

| The platform owns | External providers own |
|-------------------|----------------------|
| Technology catalog metadata (names, categories, descriptions, difficulty, duration) | Lessons, videos, courses, and assessments |
| Roadmap structure (ordered stages, sequencing, next-step logic) | Educational content within each stage |
| Curated references (URLs, titles, types, providers) | Hosted material at those URLs |
| Progress and enrollment state | Certification exam delivery |
| Organization curation (visibility, featured, org notes, project links) | Organizational project knowledge (Projects module) |
| Career path navigation graphs | Career path educational content |
| Certification readiness guidance | Official certification programs |

We store **pointers and structure**, not **knowledge artifacts**. When an employee clicks a learning resource, they leave Engineering Learning Hub and learn from the trusted provider.

---

## 1. Catalog directory structure

Canonical location: `backend/src/main/resources/catalog/`

```text
catalog/
├── manifest.json
├── schemas/
│   ├── technology.schema.json
│   ├── roadmap.schema.json
│   ├── career-path.schema.json
│   └── certification.schema.json
├── technologies/
│   ├── wave-1.json              # MVP batch (~30 technologies)
│   └── wave-2.json              # Launch batch (remaining to ~100)
├── roadmaps/
│   ├── spring-boot.json
│   ├── aws.json
│   ├── react.json
│   └── ...
├── career-paths/
│   └── wave-1.json              # F19 — not imported in F16-R
└── certifications/
    └── wave-1.json              # F20 — not imported in F16-R
```

### 1.1 `manifest.json`

The manifest is the single source of truth for catalog versioning and package discovery.

```json
{
  "catalogVersion": "1.0.0",
  "releasedAt": "2026-07-02T00:00:00Z",
  "description": "Engineering Learning Hub platform catalog — Wave 1",
  "packages": [
    {
      "type": "technologies",
      "path": "technologies/wave-1.json",
      "version": "1.0.0",
      "recordCount": 30
    },
    {
      "type": "roadmaps",
      "path": "roadmaps/",
      "version": "1.0.0",
      "pattern": "*.json"
    }
  ]
}
```

| Field | Required | Description |
|-------|----------|-------------|
| `catalogVersion` | Yes | Semver for the entire catalog release |
| `releasedAt` | Yes | ISO-8601 UTC timestamp |
| `packages` | Yes | Ordered list of importable packages |

### 1.2 Package conventions

| Rule | Detail |
|------|--------|
| File naming | Kebab-case slugs for single-entity files (`spring-boot.json`) |
| Batch files | `wave-N.json` for grouped imports |
| One roadmap per file | `roadmaps/{technology-slug}.json` |
| Schema validation | Every package file validated against `schemas/*.schema.json` in CI |
| No secrets | Catalog contains only public metadata and HTTPS URLs |

---

## 2. JSON schema recommendations

Schemas live in `catalog/schemas/` and are enforced in CI and at application startup.

### 2.1 Technology schema

**File:** `catalog/schemas/technology.schema.json`  
**Package format:** Array of technology objects in `technologies/wave-*.json`

#### Technology object — required fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `slug` | string | `^[a-z0-9]+(-[a-z0-9]+)*$`, unique | Stable business key |
| `version` | string | Semver | Catalog record version |
| `name` | string | max 100 | Display name |
| `shortName` | string | max 30 | Compact label |
| `category` | enum | See §2.1.1 | Technology category |
| `shortDescription` | string | max 500 | Why this technology matters |
| `difficulty` | enum | `BEGINNER` \| `INTERMEDIATE` \| `ADVANCED` | Entry difficulty |
| `estimatedDuration` | string | max 50 | e.g. `4-6 weeks` |
| `source` | string | max 100 | Provenance label (e.g. `platform-team`, `roadmap.sh`) |
| `updatedAt` | string | ISO-8601 UTC | Last catalog update for this record |

#### Technology object — optional fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `officialWebsite` | string | HTTPS URL, max 2048 | Vendor or project homepage |
| `officialDocumentation` | string | HTTPS URL, max 2048 | Primary documentation URL |
| `tags` | string[] | max 10 items, each max 50 chars | Searchable tags |
| `featured` | boolean | default `false` | Catalog default for featured flag |
| `sourceUrl` | string | HTTPS URL | Attribution link for curation source |

#### 2.1.1 Technology categories (enum)

`BACKEND` · `FRONTEND` · `CLOUD` · `DEVOPS` · `DATABASE` · `AI_AND_GENAI` · `TESTING` · `SECURITY` · `MOBILE` · `ARCHITECTURE` · `DATA_ENGINEERING`

#### 2.1.2 Technology example

```json
{
  "slug": "spring-boot",
  "version": "1.0.0",
  "name": "Spring Boot",
  "shortName": "Spring Boot",
  "category": "BACKEND",
  "shortDescription": "Production-ready Java applications with convention-over-configuration and a rich ecosystem.",
  "difficulty": "INTERMEDIATE",
  "estimatedDuration": "4-6 weeks",
  "officialWebsite": "https://spring.io/projects/spring-boot",
  "officialDocumentation": "https://docs.spring.io/spring-boot/docs/current/reference/html/",
  "tags": ["java", "microservices", "rest-api"],
  "featured": true,
  "source": "platform-team",
  "sourceUrl": "https://roadmap.sh/spring-boot",
  "updatedAt": "2026-07-02T00:00:00Z"
}
```

#### 2.1.3 Technology package file example

```json
{
  "packageVersion": "1.0.0",
  "type": "technologies",
  "updatedAt": "2026-07-02T00:00:00Z",
  "technologies": [
    { "slug": "spring-boot", "version": "1.0.0", "...": "..." },
    { "slug": "aws", "version": "1.0.0", "...": "..." }
  ]
}
```

---

### 2.2 Roadmap schema

**File:** `catalog/schemas/roadmap.schema.json`  
**Package format:** One roadmap per file in `roadmaps/{technology-slug}.json`

#### Roadmap root — required fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `technologySlug` | string | Must match existing technology `slug` | Parent technology |
| `version` | string | Semver | Roadmap catalog version |
| `source` | string | max 100 | Provenance (e.g. `roadmap.sh`, `platform-team`) |
| `updatedAt` | string | ISO-8601 UTC | Last catalog update |
| `stages` | array | min 3, max 20 items | Ordered learning stages |

#### Roadmap root — optional fields

| Field | Type | Description |
|-------|------|-------------|
| `sourceUrl` | string | Attribution URL |
| `description` | string | Roadmap overview (max 2000) |

#### Stage object — required fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `order` | integer | 1-based, contiguous | Stage sequence |
| `slug` | string | unique within roadmap | Stable stage key (e.g. `foundations`) |
| `title` | string | max 150 | Stage title |
| `description` | string | max 3000 | What the learner will understand |
| `estimatedEffort` | string | max 50 | e.g. `1-2 weeks` |
| `learningResources` | array | min 1 item | External study links |

#### Stage object — optional fields

| Field | Type | Description |
|-------|------|-------------|
| `practiceResources` | array | External hands-on links (0–n) |

#### Resource reference object (learning and practice)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| `slug` | string | unique within stage | Stable resource key |
| `title` | string | max 200 | Display title |
| `url` | string | HTTPS, max 2048 | External link |
| `type` | enum | See §2.2.1 | Resource classification |
| `provider` | string | max 100 | e.g. `Spring`, `AWS`, `freeCodeCamp` |
| `freePaid` | enum | `FREE` \| `PAID` \| `FREEMIUM` | Cost disclosure |
| `version` | string | Semver | Resource record version |
| `source` | string | max 100 | Provenance |
| `updatedAt` | string | ISO-8601 UTC | Last update |

#### 2.2.1 Resource types (enum)

`OFFICIAL_DOCUMENTATION` · `OFFICIAL_TUTORIAL` · `OPEN_EDUCATIONAL_RESOURCE` · `VIDEO` · `ARTICLE` · `GITHUB` · `INTERACTIVE_TUTORIAL` · `PRACTICE_LAB` · `OTHER`

#### 2.2.2 Roadmap example

```json
{
  "technologySlug": "spring-boot",
  "version": "1.0.0",
  "source": "platform-team",
  "sourceUrl": "https://roadmap.sh/spring-boot",
  "updatedAt": "2026-07-02T00:00:00Z",
  "description": "A structured path from Spring Boot fundamentals to production readiness.",
  "stages": [
    {
      "order": 1,
      "slug": "foundations",
      "title": "Spring Boot Foundations",
      "description": "Understand auto-configuration, starters, and project structure.",
      "estimatedEffort": "1-2 weeks",
      "learningResources": [
        {
          "slug": "spring-boot-reference",
          "version": "1.0.0",
          "title": "Spring Boot Reference Documentation",
          "url": "https://docs.spring.io/spring-boot/docs/current/reference/html/",
          "type": "OFFICIAL_DOCUMENTATION",
          "provider": "Spring",
          "freePaid": "FREE",
          "source": "spring.io",
          "updatedAt": "2026-07-02T00:00:00Z"
        }
      ],
      "practiceResources": [
        {
          "slug": "spring-petclinic",
          "version": "1.0.0",
          "title": "Spring PetClinic Sample",
          "url": "https://github.com/spring-projects/spring-petclinic",
          "type": "GITHUB",
          "provider": "Spring",
          "freePaid": "FREE",
          "source": "spring.io",
          "updatedAt": "2026-07-02T00:00:00Z"
        }
      ]
    }
  ]
}
```

---

## 3. Catalog import lifecycle

The import pipeline runs on application startup via `CatalogImportService`. Flyway manages **schema**; the catalog service manages **data**.

### 3.1 Lifecycle overview

```text
Application startup
       │
       ▼
┌──────────────────┐
│ Read manifest.json│
└────────┬─────────┘
         ▼
┌──────────────────┐     fail ──► Log error; block startup (fail-fast)
│ Schema validate   │            or skip import (configurable — default fail-fast)
│ all package files │
└────────┬─────────┘
         ▼
┌──────────────────┐
│ Compare manifest  │
│ catalogVersion vs │
│ learn_catalog_    │
│ imports table     │
└────────┬─────────┘
         │
    same version ──► Skip import (no-op)
         │
    new version ──► Continue
         ▼
┌──────────────────┐
│ For each package  │
│ in manifest order:│
│  1. Parse JSON    │
│  2. Validate refs │
│  3. Upsert by slug│
│  4. Preserve org  │
│     overrides     │
└────────┬─────────┘
         ▼
┌──────────────────┐
│ Record import in  │
│ learn_catalog_    │
│ imports           │
└────────┬─────────┘
         ▼
    Application ready
```

### 3.2 Startup validation

| Step | Detail |
|------|--------|
| Manifest parse | `manifest.json` must be valid JSON with required fields |
| Package discovery | Every `packages[].path` must resolve to a file or directory |
| Schema validation | Each package file validated against its JSON Schema |
| Referential integrity | Roadmap `technologySlug` must exist in imported technologies |
| URL validation | All resource URLs must be HTTPS (BR-LR01) |
| Stage rules | Contiguous order starting at 1; 3–20 stages per roadmap |

**Fail-fast default:** Invalid catalog prevents application startup in production. Development may allow `catalog.import.fail-fast=false` for partial testing.

### 3.3 JSON schema validation

| When | Where |
|------|-------|
| CI (required) | `mvn test` or dedicated `catalog-validate` job validates all files against schemas |
| Startup (required) | `CatalogImportService` re-validates before import |
| PR review | Human review of catalog diffs alongside automated validation |

### 3.4 Idempotent import

| Rule | Behaviour |
|------|-----------|
| Upsert key | `slug` for technologies; `technologySlug` + stage `slug` for stages; stage + resource `slug` for resources |
| Re-run safety | Importing the same `catalogVersion` twice is a no-op |
| New version | Upsert updates catalog-owned fields; org overrides preserved (see §3.5) |
| Deletions | Catalog entries removed from JSON are **soft-hidden** (`catalog_present = false`), not hard-deleted, if org has curation history |
| Transaction | Each package imports in a single transaction; failure rolls back package |

### 3.5 Slug-based updates

```text
Catalog JSON (source of truth for content)     Database (runtime)
─────────────────────────────────────────     ────────────────────
slug: spring-boot                      ──►    UPSERT learn_technologies
  name, description, URLs, tags               WHERE slug = 'spring-boot'
  version, source, updatedAt                  SET catalog fields = JSON values
```

When `catalogVersion` bumps:

1. Match existing row by `slug`
2. Update catalog-owned columns from JSON
3. **Do not overwrite** organization-specific columns

### 3.6 Organization-specific override preservation

These fields are **owned by the organization**, not the catalog. They are **never overwritten** on reimport:

| Field | Table | Owner | Set by |
|-------|-------|-------|--------|
| `featured` (org override) | `learn_technologies` | Organization | Admin curation — if admin has set override, preserve; else apply catalog default |
| `status` (visibility) | `learn_technologies` | Organization | Admin publish/hide/archive |
| `org_notes` | `learn_technologies` | Organization | Admin free-text |
| Project links | `learn_technology_project_links` | Organization | Admin link/unlink — **never touched by import** |
| Enrollment progress | `learn_learning_enrollments` | Employee | F18 — never touched by import |

#### Override resolution rules

| Scenario | Behaviour |
|----------|-----------|
| First import | Apply catalog `featured` default; set `status = HIDDEN` until admin publishes |
| Reimport, admin has not curated | Update catalog fields; apply new catalog `featured` default |
| Reimport, admin has set `featured` | Preserve admin `featured`; update all other catalog fields |
| Reimport, admin has `org_notes` | Preserve `org_notes` unchanged |
| Reimport, admin has project links | Preserve all project links unchanged |
| Reimport, admin has set `status = ARCHIVED` | Preserve `status`; update catalog metadata only |

#### Implementation hint (for F16-R)

```text
learn_technologies columns:
  -- catalog-owned (updated on import)
  slug, name, short_name, description, category, difficulty,
  estimated_duration, official_website, official_documentation,
  tags, catalog_version, catalog_source, catalog_updated_at

  -- org-owned (preserved on import)
  featured_override, status, org_notes

  -- resolved at read time
  featured = COALESCE(featured_override, catalog_featured_default)
```

---

## 4. Database tracking

### `learn_catalog_imports` table (F16-R)

| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID | PK |
| `catalog_version` | VARCHAR | From manifest |
| `imported_at` | TIMESTAMPTZ | When import completed |
| `package_type` | VARCHAR | `technologies`, `roadmaps`, etc. |
| `records_upserted` | INT | Count |
| `status` | VARCHAR | `SUCCESS` \| `FAILED` |

---

## 5. CI requirements

| Check | Command / trigger |
|-------|-------------------|
| Schema validation | All `catalog/**/*.json` against `catalog/schemas/*.schema.json` |
| Slug uniqueness | No duplicate slugs within or across technology packages |
| Referential integrity | Every roadmap `technologySlug` exists in technology packages |
| HTTPS URLs | All `url`, `officialWebsite`, `officialDocumentation` fields |
| Manifest consistency | `recordCount` matches actual records |

---

## 6. F16-R import scope

F16-R imports **technologies only** (`catalog/technologies/wave-1.json`). Roadmaps, career paths, and certifications are defined here for consistency but imported in later phases.

| Package | F16-R | F17 | F19 | F20 |
|---------|-------|-----|-----|-----|
| `technologies/` | ✓ | — | — | — |
| `roadmaps/` | — | ✓ | — | — |
| `career-paths/` | — | — | ✓ | — |
| `certifications/` | — | — | — | ✓ |

---

## Approval

| Gate | Status |
|------|--------|
| Catalog specification v1.0 | **FROZEN — approved** |
| F16-R implementation | **Ready to begin** |
