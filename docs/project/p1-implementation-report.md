# Project Module P1 — Project Foundation & Overview Portal — Implementation Report

**Phase:** P1  
**Branch:** `cursor/project-module-p1-foundation-63ba`  
**Status:** Complete  
**P2–P5:** Not started

---

## 1. Impact Analysis

| Area | Impact |
|------|--------|
| **Existing behavior** | Project list/detail APIs enriched; dashboard "My Projects" now membership-based (`assigned=true`); project creation restricted to ADMIN |
| **Schema** | `projects.status` added (V17); `archived` boolean kept in sync via entity helper |
| **APIs reused** | `GET/PUT /projects`, members APIs, Learn project-link APIs, `learn_technology_project_links` |
| **APIs changed** | `POST /projects` ADMIN-only; list/detail responses enriched; list adds `status`, `assigned` query params |
| **Frontend** | Placeholder `ProjectKnowledgePage` replaced by `ProjectsPage` + `ProjectDetailPage` portal |
| **Authorization** | Backend `@PreAuthorize` on create; visibility and membership rules unchanged for content |
| **Migration risk** | Low — V17 backfills ACTIVE/ARCHIVED from existing rows; regression test added |

---

## 2. Existing Functionality Reused

- V6 tables: `projects`, `project_members`, `project_knowledge_*`
- Membership roles: OWNER, CONTRIBUTOR, VIEWER
- PUBLIC / MEMBERS_ONLY visibility model
- `learn_technology_project_links` for Technology ↔ Project cross-navigation
- Admin technology curation project-link APIs
- Project member management APIs (exposed in P1 via Manage Members dialog for admin/owner)

---

## 3. Files Changed

### Backend

| Path | Change |
|------|--------|
| `V17__project_status.sql` | New migration |
| `ProjectStatus.java` | New enum |
| `Project.java` | `status` field + `applyStatus()` |
| `ProjectResponse.java`, `UpdateProjectRequest.java` | Status, owner, memberCount, role, technologies |
| `ProjectRepository.java` | Visibility, assigned filter, search pattern fix |
| `ProjectMemberRepository.java` | Count, batch owner/role lookups |
| `LearnTechnologyProjectLinkRepository.java` | Visible-by-technology query with archived filter |
| `ProjectKnowledgeService.java` | Admin-only create, enrichment, assigned filter |
| `LearnTechnologyService.java` | Employee project-link visibility filtering |
| `ProjectKnowledgeController.java` | `status`, `assigned` params |
| Tests: service, security, integration, migration | P1 coverage |

### Frontend

| Path | Change |
|------|--------|
| `types/projects.ts` | Project types |
| `api/projectsApi.ts` | Full client |
| `pages/projects/ProjectsPage.tsx` | Discovery list |
| `pages/projects/ProjectDetailPage.tsx` | Overview portal |
| `components/projects/*` | Cards, filters, dialogs, areas panel |
| `dashboardApi.ts`, `DashboardPage.tsx` | Assigned projects + links |
| `AppRoutes.tsx` | Project detail route |
| `TechnologyCurationPanel.tsx` | Updated `projectsApi.list` signature |
| `scripts/capture-project-portal-screenshots.mjs` | Responsive screenshots |

### Documentation

- `docs/project/README.md`, `docs/project-roadmap.md`, `README.md`
- `.cursor/project-context.md`, `.cursor/architecture.md`
- `docs/development-workflow.md` (Flyway version note)
- `scripts/p1-e2e-smoke.sh`

---

## 4. Database Changes

**Migration:** `V17__project_status.sql`

- Adds `status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'`
- Backfills `ARCHIVED` where `archived = TRUE`
- Check constraint + index on `status`

**Regression:** `ProjectStatusFlywayMigrationTest` (Testcontainers; skipped when Docker unavailable)

---

## 5. Backend Changes

- `ProjectStatus`: ACTIVE, ON_HOLD, COMPLETED, ARCHIVED
- List/detail enrichment: owner, member count, current member role, related technologies (with `shortName`)
- `assignedOnly` filter for membership-based discovery
- Archived projects excluded from normal employee list unless admin sets `includeArchived=true`
- PostgreSQL `lower(bytea)` fix: pre-built `searchPattern` in service layer
- Admin-only `@PreAuthorize("hasRole('ADMIN')")` on `createProject`

---

## 6. API Changes

| Method | Path | Role / access | Purpose | Change |
|--------|------|---------------|---------|--------|
| POST | `/api/v1/projects` | **ADMIN** | Create project | **Changed** — was broader |
| GET | `/api/v1/projects` | ADMIN, EMPLOYEE | List/search projects | **Changed** — `status`, `assigned`, enriched body |
| GET | `/api/v1/projects/{id}` | Visibility-aware | Project overview | **Changed** — enriched body |
| PUT | `/api/v1/projects/{id}` | OWNER, ADMIN | Update metadata | **Changed** — `status` in body |
| POST | `/api/v1/projects/{id}/archive` | OWNER, ADMIN | Archive | Existing |
| GET/POST/PUT/DELETE | `/api/v1/projects/{id}/members` | Per matrix | Member management | Existing |
| GET/POST | `/api/v1/learn/technologies/{id}` | Employee/Admin | Tech detail + related projects | **Changed** — visibility filter for employees |
| POST/DELETE | `/api/v1/learn/manage/technologies/{id}/project-links` | ADMIN | Link management | Existing |

---

## 7. Frontend Changes

