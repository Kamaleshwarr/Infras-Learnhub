# v0.8.0 — Learning Navigation Platform Architecture Revision

**Version:** 2.0 (architecture revision)  
**Date:** 2026-07-02  
**Status:** **Pending product owner approval** — documentation only  
**Triggers:** F16 implementation review — design still resembled an LMS  
**Supersedes (when approved):** Admin-authoring assumptions in `07-implementation-plan.md` v1

---

## Executive summary

Engineering Learning Hub Learn must evolve from an **admin-authored curriculum model** (Technology CRUD → Roadmap CRUD → Stage CRUD → Resource CRUD) to a **Learning Navigation Platform** where:

- **Employees** ask: *What should I learn?*
- **The platform** answers: *what*, *why*, *where*, and *what next*
- **Trusted providers** own learning content
- **Administrators** **curate** — they do not author educational content

This revision preserves the approved product vision (guidance, not LMS) and simplifies long-term maintenance by shipping a **built-in Technology Catalog** (~100 technologies) and **catalog-sourced roadmaps** as seed data.

**No F17 implementation should begin until this revision is approved.**

---

## 1. Updated product philosophy

### 1.1 Core repositioning

| Before (drift risk) | After (target) |
|---------------------|----------------|
| Admins build Technologies like Initiatives | Platform ships a Technology Catalog; admins curate visibility |
| Admins author Roadmaps, Stages, Resources | Platform ships curated roadmaps from trusted public knowledge |
| Learn feels like internal Udemy/Coursera | Learn feels like Google Maps for engineering skills |
| Content ownership in our DB | Content ownership external; we store references and navigation |

### 1.2 The four employee questions (unchanged — strengthened)

Every Learn surface answers:

| # | Question | Platform answer |
|---|----------|-----------------|
| 1 | What should I learn? | Technology Catalog, Career Paths, Certifications |
| 2 | Why should I learn it? | Technology description, career path context, certification readiness |
| 3 | Where should I learn it? | Curated external links (official docs, roadmap.sh, freeCodeCamp, vendor learning) |
| 4 | What should I learn next? | Roadmap stages, progress, Continue Learning, Next up |

### 1.3 What we are

- A **Learning Navigation Platform**
- A **curated map** of external learning
- A **progress companion** for self-directed growth
- An **integration layer** between learning, organizational Projects, and Initiatives

### 1.4 What we are not

- Udemy, Coursera, Pluralsight, or any LMS
- A course authoring environment
- A host for videos, documents, or lessons
- A place where admins write educational curriculum

### 1.5 Guiding metaphor (reaffirmed)

```text
Google Maps                 Engineering Learning Hub Learn
───────────                 ─────────────────────────────
Destinations                Technologies
Routes                      Roadmaps (ordered stages)
Turn-by-turn                Stage sequence + Next up
Business listings           External learning providers
Your location               Progress / My Journey
```

We do not own restaurants. We guide people to them.

---

## 2. Updated architecture

### 2.1 Layered model

