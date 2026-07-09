# Project Module — Recommended Architecture

**Date:** 2026-07-06  
**Decision:** **Option C — Hybrid model**  
**Status:** Proposed for approval

---

## 1. Architecture decision record

| Field | Value |
|-------|-------|
| Decision | Hybrid: typed entities for Environments, Repositories, Credential References; generic Resources in folders for everything else |
| Context | Existing V6 project knowledge backend; Learn v1.1 complete; portal UX not started |
| Consequences | New Flyway migrations; extend `ProjectKnowledgeService` or add focused sub-services; frontend built in phases |

---

## 2. Logical architecture

```text
┌─────────────────────────────────────────────────────────────────┐
│                     React Project Portal (UI)                      │
│  List · Overview · KB · Environments · Repos · Team · Settings  │
└────────────────────────────┬────────────────────────────────────┘
                             │ /api/v1/projects/*
┌────────────────────────────▼────────────────────────────────────┐
│              ProjectModule (Spring Boot services)                  │
│  ProjectService · ResourceService · EnvironmentService             │
│  RepositoryService · CredentialReferenceService · SearchService    │
│  (may start as ProjectKnowledgeService extensions)                 │
└─────┬──────────────┬──────────────┬──────────────┬───────────────┘
      │              │              │              │
      ▼              ▼              ▼              ▼
 projects      project_         project_       learn_technology_
 + members     resources        environments   project_links
               + folders        + repos
                                + credential_refs
```

---

## 3. Module boundaries

| Boundary | Inside Project Module | Outside |
|----------|----------------------|---------|
| Project metadata | ✓ | |
| Knowledge organization | ✓ | |
| Environment URL registry | ✓ | |
| Repo links | ✓ | |
| Credential references | ✓ | |
| Technology association | Junction only | Learn owns technology catalog |
| Initiative association | Future junction | Initiatives module owns lifecycle |
| Secret storage | ✗ | Vault / cloud SM |
| Document content | ✗ | Confluence, SharePoint, Git |
| Issue tracking | ✗ | Jira, TAPD |

---

## 4. Core domain model

### 4.1 Project aggregate root

`Project` — metadata, access, status, owners, members.

### 4.2 Operational entities (typed)

| Entity | Responsibility |
|--------|----------------|
| `ProjectRepository` (name clash with JPA — use `ProjectRepoLink` in code) | Git remote navigation |
| `ProjectEnvironment` | Named runtime target |
| `ProjectEnvironmentReference` | URL or credential pointer per env |
| `ProjectCredentialReference` | Vault pointer, never secret value |

### 4.3 Knowledge entities (generic)

| Entity | Responsibility |
|--------|----------------|
| `ProjectKnowledgeFolder` | 2-level UX-organized hierarchy |
| `ProjectResource` | External link (MVP) with `resource_type` enum |

### 4.4 Integration entities (existing)

| Entity | Owner |
|--------|-------|
| `LearnTechnologyProjectLink` | Learn module — **do not duplicate** |

---

## 5. API design (target)

Base path remains `/api/v1/projects`.

### Existing endpoints — KEEP

All current project, member, folder, item endpoints remain. Items gain extended DTO fields.

### New endpoints (proposed)

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/projects/{id}/overview` | Aggregated overview DTO (optional convenience) |
| `CRUD` | `/projects/{id}/repositories` | Repository links |
| `CRUD` | `/projects/{id}/environments` | Environments |
| `CRUD` | `/projects/{id}/environments/{envId}/references` | Env URLs |
| `CRUD` | `/projects/{id}/credential-references` | Vault pointers |
| `GET` | `/projects/search` | Cross-project search (or extend `GET /projects`) |
| `POST` | `/projects/{id}/folders/apply-template` | Seed KB folder structure |

---

## 6. Frontend architecture

```text
frontend/src/
├── api/projectsApi.ts          # extend
├── types/projects.ts           # new
├── pages/projects/
│   ├── ProjectListPage.tsx     # replace placeholder
│   ├── ProjectDetailPage.tsx   # shell with tabs
│   ├── ProjectOverviewTab.tsx
│   ├── ProjectKnowledgeTab.tsx
│   ├── ProjectEnvironmentsTab.tsx
│   ├── ProjectRepositoriesTab.tsx
│   ├── ProjectTeamTab.tsx
│   └── ProjectSettingsTab.tsx
└── components/projects/
    ├── ProjectCard.tsx
    ├── QuickLinksPanel.tsx
    ├── EnvironmentCard.tsx
    ├── ResourceList.tsx
    └── projectFormState.ts
```

Reuse patterns from Initiatives: confirm dialogs, snackbars, role-gated actions.

---

## 7. Key design rules

1. **Overview is the default tab** — not Knowledge Base.
2. **Links are the MVP content type** — align with Learn module philosophy.
3. **Environments and repos are not folders** — dedicated typed tables and UI sections.
4. **Credential references never contain secrets** — service-layer blocklist validation.
5. **Technology links stay in Learn admin** — project UI shows read-only stack.
6. **Project roles stay OWNER/CONTRIBUTOR/VIEWER** — no new global roles.
7. **2-level folder UX cap** for MVP — database unlimited.
8. **Audit columns added incrementally** — `updated_by`, `last_reviewed_at` nullable first.

---

## 8. Service decomposition (implementation guidance)

**Phase 1:** Extend monolithic `ProjectKnowledgeService` — acceptable.

**Phase 3+:** Extract when file grows:

- `ProjectEnvironmentService`
- `ProjectRepositoryLinkService`
- `ProjectCredentialReferenceService`
- `ProjectSearchService`

Keep shared `requireReadAccess` / `requireContributor` / `requireOwner` in a `ProjectAuthorizationHelper`.

---

## 9. Alignment with platform philosophy

> Engineering Learning Hub owns **guidance and navigation**, not knowledge.

| Module | Applies how |
|--------|-------------|
| Learn | External learning URLs | 
| **Project** | External operational URLs + structured env/repo registry |
| Initiatives | Certification workflow — future optional links |

---

## 10. Approval checklist

- [ ] Hybrid model (Option C) approved
- [ ] MVP links-only for documents approved
- [ ] 2-level folder UX cap approved
- [ ] Project create policy decided (admin-only vs open)
- [ ] Credential reference visibility policy decided
- [ ] Initiative ↔ Project deferred to post-MVP
