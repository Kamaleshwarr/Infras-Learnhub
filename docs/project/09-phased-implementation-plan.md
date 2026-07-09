# Project Module — Phased Implementation Plan

**Date:** 2026-07-06  
**Prerequisite:** Architecture approval ([08-recommended-architecture.md](./08-recommended-architecture.md))

All phases must complete the mandatory [11-step development workflow](../development-workflow.md) before being marked done.

**Phase numbering:** P1–P5 (Project module). Not to be confused with Learn F16–F22.

---

## Phase overview

| Phase | Name | Depends on |
|-------|------|------------|
| **P1** | Project Foundation & Overview | Learn v1.1 (complete) |
| **P2** | Knowledge Base & Folders | P1 |
| **P3** | Environments & Repositories | P1 |
| **P4** | Project Search & Cross-Navigation | P2, P3 |
| **P5** | Governance, Review Metadata & Polish | P2, P3 |

P2 and P3 may run **in parallel** after P1 if staffed separately.

---

## P1 — Project Foundation & Overview

### Objective

Ship a usable **project list and overview portal** on top of existing APIs. Establish frontend patterns and extend project metadata.

### Backend scope

- Flyway V17: add `status`, `technical_owner_id`, `qa_owner_id`, `tags`, `updated_by` to `projects`
- Extend `ProjectResponse` / update DTOs
- Optional: `GET /projects/{id}/overview` aggregate endpoint (or compose client-side)
- Fix dashboard semantics: `?assigned=true` filter for member projects (recommended)
- Keep existing authorization

### Frontend scope

- Replace `ProjectsPage` placeholder with searchable list (cards/table)
- Replace `ProjectKnowledgePage` with `ProjectDetailPage` shell + **Overview tab**
- Display: name, description, status, access, owners, members summary, tech stack (`RelatedTechnologiesCard`)
- Quick links panel (pinned items — requires `is_pinned` column or use first N links in P2)
- Dashboard list items link to `/projects/:id`
- Role-aware: hide settings for VIEWER

### Database impact

- V17 migration — additive columns only

### APIs

- Extend `PUT /projects/{id}` for new fields
- Extend `GET /projects` with status filter, `assigned` filter
- Existing member list for team summary

### Permissions

- Document decision: project create policy (recommend admin-only)
- Reuse OWNER/CONTRIBUTOR/VIEWER

### Tests

- Backend: service tests for new fields and assigned filter
- Frontend: list page render, overview load, role gating
- Extend `ProjectKnowledgeServiceTest`

### E2E scenarios

1. Login as admin → create project → appears in list
2. Login as employee → see PUBLIC project overview
3. MEMBERS_ONLY project → non-member gets 404
4. Technology linked in Learn → appears on project overview
5. Dashboard project row navigates to overview

### Dependencies

- Existing `/api/v1/projects` APIs
- `learn_technology_project_links` populated for demo

### Risks

- Empty environments in fresh DB — use seed script or admin setup guide
- "Assigned projects" currently not membership-filtered

### Exit criteria

- [ ] Steps 1–11 complete
- [ ] Employee can browse projects and view overview with tech stack
- [ ] Admin can create/edit project metadata
- [ ] Dashboard links work
- [ ] No P2/P3 code started in same release without approval

---

## P2 — Knowledge Base & Folders

### Objective

Ship **folder browser and link resource management** — primary knowledge organization.

### Backend scope

- Flyway V18: `resource_type` column (backfill from `category`), `is_pinned`, `sort_order` on folders
- `POST /projects/{id}/folders/apply-template` — seed Requirements, Technical Docs, QA, Deployment
- Extend link create/update for `resource_type`
- **No file upload UI** in this phase (API may remain for admin use)

### Frontend scope

- Knowledge Base tab with breadcrumb navigation
- 2-level folder browser
- Link resource create/edit drawer
- Pin to quick links (`is_pinned`)
- Item list with type icons and external open
- Search within project items (existing API)

### Database impact

- V18 migration

### APIs

- Existing folder + item link endpoints (primary)
- Template apply endpoint (new)

### Permissions

- CONTRIBUTOR+ for create/update links and folders
- OWNER for delete

### Tests

- Folder uniqueness, empty delete, template apply
- Frontend: folder navigation, link CRUD

### E2E scenarios

1. Apply folder template → 4 root folders created
2. Add subfolder under Technical Docs
3. Create API documentation link → opens externally
4. Pin link → appears on Overview quick links
5. VIEWER cannot see edit buttons

### Dependencies

- P1 project detail shell

### Risks

