# Project Module — Current State Audit

**Date:** 2026-07-06  
**Scope:** Backend, frontend, database, APIs, permissions, integrations, tests

---

## 1. Module identity

The codebase refers to this area as **Project Knowledge Repository** (`com.company.learninghub.projectknowledge`). It is listed as a completed backend module in `README.md` and `.cursor/project-context.md`, but the **frontend is placeholder-only**.

| Layer | Maturity |
|-------|----------|
| Database schema (V6) | Shipped |
| Domain entities & repositories | Shipped |
| REST API | Shipped (full CRUD) |
| Service & authorization | Shipped |
| File storage (local) | Shipped |
| Unit / security tests | Shipped |
| Frontend UI | **Placeholder only** |
| Seed / demo data | **None** |
| Initiative integration | **None** |
| Global project search | **None** |

---

## 2. Database (Flyway V6)

Migration: `backend/src/main/resources/db/migration/V6__create_project_knowledge_repository.sql`

### 2.1 Tables

| Table | Purpose |
|-------|---------|
| `projects` | Core project record |
| `project_members` | Per-project membership and role |
| `project_knowledge_folders` | Hierarchical folders (`parent_id` self-FK) |
| `project_knowledge_items` | Files or external links |
| `project_knowledge_access_events` | Per-item access audit |

### 2.2 `projects` columns

| Column | Notes |
|--------|-------|
| `id` | UUID PK |
| `name` | VARCHAR(200), globally unique |
| `description` | TEXT, optional |
| `access_type` | `PUBLIC` \| `MEMBERS_ONLY` |
| `archived` | BOOLEAN, default false |
| `created_by` | FK → `users` |
| `created_at`, `updated_at` | TIMESTAMPTZ |

**Missing vs target:** project status, technical/QA owners, tags, quick-link aggregates, review metadata.

### 2.3 `project_members` columns

| Column | Notes |
|--------|-------|
| `project_role` | `OWNER` \| `CONTRIBUTOR` \| `VIEWER` |
| Unique | `(project_id, user_id)` |

### 2.4 `project_knowledge_folders`

- Supports **unlimited nesting** via nullable `parent_id`
- Unique name per `(project_id, parent_id)` (case-insensitive)
- Delete blocked if folder has children or items

### 2.5 `project_knowledge_items`

| Column | Notes |
|--------|-------|
| `category` | See `KnowledgeCategory` enum (8 values) |
| `source_type` | `FILE` \| `LINK` |
| File fields | `storage_provider`, `storage_key`, `original_filename`, `content_type`, `file_size_bytes` |
| Link fields | `external_url` |
| `access_count` | Incremented on download/link access |
| `uploaded_by` | FK → `users` |

**Categories (CHECK constraint):**

- `REQUIREMENTS`
- `KT_DOCUMENTS`
- `ARCHITECTURE_DOCUMENTS`
- `RELEASE_NOTES`
- `TEST_STRATEGY`
- `TEST_DATA_DOCUMENTATION`
- `KT_VIDEOS`
- `EXTERNAL_LINKS`

### 2.6 Learn junction (Flyway V12)

Table: `learn_technology_project_links`

- M:N between `learn_technologies` and `projects`
- Unique `(technology_id, project_id)`
- CASCADE delete on either side
- **No seed links** in migrations

---

## 3. Backend package structure

```text
backend/src/main/java/com/company/learninghub/projectknowledge/
├── controller/ProjectKnowledgeController.java
├── domain/
│   ├── Project.java
│   ├── ProjectMember.java
│   ├── ProjectRole.java (OWNER, CONTRIBUTOR, VIEWER)
│   ├── ProjectAccessType.java (PUBLIC, MEMBERS_ONLY)
│   ├── ProjectKnowledgeFolder.java
│   ├── ProjectKnowledgeItem.java
│   ├── KnowledgeCategory.java
│   ├── KnowledgeSourceType.java (FILE, LINK)
│   └── ProjectKnowledgeAccessEvent.java
├── dto/ (request/response records)
├── mapper/ProjectKnowledgeMapper.java
├── repository/ (4 repositories + ProjectRepository)
└── service/ProjectKnowledgeService.java
```