- **Projects list:** search, status/access filters, assigned toggle, responsive cards, pagination, admin create
- **Project overview:** hero with status/visibility/role, about/metadata/team sections, technology stack, project areas (Coming Soon for P2+), admin edit/members actions
- **Dashboard:** assigned projects link to `/projects/:id`
- Removed misleading placeholder knowledge page

---

## 8. Permission Rules

| Action | ADMIN | OWNER | CONTRIBUTOR | VIEWER | Employee (non-member) |
|--------|-------|-------|-------------|--------|----------------------|
| Create project | ✓ | — | — | — | ✗ (403) |
| Edit project metadata | ✓ | ✓ | ✗ | ✗ | ✗ |
| Manage members | ✓ | ✓ | ✗ | ✗ | ✗ |
| View PUBLIC project | ✓ | ✓ | ✓ | ✓ | ✓ |
| View MEMBERS_ONLY project | ✓ | ✓ (if member) | ✓ (if member) | ✓ (if member) | ✗ |

---

## 9. Project Visibility Rules

- **PUBLIC:** visible in employee project discovery
- **MEMBERS_ONLY:** visible only to members and admins
- **ARCHIVED / status ARCHIVED:** hidden from normal employee discovery; admins may include via `includeArchived=true`
- **Assigned filter (`assigned=true`):** membership only — public projects without membership are excluded
- **Learn "Used in Projects":** employees do not see MEMBERS_ONLY projects unless they are members

---

## 10. Project ↔ Technology Integration

- Reuses `learn_technology_project_links` (no new junction table)
- Project overview → Technology Stack → `/learn/technologies/:id`
- Technology detail → Related Projects → `/projects/:id` (existing Learn UI)
- Employee cannot access MEMBERS_ONLY project via technology link alone

---

## 11. Automated Test Results

| Suite | Result |
|-------|--------|
| Backend P1 unit/security (`ProjectKnowledge*`, `LearnTechnology*`) | **30 passed** |
| `ProjectModuleP1IntegrationTest` | **Skipped** (Testcontainers Docker API version mismatch in CI VM) |
| `ProjectStatusFlywayMigrationTest` | **Skipped** (same) |
| Frontend Vitest | **415 passed** (95 files) |

---

## 12. Build Results

| Step | Result |
|------|--------|
| `mvn -f backend/pom.xml compile` | ✓ Pass |
| `mvn -f backend/pom.xml package -DskipTests` | ✓ Pass |
| `cd frontend && npm run build` | ✓ Pass |

Docker image build not executed (Docker overlay mount failure in environment).

---

## 13. Flyway Verification

Native PostgreSQL runtime:

```
Successfully applied 17 migrations … now at version v17
```

`flyway_schema_history`: version 17 = `project status`

---

## 14. Backend Startup Verification

```
Started LearningHubApplication
GET /api/v1/health → {"status":"UP"}
```

---

## 15. Frontend Runtime Verification

```
npm run dev → http://localhost:5173 — serving
```

---

## 16. End-to-End Test Results

Script: `scripts/p1-e2e-smoke.sh` against running backend + PostgreSQL

| Step | Result |
|------|--------|
| Employee create project → 403 | ✓ |
| Admin create project | ✓ |
| Employee blocked from MEMBERS_ONLY detail | ✓ |
| Admin project overview | ✓ |
| Employee `assigned=true` empty when not member | ✓ |
| Technology cross-nav respects access | ✓ |
| Learn regression (`/learn/journey`, search, roadmap) | ✓ |

---

## 17. Restart / Persistence Verification

- Projects count **4** before and after backend restart
- Flyway remains at V17 after restart

---

## 18. Learn Regression Results

With catalog import enabled at startup:

| Endpoint | Status |
|----------|--------|
| `GET /learn/journey` | 200 |
| `GET /learn/technologies?search=spring` | 200 |
| `GET /learn/technologies/spring-boot/roadmap` | 200 |

---

## 19. Responsive UI Verification

Playwright screenshots at 1280px, 834px, 390px:

`docs/screenshots/p1-project-portal/`

- `p1-projects-list-{desktop,tablet,mobile}.png`
- `p1-project-overview-{desktop,tablet,mobile}.png`

---

## 20. Screenshots

See `docs/screenshots/p1-project-portal/`.

---

## 21. Risks and Known Limitations

- Integration tests using Testcontainers skip when Docker client API is incompatible
- Docker Compose build not verified in this environment
- P1 does not implement Knowledge Base folder UI, environments, repositories, or credential references
- Recent Activity section deferred (no reliable activity model)
- Technology linking in P1 uses existing admin curation panel, not a new project-side UI

---

## 22. Documentation Updates

- `docs/project/README.md` — P1 status
- `docs/project-roadmap.md` — P1 complete
- `README.md`, `.cursor/project-context.md`, `.cursor/architecture.md`
- `docs/development-workflow.md` — Flyway V17 note
- This report

---

## 23. P2 Readiness

P1 delivers the portal shell and overview IA. P2 can attach Knowledge Base folder navigation to the existing backend folder/item APIs and the "Coming Soon" Knowledge Base area entry.

---

## 24. Explicit Confirmation

- **P2 not started**
- **P3 not started**
- **P4 not started**
- **P5 not started**
- **Leaderboards not started**
- **Email not started**
- **AI Assistant not started**
- **Initiative ↔ Project not started**
- **Version 2 Learn catalog expansion not started**
