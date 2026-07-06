# Project Module — Gap Analysis

**Date:** 2026-07-06  
**Comparison:** Current state (audit) vs target Project Module vision

Classification key: **KEEP** · **EXTEND** · **REFACTOR** · **REMOVE** · **NEW**

---

## 1. Capability matrix

| Capability | Classification | Current | Target | Rationale |
|------------|----------------|---------|--------|-----------|
| `projects` table | **KEEP** | name, description, access, archived | + status, owners, tags | Solid core; extend metadata |
| `project_members` | **KEEP** | OWNER/CONTRIBUTOR/VIEWER | Same + contact display | Fits maintainer model |
| PUBLIC / MEMBERS_ONLY | **KEEP** | Implemented | Same | Proven access pattern |
| Archive (soft) | **KEEP** | `archived` flag | Same | Aligns with governance |
| Project create on API | **EXTEND** | Any employee can create | Admin create + delegated maintainers (TBD) | May need governance policy |
| Recursive folders | **KEEP** (DB) / **REFACTOR** (UX) | Unlimited depth | 2-level UX for MVP; DB retains depth | Avoid file-explorer complexity |
| `project_knowledge_items` | **REFACTOR** | FILE + LINK, 8 categories | Generic resources + typed side entities | Categories too narrow for portal |
| File upload | **KEEP** (infra) / deprioritize MVP | Local storage, full API | Links-only MVP; files in P5+ | Aligns with "navigation not knowledge" |
| Link items | **KEEP** | external_url, access tracking | Primary MVP content type | Core navigation primitive |
| `KnowledgeCategory` enum | **REFACTOR** | 8 KT-focused values | Richer `ProjectResourceType` | Missing API docs, env, CI/CD, etc. |
| Access events | **EXTEND** | download/link only | + optional change events later | Foundation for audit |
| Project search (list) | **EXTEND** | name, description | + tags, tech, env, items | Deterministic global search |
| Item search | **KEEP** | per-project title/desc | + type, folder, tags | Extend filters |
| Learn tech ↔ project links | **KEEP** | Junction + admin APIs | Enrich UI both directions | Do not duplicate |
| Technology on project detail | **KEEP** | `relatedTechnologies` in API | Technology Stack section on overview | Already wired |
| Initiative ↔ Project | **NEW** (future) | None | Optional M:N association | Document only in this phase |
| Project overview page | **NEW** | No UI | Rich landing per project | Primary UX entry |
| Quick links | **NEW** | None | Pinned high-value URLs | Overview surfacing |
| Environments | **NEW** | None | Named envs + typed URL refs | Core operational need |
| Repositories | **NEW** | Could be link items | First-class entity | Better search/AI structure |
| Credential references | **NEW** | None | Provider + non-secret ref | Security requirement |
| Team & contacts | **EXTEND** | members list API | Roles + contact metadata on overview | Extend members or contacts table |
| Recent updates feed | **NEW** | Dashboard list only | Project-scoped activity | Uses updated_at + events |
| Stale / review metadata | **NEW** | None | last_reviewed, owner, badge | Lightweight freshness |
| Project status | **NEW** | None | ACTIVE, ON_HOLD, etc. | Overview and filtering |
| Frontend list page | **NEW** | Placeholder | Searchable project catalog | Critical gap |
| Frontend detail/knowledge | **NEW** | Placeholder | Full portal sections | Critical gap |
| Admin project management | **EXTEND** | API-only | UI for create/archive/members | Needed for adoption |
| Dashboard drilldowns | **EXTEND** | Display only | Link to `/projects/{id}` | Small UX win |
| Global unified search | **NEW** | None | Cross-project (F22 overlap) | Phase P4 |
| Email notifications | **NEW** (future) | None | Domain events | Design hooks only |
| AI Assistant | **NEW** (future) | None | Structured retrieval | Design hooks only |
| Package name `projectknowledge` | **REFACTOR** (optional) | Legacy name | `project` when convenient | Cosmetic; not blocking |
| Swagger tag "Project Knowledge" | **REFACTOR** | Legacy | "Projects" | API naming consistency |

---

## 2. Sunk-cost assessment

### High reuse value (low migration risk)

| Asset | Investment | Reuse strategy |
|-------|------------|----------------|
| V6 schema + entities | Significant | Extend with new tables; avoid renaming core tables |
| `ProjectKnowledgeService` authorization | Significant | Extend service; extract sub-services if needed |
| Member role model | Medium | Keep OWNER/CONTRIBUTOR/VIEWER |
| Folder hierarchy logic | Medium | Keep; constrain UI depth in MVP |
| Storage service | Medium | Retain for future file support |
| Learn junction | Medium | **Do not touch** — extend UI only |
| Test suite | Medium | Extend; refactor tests if enums change |

### Refactor cost (moderate)

| Area | Risk | Mitigation |
|------|------|------------|
| `KnowledgeCategory` → `ProjectResourceType` | DB CHECK constraint migration | Map old values; additive enum expansion first |
| Item model generalization | API contract change | Version DTOs; deprecate gradually |
| "Assigned projects" dashboard semantics | API doesn't filter by membership | Add `?membership=mine` or dedicated endpoint |

### Low sunk cost (safe to deprioritize)

| Area | Notes |
|------|-------|
| Frontend placeholders | No user investment |
| KT-specific categories (KT_VIDEOS, KT_DOCUMENTS) | Can map to generic types |
| File upload UI | Never shipped |

---

## 3. Migration impact summary

| Change type | Flyway impact | API impact | Frontend impact |
|-------------|---------------|------------|-----------------|
| Add project metadata columns | New migration V17+ | Extend `ProjectResponse` | Overview UI |
| Add `project_environments` tables | New migration | New endpoints | Environments section |
| Add `project_repositories` | New migration | New endpoints | Repositories section |
| Add `project_credential_references` | New migration | New endpoints | Restricted section |
| Extend item type enum | ALTER CHECK or new column | Extend item DTOs | Resource cards |
| Add review columns on items | New columns nullable | Optional fields | Stale badge |
| Initiative junction (future) | New table | New association API | Initiative detail card |

**No tables should be dropped** in early phases. Archive old categories via mapping, not deletion.

---

## 4. What NOT to build (explicit boundaries)

Per product philosophy, the Project Module must **not** become:

| System | Boundary |
|--------|----------|
| GitHub / GitLab | Store repo **links** only |
| Jira / TAPD | Link to boards; no issue sync |
| Confluence / Wiki | Link to pages; no wiki editor |
| SharePoint / Drive | Link to folders; no file browser |
| Secrets manager | **Reference only** — never store secrets |
| CI/CD platform | Link to pipelines; no build execution |
| Monitoring | Link to dashboards; no metric ingestion |

---

## 5. Priority gaps (implementation order)

1. **Frontend foundation** — list + overview (no full portal value without UI)
2. **Project metadata** — status, owners for useful overview
3. **Link-based knowledge base** — folders + external links (reuse existing API)
4. **Environments & repositories** — structured operational navigation
5. **Search & cross-nav polish** — discoverability
6. **Governance** — review metadata, credential refs, stale indicators

---

## 6. Classification summary

| Classification | Count (approx.) |
|----------------|-----------------|
| KEEP | 12 |
| EXTEND | 10 |
| REFACTOR | 6 |
| REMOVE | 0 |
| NEW | 15 |

**Conclusion:** The existing backend is a **foundation to extend**, not a failed experiment. Zero **REMOVE** classifications for core tables. Primary work is **NEW** product surfaces and **REFACTOR** of the item taxonomy toward a portal model.