Storage: `com.company.learninghub.storage.ProjectKnowledgeStorageService` — local filesystem under `project-knowledge/` directory.

---

## 4. REST API (`/api/v1/projects`)

Controller: `ProjectKnowledgeController`  
Swagger tag: **Project Knowledge**

| Method | Path | Auth | Role requirement |
|--------|------|------|------------------|
| `POST` | `/projects` | JWT | Any authenticated user; creator becomes OWNER |
| `PUT` | `/projects/{id}` | JWT | OWNER or system ADMIN |
| `POST` | `/projects/{id}/archive` | JWT | OWNER or system ADMIN |
| `GET` | `/projects` | JWT | Accessible projects only (search, filter, paginate) |
| `GET` | `/projects/{id}` | JWT | Read access; includes `relatedTechnologies` |
| `POST` | `/projects/{id}/members` | JWT | OWNER or ADMIN |
| `GET` | `/projects/{id}/members` | JWT | Read access |
| `DELETE` | `/projects/{id}/members/{userId}` | JWT | OWNER or ADMIN |
| `POST` | `/projects/{id}/folders` | JWT | OWNER or CONTRIBUTOR |
| `PUT` | `/projects/{id}/folders/{folderId}` | JWT | OWNER or CONTRIBUTOR |
| `DELETE` | `/projects/{id}/folders/{folderId}` | JWT | OWNER; folder must be empty |
| `GET` | `/projects/{id}/folders` | JWT | Read access; filter by `parentId` |
| `POST` | `/projects/{id}/items/files` | JWT | OWNER or CONTRIBUTOR; multipart |
| `POST` | `/projects/{id}/items/links` | JWT | OWNER or CONTRIBUTOR |
| `PUT` | `/projects/{id}/items/{itemId}` | JWT | OWNER or CONTRIBUTOR |
| `DELETE` | `/projects/{id}/items/{itemId}` | JWT | OWNER |
| `GET` | `/projects/{id}/items` | JWT | Search within project |
| `GET` | `/projects/{id}/items/{itemId}` | JWT | Read access |
| `GET` | `/projects/{id}/items/{itemId}/download` | JWT | Read access; tracks access |
| `GET` | `/projects/{id}/items/{itemId}/link` | JWT | Read access; tracks access |

### Search capabilities today

- **Projects:** name and description (LIKE, case-insensitive)
- **Items:** title and description within a single project
- **No cross-project search**
- **No technology/environment/tag search on projects**

---

## 5. Authorization model (implemented)

### Global roles

- `ADMIN` — bypasses project membership checks for read/write where coded
- `EMPLOYEE` — subject to project access rules

### Project roles

| Role | Read | Contribute (folders/items) | Owner actions |
|------|------|---------------------------|---------------|
| OWNER | ✓ | ✓ | update project, archive, delete items/folders, manage members |
| CONTRIBUTOR | ✓ | ✓ create/update folders & items | — |
| VIEWER | ✓ | — | — |

### Access visibility

| `access_type` | Who can see |
|---------------|-------------|
| `PUBLIC` | All authenticated users |
| `MEMBERS_ONLY` | Members + system ADMIN only |
| Archived | Hidden from non-admin (404, not 403) |

**Pattern:** Uses 404 for unauthorized project access (consistent with Initiatives employee visibility).

---

## 6. Learn module integration (shipped)

### Backend

- `LearnTechnologyProjectLink` entity and `learn_technology_project_links` table
- Admin APIs on `LearnTechnologyManageController`:
  - `POST /learn/manage/technologies/{id}/project-links`
  - `DELETE /learn/manage/technologies/{id}/project-links/{projectId}`