- Category → resource_type migration mapping errors
- Folder template idempotency (don't duplicate on re-apply)

### Exit criteria

- [ ] Steps 1–11 complete
- [ ] Team can organize and open external doc links
- [ ] Quick links on overview functional

---

## P3 — Environments & Repositories

### Objective

Structured **environment URL registry** and **repository links**.

### Backend scope

- Flyway V19: `project_environments`, `project_environment_references`, `project_repositories`
- CRUD services and controller endpoints
- Validation: HTTPS URLs, unique env names per project

### Frontend scope

- Environments tab with env cards and reference groups
- Repositories tab with provider icons
- Environment summary on Overview (from P1 shell)
- Repository summary on Overview

### Database impact

- V19 migration — new tables

### APIs

- Full CRUD for environments, references, repositories

### Permissions

- CONTRIBUTOR+ write, VIEWER read

### Tests

- Service tests per entity
- Env reference type validation
- Frontend tab tests

### E2E scenarios

1. Add QA environment with Swagger URL
2. Add GitHub repository marked primary
3. Overview shows env and repo summaries
4. Non-member cannot view MEMBERS_ONLY project envs

### Dependencies

- P1 overview shell (can ship env UI before summary wiring)

### Risks

- Name collision with generic link items — document that envs live in typed tables only

### Exit criteria

- [ ] Steps 1–11 complete
- [ ] "Where is QA Swagger?" answerable from UI
- [ ] Repository links searchable within project

---

## P4 — Project Search & Cross-Navigation

### Objective

**Discoverability** across projects, technologies, and resources.

### Backend scope

- Extend `GET /projects` search: tags, technology slug/id filter
- `GET /projects/resources/search` or global `GET /search/projects` (deterministic)
- Join queries across projects, resources, repos, environments
- Optional: PostgreSQL full-text indexes

### Frontend scope

- Project list technology filter
- Global search entry point (header or projects page) — may overlap F22
- Polish Learn ↔ Project cards on both sides
- Initiative placeholder card on overview ("Coming soon") — optional

### Database impact

- Indexes for search performance (V20)

### APIs

- Extended search parameters
- Cross-entity search endpoint

### Permissions

- Search respects project visibility (same as list)

### Tests

- Search returns only accessible projects
- Technology filter: "projects using Kafka"
- Regression: Learn project links unchanged

### E2E scenarios

1. Search "payments" → finds project by description
2. Filter by React technology → correct projects
3. Search "swagger" → finds env reference
4. Technology detail → project link → overview → tech stack round trip

### Dependencies

- P2 resources, P3 envs/repos for rich results

### Risks

- Performance on large datasets — paginate, index early
- Scope creep into F22 unified search

### Exit criteria

- [ ] Steps 1–11 complete
- [ ] Four example queries from product doc work
- [ ] Learn cross-nav regression passed

---

## P5 — Governance, Review Metadata & Polish

### Objective

**Freshness, accountability, credential references**, optional file upload.

### Backend scope

- Flyway V21: `project_credential_references`, review columns, `updated_by` on resources
- Stale badge computation (configurable threshold)
- Credential reference CRUD with secret blocklist validation
- Optional: enable file upload UI behind `app.projects.file-upload.enabled=false` default
- Optional: `project_audit_events` for activity feed

### Frontend scope

- Stale badge on resources
- Last reviewed date editor (CONTRIBUTOR+)
- Resource owner assignment
- Credential references in Settings (restricted section)
- Recent Updates feed on overview (real activity)
- Team settings polish

### Database impact

- V21 migration

### APIs

- Credential reference CRUD
- PATCH resource review metadata

### Permissions

- Credential refs: confirm visibility policy
- File upload: CONTRIBUTOR+ if enabled

### Tests

- Secret blocklist validation
- Stale threshold computation
- Security test: no secret patterns accepted

### E2E scenarios

1. Set last reviewed → stale badge clears
2. Create credential ref → shows provider + identifier, never secret
3. Archive project → hidden from employee search
4. (If enabled) Upload PDF → download with access tracking

### Dependencies

- P2 resources, P3 operational entities

### Risks

- Users paste secrets into reference field — blocklist + UX warnings
- File upload reopens storage/security scope

### Exit criteria

- [ ] Steps 1–11 complete
- [ ] Governance metadata usable
- [ ] Credential references meet security requirement
- [ ] Implementation report filed

---

## Post-P5 backlog (not scheduled)

| Item | Notes |
|------|-------|
| Initiative ↔ Project M:N | After F21/F22 Learn roadmap |
| Email notifications | Event hooks designed in P5 |
| AI Assistant | Structured data ready from P3–P4 |
| Broken link checker | Scheduled job |
| File upload production hardening | S3, virus scan |
| Project audit history UI | If audit_events table added |

---

## Flyway map (proposed)

| Migration | Phase | Contents |
|-----------|-------|----------|
| V17 | P1 | Project metadata columns |
| V18 | P2 | Resource type, pins, folder ordering |
| V19 | P3 | Environments, repos |
| V20 | P4 | Search indexes |
| V21 | P5 | Credential refs, review metadata |

*Note: V16 is `learn_resource_overrides` (shipped). Next available is V17.*

---

## Relationship to Learn roadmap

| Learn phase | Interaction with Project |
|-------------|-------------------------|
| F19–F20 | None required |
| F21 Initiative ↔ Certification | Do not conflate with Project |
| F22 Unified Search | Coordinate with P4 — avoid duplicate search UIs |

**Project Module can proceed independently** of F19–F21. Coordinate with F22 for global search UX.
