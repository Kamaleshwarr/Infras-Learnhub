# Project Module — Risks and Open Decisions

**Date:** 2026-07-06

---

## 1. Open decisions requiring approval

| ID | Decision | Options | Recommendation |
|----|----------|---------|----------------|
| OD-01 | **Who can create projects?** | A) Any employee (current API) B) Admin only C) Admin + allowlist | **B — Admin only** for enterprise control |
| OD-02 | **Project status enum values** | Minimal: ACTIVE, ON_HOLD, DEPRECATED vs extended | Start minimal + `archived` flag |
| OD-03 | **MVP document strategy** | Links only vs enable file upload | **Links only** P1–P4 |
| OD-04 | **Folder depth in UI** | 1-level, 2-level, unlimited | **2-level** under Knowledge Base |
| OD-05 | **Credential ref visibility** | All project readers vs Contributor+ | All readers (same as project access) |
| OD-06 | **Package rename** | Keep `projectknowledge` vs rename to `project` | Defer rename until P3+ |
| OD-07 | **Initiative ↔ Project timing** | With P4 vs post-P5 | **Post-P5** — design junction only now |
| OD-08 | **Seed/demo data** | Migration seed vs setup script | Admin setup script + docs |
| OD-09 | **Dashboard "assigned" semantics** | Membership filter vs all accessible | **Membership filter** |
| OD-10 | **Global search ownership** | Project P4 vs Learn F22 | Shared search service in F22; project search in P4 |

---

## 2. Technical risks

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Stale/outdated links | High | High | Review metadata P5; owner assignment; future link checker |
| Users paste secrets into forms | Medium | Critical | Blocklist validation; UI warnings; security review |
| File upload scope creep | Medium | Medium | Disable UI by default; links-only MVP |
| Empty project adoption | Medium | Medium | Folder templates; admin seed guide; dashboard visibility |
| `KnowledgeCategory` migration | Low | Medium | Additive `resource_type`; map old values; no data loss |
| Search performance | Low | Medium | Indexes in P4; paginate |
| MEMBERS_ONLY 404 confusion | Low | Low | Consistent copy with Initiatives |
| Monolithic service growth | Medium | Low | Extract sub-services at P3 |
| Learn link regression | Low | High | Regression checklist includes F16 cross-nav |
| S3/cloud storage for files | Medium | Medium | Defer; local storage exists for dev only |

---

## 3. Sunk-cost and migration impact

### Preserve (high value)

- V6 schema and all 5 tables
- Authorization model and tests
- Learn technology junction — **zero migration**
- Storage service for optional future files

### Refactor (controlled)

- `KnowledgeCategory` → `ProjectResourceType` with backfill
- Dashboard API semantics for assigned projects
- Frontend placeholders → full portal (expected)

### No removal planned

- File upload API (keep dormant until P5+ decision)
- Access events table
- Recursive folder DB capability

### Migration impact rating

| Area | Impact |
|------|--------|
| Database | **Low–Medium** — additive migrations V17–V21 |
| Backend API | **Medium** — extend DTOs; new endpoints |
| Frontend | **High** — new module UI (expected; placeholder today) |
| Learn integration | **None** for MVP |
| Initiatives | **None** until future junction |

---

## 4. Product risks

| Risk | Mitigation |
|------|------------|
| Portal becomes file explorer | Fixed sections + 2-level folders |
| Duplicates Confluence | Links only; no editor |
| Low maintainer engagement | Resource owners, review reminders (P5) |
| Inconsistent project structure | Folder templates on create |
| Technology links unmaintained | Admin-only link management in Learn |

---

## 5. Organizational risks

| Risk | Mitigation |
|------|------------|
| No canonical project list | Admin owns project creation (OD-01) |
| Competing wiki mandate | Position as navigation hub, not wiki replacement |
| Secret manager varies by team | `CredentialProvider` enum + OTHER |

---

## 6. Quality gate risks

Per [development-workflow.md](../development-workflow.md), phases fail if:

- E2E not run on actual application
- Regression of Learn F16–F18/v1.1 not verified
- Flyway not tested on fresh DB

**Mitigation:** Include Learn cross-nav in every phase regression checklist.

---

## 7. Decision log template (for implementation)

When implementing, record decisions in phase implementation reports:

```text
Decision: OD-0X
Chosen: ...
Rationale: ...
Approved by: ...
Date: ...
```