- `LearnTechnologyService.listPublishedTechnologiesForProject(projectId)` — used by `ProjectKnowledgeService.getProject()`
- `TechnologyResponse.relatedProjects` on technology detail (employee-facing)
- Only **published** technologies appear on project detail

### Frontend

- `RelatedOrganizationProjectsCard` on technology detail → links to `/projects/{id}`
- `RelatedTechnologiesCard` on `ProjectKnowledgePage` (placeholder) → links to `/learn/technologies/{id}`
- Admin curation panel manages project links per technology

### Data flow

```text
Admin links Technology ↔ Project (learn_technology_project_links)
    ↓
Technology detail: "Used in Projects" list
Project detail API: relatedTechnologies[]
```

---

## 7. Initiative integration

**No database table, API, or UI links Projects to Initiatives.**

Planned Learn roadmap item **F21 — Optional Initiative Association** refers to **Initiative ↔ Certification** linkage, not Projects. See `docs/v0.8.0/09-implementation-plan-v2.md`.

---

## 8. Dashboard integration (partial)

`frontend/src/api/dashboardApi.ts`:

- **Admin:** "Recent Project Updates" — `projectsApi.list(size: 5, sort: updatedAtUtc)`
- **Employee:** "Assigned Projects" — same API call (not filtered by membership server-side; relies on search query returning accessible projects)

`DashboardPage.tsx` displays project name and access type but **no navigation links** to project detail.

---

## 9. Frontend (placeholder)

| Route | Component | State |
|-------|-----------|-------|
| `/projects` | `ProjectsPage` | `PlaceholderPanel` only |
| `/projects/:projectId` | `ProjectKnowledgePage` | Placeholder + `RelatedTechnologiesCard` |

`frontend/src/api/projectsApi.ts` exposes only:

- `list(search?, pagination)`
- `get(projectId)`
- `knowledgeItems(projectId, search?)`

**No frontend methods** for folders, members, file upload, link create, or mutations.

Sidebar: "Projects" nav item exists (`navigation.tsx`).

---

## 10. Tests

| Test class | Coverage |
|------------|----------|
| `ProjectKnowledgeServiceTest` | Create project + owner membership, access control, folders, file upload rollback, link access tracking |
| `ProjectKnowledgeMethodSecurityTest` | `@PreAuthorize` on service methods |
| `ProjectKnowledgeStorageServiceTest` | Local file store/load/delete |
| `LearnTechnologyServiceTest` | Project link add/remove (partial) |

**Missing:** Controller integration tests, frontend tests, E2E project flows.

---

## 11. Audit trail (current)

| Entity | created_at | updated_at | created_by / uploaded_by | Change history |
|--------|------------|------------|--------------------------|----------------|
| Project | ✓ | ✓ | created_by | ✗ |
| Folder | ✓ | ✓ | created_by | ✗ |
| Item | ✓ | ✓ | uploaded_by | ✗ |
| Access | — | — | accessed_by + accessed_at_utc | Per-event only |

**No:** `updated_by`, `last_reviewed_at`, resource owner, stale flags, version history.

---

## 12. Seed data

- **No `INSERT INTO projects`** in any migration
- Projects must be created via API or manual DB insert for testing
- Dashboard project widgets appear empty in fresh environments

---

## 13. Naming and packaging notes

- Package name `projectknowledge` reflects original "knowledge repository" scope
- Product direction shifts to **Project Module** / **project portal** — rename is cosmetic until implementation
- Philosophy alignment: Learn module owns guidance; Project module should own **navigation to org systems**, not replicate Confluence/Jira/GitHub

---

## 14. Summary

The Project Module is **backend-complete for a v0 knowledge repository** but **product-incomplete** for the stated engineering portal vision. Substantial foundation exists and should be **extended**, not replaced. The largest gap is **frontend UX** and **structured domain concepts** (environments, repositories, credential references, overview metadata).
