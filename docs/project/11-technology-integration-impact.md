# Project Module — Technology Integration Impact

**Date:** 2026-07-06

---

## 1. Current implementation (KEEP)

### Database

```text
learn_technologies ←→ learn_technology_project_links ←→ projects
```

- Migration: V12 `learn_technology_project_links`
- Unique `(technology_id, project_id)`
- CASCADE on delete

### Backend ownership

| Concern | Owner module | Location |
|---------|--------------|----------|
| Junction table | Learn | `LearnTechnologyProjectLink` |
| Add/remove links | Learn (admin) | `LearnTechnologyManageController` |
| List techs for project | Learn | `LearnTechnologyService.listPublishedTechnologiesForProject()` |
| Project detail enrichment | Project | `ProjectKnowledgeService.getProject()` calls Learn service |

### API surfaces

| Endpoint | Direction |
|----------|-----------|
| `POST/DELETE /learn/manage/technologies/{id}/project-links` | Admin links tech → project |
| `GET /projects/{id}` | Returns `relatedTechnologies[]` (published only) |
| `GET /learn/technologies/{id}` | Returns `relatedProjects[]` |

### Frontend

| Component | Location | Route |
|-----------|----------|-------|
| `RelatedOrganizationProjectsCard` | Technology detail | → `/projects/{id}` |
| `RelatedTechnologiesCard` | Project detail (placeholder) | → `/learn/technologies/{id}` |
| Admin curation | `TechnologyCurationPanel` | Manage project links |

---

## 2. Target experience

### Technology Detail — "Used in Projects"

```text
Used in Projects
  • Project A  → /projects/{id}
  • Project B  → /projects/{id}
```

**Status:** Shipped on Learn side. **No backend change required.**

### Project Detail — "Technology Stack"

```text
Technology Stack
  • Java          → /learn/technologies/{id}
  • Spring Boot   → /learn/technologies/{id}
  • React         → /learn/technologies/{id}
```

**Status:** API returns data; **P1 frontend** renders on overview.

---

## 3. Gap analysis for technology integration

| Item | Classification | Action |
|------|----------------|--------|
| Junction table | **KEEP** | No schema change |
| Admin link APIs | **KEEP** | No change |
| Published-only filter on project | **KEEP** | Correct — draft techs hidden |
| `RelatedProjectSummary` (id, name) | **EXTEND** optional | Add slug for prettier URLs later |
| `RelatedTechnologySummary` | **KEEP** | Already has id, name |
| Project overview tech chips | **NEW** UI | P1 |
| "Projects using {tech}" search | **NEW** | P4 filter via junction |
| Employee manage tech links on project | **REMOVE** idea | Stay admin-only in Learn |
| Duplicate junction in Project module | **DO NOT** | Single source of truth in Learn |

---

## 4. Data rules (preserve)

| Rule | Implementation |
|------|----------------|
| Many-to-many | One project ↔ many technologies; one technology ↔ many projects |
| Admin-only link management | `@PreAuthorize ADMIN` on manage controller |
| Published technologies only on project detail | `findPublishedTechnologiesByProjectId` with status filter |
| Archived/hidden projects on tech detail | Currently returns all linked projects — **consider filtering archived** in P1 |
| Catalog import preserves links | `CatalogImportService` uses `findCatalogPresentWithProjectLinks` |

---

## 5. Recommended P1 enhancement (optional)

Filter **archived projects** from `relatedProjects` on technology detail for employees.

```text
Today: all linked projects shown
Proposed: hide archived unless admin
```

Classification: **EXTEND** — small query change in `LearnTechnologyService.loadRelatedProjects()`.

---

## 6. Search integration (P4)

### Query: "Which project uses Kafka?"

```sql
SELECT p.* FROM projects p
JOIN learn_technology_project_links l ON l.project_id = p.id
JOIN learn_technologies t ON t.id = l.technology_id
WHERE t.slug = 'kafka' OR LOWER(t.name) LIKE '%kafka%'
```

Apply standard project visibility rules in service layer.

### Query: "Show projects using React"

Same pattern with technology filter on `GET /projects?technologySlug=react`.

**Do not duplicate** technology data on project rows — always join through junction.

---

## 7. AI Assistant compatibility

Structured fields for future queries:

| Question | Data source |
|----------|-------------|
| Which project uses Kafka? | `learn_technology_project_links` + `learn_technologies.slug` |
| Technologies used by Project A? | Same junction from project side |
| UAT Swagger URL? | `project_environment_references` (P3) — not technology link |

Technology links answer **stack** questions; environment refs answer **URL** questions.

---

## 8. Impact summary

| Area | Impact level | Notes |
|------|--------------|-------|
| Learn database | **None** | Keep junction |
| Learn APIs | **None** | Maybe archived filter |
| Learn frontend | **None** | Card already works |
| Project database | **None** for tech | No new FK on projects |
| Project APIs | **None** | Already enriched |
| Project frontend | **Medium** | Render existing API data |

**Conclusion:** Project Module implementation **reuses** the existing Technology ↔ Project model without duplication. Primary work is **frontend rendering** and **search filters** in P4.