```text
┌─────────────────────────────────────────────────────────────┐
│  Employee experience (navigation + progress)                  │
│  Learn Home → Catalog Browse → Technology → Roadmap Viewer   │
│  → External Resources → Progress / My Journey                 │
└─────────────────────────────────────────────────────────────┘
                              ▲
┌─────────────────────────────────────────────────────────────┐
│  Organization curation layer (admin)                          │
│  Publish / Archive / Feature / Hide                         │
│  Org notes · Related Projects · Initiative links            │
│  Review catalog import updates                              │
└─────────────────────────────────────────────────────────────┘
                              ▲
┌─────────────────────────────────────────────────────────────┐
│  Platform catalog (shipped by Engineering Learning Hub)     │
│  ~100 Technologies · Roadmaps · Stages · Resource refs      │
│  Versioned JSON packages · Idempotent import                │
└─────────────────────────────────────────────────────────────┘
                              ▲
┌─────────────────────────────────────────────────────────────┐
│  External providers (content owners)                          │
│  Official docs · Vendor learning · GitHub · YouTube · etc.  │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Content ownership matrix

| Artifact | Owner | Stored in ELH | Admin action |
|----------|-------|-----------------|--------------|
| Technology metadata | Platform catalog | Yes (imported) | Curate visibility, org notes |
| Roadmap structure | Platform catalog | Yes (imported) | Review updates; no manual authoring |
| Stages | Platform catalog | Yes (imported) | None (read-only) |
| Learning Resources | External providers | Reference only (URL + metadata) | None (catalog-maintained) |
| Practice Resources | External providers | Reference only | None (catalog-maintained) |
| Career Paths | Platform catalog | Yes (imported) | Curate visibility |
| Certifications | External providers + catalog refs | Reference metadata | Curate visibility |
| Progress | Employee | Yes (enrollment data) | **Forbidden** (BR-PR10) |
| Org Projects | Projects module | Junction links only | Manage links (F16) |

### 2.3 Technology Catalog schema (conceptual)

Each catalog Technology contains **metadata only**:

| Field | Required | Notes |
|-------|----------|-------|
| `slug` | Yes | Stable business key (`spring-boot`, `aws`) — survives reimport |
| `name` | Yes | Display name |
| `category` | Yes | See §2.4 |
| `shortDescription` | Yes | 1–3 sentences — *why* this matters |
| `difficulty` | Yes | BEGINNER \| INTERMEDIATE \| ADVANCED |
| `estimatedDuration` | Yes | e.g. `4-6 weeks` — guidance, not a deadline |
| `officialWebsite` | Optional | HTTPS URL |
| `officialDocumentation` | Optional | HTTPS URL |
| `tags` | Optional | e.g. `java`, `microservices`, `cloud` |
| `featured` | Catalog default | Overridable per org |
| `status` | Org layer | PUBLISHED \| HIDDEN \| ARCHIVED (org curation) |

**No hosted content. No lesson bodies. No video files.**

### 2.4 Technology categories (catalog)

| Category | Examples |
|----------|----------|
| Backend | Java, Spring Boot, Node.js, Go |
| Frontend | React, Angular, TypeScript |
| Cloud | AWS, Azure, GCP |
| DevOps | Kubernetes, Terraform, CI/CD |
| Database | PostgreSQL, MongoDB, Redis |
| AI & GenAI | LLMs, Prompt Engineering, ML Ops |
| Testing | Selenium, JUnit, Performance Testing |
| Security | OWASP, IAM, Secrets Management |
| Mobile | Android, iOS, Flutter |
| Architecture | Microservices, Event-Driven, DDD |
| Data Engineering | Spark, Airflow, Data Pipelines |

Target: **~100 technologies** at launch, grouped into these categories.

### 2.5 Roadmap model (revised)

Roadmaps are **not admin-authored**. They are:

1. **Curated by the platform team** from trusted public knowledge (roadmap.sh, official vendor learning paths, community consensus)
2. **Shipped as catalog seed data** alongside technologies
3. **Viewed read-only** by employees and admins in MVP
4. **Owned by Engineering Learning Hub** as navigation structure — not as educational IP

Each Roadmap contains ordered Stages. Each Stage contains **references only**:

- Learning Resources (study links)
- Practice Resources (hands-on links)

Future (post-MVP): catalog packages, JSON imports, provider APIs, AI-assisted generation — all feeding the same catalog import pipeline, not admin CRUD screens.

### 2.6 Admin model: Curator, not Author

```text
OLD:  Admin → Create Technology → Build Roadmap → Add Stages → Add Resources → Publish
NEW:  Platform → Import Catalog → Admin → Feature / Hide / Add org notes / Link projects → Employees navigate
```

---

## 3. Revised implementation roadmap

### 3.1 Phase changes summary

| Original | Issue | Revised |
|----------|-------|---------|
| F16 Technology CRUD | Too LMS-like | **Keep shipped value**; refactor in F16-R to catalog + curation |
| F17 Roadmap & Resource CRUD | Admin authors curriculum | **F17 Roadmap Viewer + catalog roadmaps** (read-only) |
| F19 Practice Resource CRUD | Separate authoring surface | **Merged into catalog roadmaps** (no standalone phase) |
| F20 Career Path CRUD | Admin authors paths | **F19 Career Path catalog + viewer** |
| F21 Certification CRUD | Admin authors certs | **F20 Certification catalog + curation** |
| F18 Progress | Correct as-is | **F18 Progress** (unchanged) |
| F22 Initiative assoc | Correct as-is | **F21 Initiative association** (unchanged) |
| F23 Dashboard & release | Correct as-is | **F22 Dashboard, search & release** (unchanged) |

**Phases reduced: 8 → 7** (F19 eliminated by merge; F16-R added as refactor gate).

### 3.2 Revised phase table

| Phase | Name | Employee | Admin | Complexity |
|-------|------|----------|-------|------------|
| **F16** | Technology Discovery *(shipped)* | Browse/search/detail, cross-nav | CRUD *(to be refactored)* | Done |
| **F16-R** | Catalog Foundation Refactor | Same browse experience | **Curation only** — publish/feature/hide/org notes | **M** |
| **F17** | Roadmap Viewer & Catalog Roadmaps | View roadmap, stages, external links | Review catalog; no editors | **M** |
| **F18** | Progress & Learning Journey | Enroll, progress, My Journey | N/A (BR-PR10) | **L** |
| **F19** | Career Path Catalog | Browse/start paths | Curate path visibility | **M** |
| **F20** | Certification Catalog | Browse, readiness, provider CTA | Curate cert visibility | **S** |
| **F21** | Optional Initiative Association | Initiative progress card | Link certification on initiative | **S** |
| **F22** | Dashboard, Unified Search & Release | Widgets, search, polish | Release QA | **M** |

### 3.3 What was removed from scope

| Removed capability | Rationale |
|--------------------|-----------|
| Admin Technology create/edit (metadata) | Catalog ships technologies |
| Admin Roadmap editor | Roadmaps are catalog seed |
| Admin Stage CRUD | Stages are catalog seed |
| Admin Learning Resource library CRUD | Resources are catalog references |
| Admin Practice Resource CRUD | Merged into roadmap catalog |
| Admin Career Path authoring | Paths are catalog seed |
| Admin Certification authoring | Certifications are catalog references |
| Publish validation requiring admin-built content | Replaced by catalog completeness checks |

### 3.4 F16-R — Catalog Foundation Refactor (prerequisite)

**Objective:** Align F16 implementation with the navigation platform model before F17.

| Work | Detail |
|------|--------|
| Replace Technology CRUD | Catalog import + org curation overlay |
| Add `slug`, duration, URLs, tags | Extend schema; migrate existing seed rows |
| Admin UI | Remove create/edit metadata dialogs; add curation panel (feature, hide, org notes) |
| Catalog import service | Load `catalog/technologies.v1.json` idempotently |
| API | Employee read unchanged; admin manage → curation endpoints |
| Deprecate | `POST/PUT` create/edit technology metadata |

### 3.5 Revised F17 — Roadmap Viewer & Catalog Roadmaps

**Objective:** Answer *where to learn* with read-only roadmaps and external links.

| Employee | Admin |
|----------|-------|
| Roadmap page with stepper/timeline | No roadmap editor |
| Stages with learning + practice links | Review imported catalog version |
| Start with Stage 1 / Next up (static until F18) | Enable/disable technology visibility only |

| In scope | Out of scope |
|----------|--------------|
| `catalog/roadmaps/*.json` import | Roadmap editor UI |
| `GET .../technologies/{slug}/roadmap` | Stage CRUD APIs |
| External link cards (new tab) | Resource library admin |
| View Roadmap CTA active | Progress checkmarks (F18) |

---

## 4. Technology Catalog architecture recommendation

### 4.1 Options evaluated

| Option | Description | Pros | Cons |
|--------|-------------|------|------|
| **A — Flyway SQL seed** | `INSERT` ~100 technologies in migration SQL | Simple; runs once with migrations | Unmaintainable diffs; roadmaps/resources explode; hard to review |
| **B — JSON startup import** | Load JSON on every app start | Easy to edit; version-controlled | No migration coupling; race conditions; harder to test deterministically |
| **C — Hybrid (recommended)** | Schema via Flyway; data via versioned catalog packages + idempotent import | Best of both; reviewable; scalable to roadmaps | Slightly more engineering upfront |

### 4.2 Recommendation: **Option C — Hybrid catalog packages**

```text
backend/src/main/resources/catalog/
├── manifest.json                 # catalog version, package list
├── technologies.v1.json          # ~100 technology records
├── roadmaps/
│   ├── spring-boot.v1.json
│   ├── aws.v1.json
│   └── ...
├── career-paths.v1.json
└── certifications.v1.json
```

**Import pipeline:**

1. Flyway manages **schema** (`learn_technologies`, `learn_roadmaps`, etc.)
2. `learn_catalog_imports` table records applied catalog version
3. `CatalogImportService` runs on startup (or `@EventListener(ApplicationReadyEvent)`)
4. Import is **idempotent** — upsert by `slug`, never duplicate
5. Org curation fields (`featured`, `status`, `orgNotes`) preserved on reimport
6. CI validates catalog JSON against JSON Schema before merge

**Why this wins at ~100 technologies:**

| Concern | How hybrid addresses it |
|---------|-------------------------|
| Maintainability | JSON diffs in PR review, not 5,000-line SQL |
| Performance | Data in PostgreSQL after import — full-text search, filters |
| Org overrides | Curation layer separate from catalog source |
| Future packages | `technologies.v2.json` bumps manifest version |
| Testability | Import service unit tests with fixture JSON |
| Rollback | Pin manifest version; reimport previous package |

### 4.3 Stable identifiers

Use **`slug`** (kebab-case) as the immutable business key:

- URLs: `/learn/technologies/spring-boot`
- Foreign keys: `technology_slug` in roadmaps, project links, enrollments
- UUIDs remain internal PKs but are not catalog identities

### 4.4 Search and browse

Post-import, Technologies live in PostgreSQL with:

- Full-text search on name, description, tags
- Category and difficulty filters (existing F16 patterns)
- Featured flag (catalog default + org override)

---

## 5. Recommended seed strategy

### 5.1 Technology seed (~100 entries)

| Wave | Count | Purpose |
|------|-------|---------|
| Wave 1 (MVP) | 30 | Cover all 11 categories with 2–3 exemplars each |
| Wave 2 (launch) | 70 | Fill catalog to ~100 before v0.8.0 release |
| Ongoing | +N | Catalog package version bumps (v2, v3) |

**Wave 1 priority technologies (illustrative):**

- Backend: Java, Spring Boot, Go
- Frontend: React, TypeScript
- Cloud: AWS, Kubernetes
- DevOps: Terraform, Docker
- Database: PostgreSQL, Redis
- AI & GenAI: Prompt Engineering, LLM APIs
- Testing: JUnit, Selenium
- Security: OWASP Top 10
- Mobile: Flutter
- Architecture: Microservices
- Data Engineering: Apache Spark

### 5.2 Roadmap seed (MVP)

| Scope | Target |
|-------|--------|
| Technologies with roadmaps at F17 | **All Wave 1 (30)** |
| Stages per roadmap | 3–8 (catalog-defined) |
| Resources per stage | 2–5 external links |
| Practice links | 0–2 per stage (in same JSON) |

**Source attribution in catalog metadata:**

```json
{
  "source": "roadmap.sh",
  "sourceUrl": "https://roadmap.sh/spring-boot",
  "curatedBy": "platform-team",
  "catalogVersion": "1.0.0"
}
```

### 5.3 Import governance

| Rule | Detail |
|------|--------|
| Catalog changes require PR review | Platform team owns `catalog/` directory |
| JSON Schema validation in CI | Block merge on invalid catalog |
| No admin UI to edit catalog content in MVP | Prevents LMS drift |
| Org admins see import version | `GET /api/v1/learn/manage/catalog/status` |

---

## 6. Recommended roadmap strategy

### 6.1 MVP (F17)

| Principle | Implementation |
|-----------|----------------|
| Read-only | No admin roadmap editor |
| Shipped | Platform team curates JSON, imports on deploy |
| Trusted sources | roadmap.sh, official vendor paths, freeCodeCamp, MDN, Microsoft Learn |
| Single roadmap per technology | BR-C05 preserved |
| External links only | BR-LR01 URL validation on import |

### 6.2 Roadmap JSON structure (illustrative)

```json
{
  "technologySlug": "spring-boot",
  "catalogVersion": "1.0.0",
  "stages": [
    {
      "order": 1,
      "title": "Spring Boot Foundations",
      "description": "Understand auto-configuration, starters, and project structure.",
      "estimatedEffort": "1-2 weeks",
      "learningResources": [
        {
          "title": "Spring Boot Reference Documentation",
          "url": "https://docs.spring.io/spring-boot/docs/current/reference/html/",
          "type": "OFFICIAL_DOCUMENTATION",
          "provider": "Spring",
          "freePaid": "FREE"
        }
      ],
      "practiceResources": [
        {
          "title": "Spring Boot PetClinic Sample",
          "url": "https://github.com/spring-projects/spring-petclinic",
          "type": "GITHUB",
          "provider": "Spring"
        }
      ]
    }
  ]
}
```

### 6.3 Future roadmap (post-MVP)

| Capability | Phase | Notes |
|------------|-------|-------|
| Catalog package v2 imports | v0.8.x | Non-breaking upsert |
| Community contribution workflow | v0.9+ | PR to catalog repo, not admin UI |
| Provider API sync | v0.9+ | e.g. Microsoft Learn catalog API |
| AI-assisted curation | v0.9+ | Draft JSON for human review — never auto-publish |
| Org-specific roadmap overlays | Deferred | Org notes only in MVP; not custom stages |

---

## 7. Recommended admin responsibilities

### 7.1 Admin CAN (curator)

| Action | Surface | Phase |
|--------|---------|-------|
| **Publish** technology for org | Learn Manage | F16-R |
| **Hide / Archive** technology | Learn Manage | F16-R |
| **Feature** technology on Learn home | Learn Manage | F16-R |
| **Add organization notes** | Technology curation panel | F16-R |
| **Manage related Projects** | Technology curation (F16) | F16-R |
| **Review catalog import status** | Learn Manage → Catalog | F17 |
| **Link Certification to Initiative** | Initiative editor | F21 |
| **Curate Career Path visibility** | Learn Manage | F19 |
| **Curate Certification visibility** | Learn Manage | F20 |

### 7.2 Admin CANNOT (author)

| Forbidden action | Replacement |
|------------------|-------------|
| Create Technology metadata | Select from catalog |
| Edit Technology name/description/difficulty | Request catalog update via platform team |
| Create or edit Roadmaps | Catalog package update |
| Create or edit Stages | Catalog package update |
| Add Learning Resources manually | Catalog package update |
| Add Practice Resources manually | Catalog package update |
| Author Career Paths | Catalog package update |
| Author Certification content | Catalog references only |
| Edit employee progress | Forbidden (BR-PR10) |

### 7.3 Admin UI evolution

```text
CURRENT (F16):                    TARGET (F16-R+):
─────────────────                 ─────────────────
Create Technology dialog    →     (removed)
Edit Technology dialog      →     Curation panel
  - name, description       →       - featured toggle
  - category, difficulty    →       - publish / hide / archive
  - project links           →       - org notes (textarea)
                                  - project links (kept)
                                  - catalog metadata (read-only)
Manage Technologies list    →     Catalog browse + curation filters
```

---

## 8. Impact analysis

### 8.1 F16 implementation impact

F16 shipped before this revision. It is **directionally correct** (browse, search, cross-nav) but **architecturally misaligned** on admin capabilities.

| F16 artifact | Status | Required change |
|--------------|--------|-----------------|
| Employee browse/search/detail | **Keep** | Minor: slug-based URLs optional |
| Learn home, list, filters | **Keep** | Featured driven by curation |
| Related Projects cross-nav | **Keep** | Link by slug or UUID |
| `CreateTechnologyDialog` | **Remove** | Replace with catalog browse |
| `EditTechnologyDialog` (metadata) | **Remove** | Replace with curation panel |
| `POST/PUT` technology manage APIs | **Deprecate** | Curation endpoints only |
| `V12` migration | **Extend** | Add slug, URLs, tags, duration, org_notes, catalog_version |
| 3-row SQL seed | **Replace** | Full catalog JSON import |
| Admin status tabs (DRAFT/PUBLISHED) | **Revise** | DRAFT → HIDDEN; catalog items start as importable |

**Estimated refactor effort:** Medium — F16-R phase before revised F17.

### 8.2 Documentation impact

| Document | Action |
|----------|--------|
| `00-product-design.md` | Update admin responsibilities section on approval |
| `03-business-rules.md` | Add BR-CAT rules; revise BR-LC publish rules |
| `07-implementation-plan.md` | Superseded by `09-implementation-plan-v2.md` |
| `04-user-flows.md` | Remove admin authoring flows |
| `.cursor/project-context.md` | Note revision pending |

### 8.3 Risk impact

| Risk | Before revision | After revision |
|------|-----------------|----------------|
| LMS scope creep | High — CRUD at every layer | Low — catalog boundary |
| Admin burden | High — author 100 roadmaps | Low — curate visibility |
| Content staleness | Admins must maintain links | Platform team bumps catalog version |
| F16 sunk cost | — | Mitigated by keeping employee UX |

### 8.4 What does NOT change

- Projects module independence (BR-M01–05)
- Progress is employee-owned (BR-PR10)
- External links only — no hosting
- Four-questions UX principle
- Cross-navigation Technology ↔ Project
- Initiative optional association (F21)
- Flyway incremental migrations for **schema** (not catalog data)

---

## Approval gate

| Gate | Status |
|------|--------|
| Architecture revision v2.0 | **Pending approval** |
| F16-R implementation | Blocked until approval |
| Revised F17 implementation | Blocked until F16-R complete |
| Original F17 (admin CRUD) | **Cancelled** |

---

## Related documents

- [09-implementation-plan-v2.md](./09-implementation-plan-v2.md) — Revised F16-R through F22 detail
- [07-implementation-plan.md](./07-implementation-plan.md) — Original plan (frozen; superseded on approval)
- [F16 implementation report](../releases/release-v0.8.0-f16-implementation-report.md)
