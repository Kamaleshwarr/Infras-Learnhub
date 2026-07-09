# Project Module — Product Architecture Proposal

**Date:** 2026-07-06

---

## 1. Product goal

The Project Module is the **internal project knowledge and navigation hub** for engineering teams.

### Problem

Project information is scattered across GitHub, Jira/TAPD, Confluence, SharePoint, Google Drive, Postman, Swagger, monitoring, CI/CD, runbooks, and credential stores. New hires and cross-team engineers waste time hunting for the right link.

### Solution

Provide **one organized place to discover and navigate** project information. The platform owns **structure, metadata, and links**. External systems own **content**.

### Platform positioning

| ELH owns | External systems own |
|----------|---------------------|
| Project catalog and overview | Source documents and wiki pages |
| Folder organization and resource types | Issue tracking and boards |
| Environment URL registry | Running applications |
| Repository link registry | Git history and PRs |
| Credential **references** | Actual secrets |
| Technology stack associations | Learning content |
| Access control and audit metadata | Tool-specific permissions |

---

## 2. Core project experience

```text
Projects
 └── Project A
      ├── Overview          ← default landing; high-signal summary
      ├── Quick Links       ← pinned operational URLs
      ├── Environments      ← DEV, QA, UAT, PROD, custom
      ├── Repositories      ← Git remotes
      ├── Knowledge Base    ← folder-organized deeper knowledge
      │    ├── Requirements
      │    ├── Technical Documentation
      │    │    ├── Architecture
      │    │    └── API Documentation
      │    ├── QA
      │    └── Deployment
      ├── Team & Contacts
      └── Recent Updates
```

This is a **portal**, not a file explorer. Top-level sections are **curated entry points**; the Knowledge Base holds deeper folder-organized material.

---

## 3. Folder nesting — MVP recommendation

### Database (already shipped)

`project_knowledge_folders.parent_id` supports **unlimited recursion**.

### UX recommendation for MVP

| Level | Purpose | Example |
|-------|---------|---------|
| L0 | Project root sections | Fixed nav: Overview, Environments, Repos, Knowledge Base |
| L1 | Knowledge Base top folders | Requirements, Technical Docs, QA, Deployment |
| L2 | Optional subfolders | Under Technical Docs: Architecture, API Docs |

**MVP UI cap: 2 folder levels** under Knowledge Base (parent + child). Database retains unlimited depth for future expansion.

### Rationale

| Approach | Pros | Cons |
|----------|------|------|
| Unlimited nesting in UI | Flexible | File-explorer UX; hard to search; inconsistent structure |
| Flat items only | Simple | Poor organization at scale |
| **2-level folders (recommended)** | Balanced; template-friendly | Occasional edge cases need links not depth |
| Fixed sections only (no folders) | Very simple | Cannot model diverse project needs |

**Decision:** Portal sections (fixed) + **optional 2-level knowledge folders** for MVP.

---

## 4. Project Overview design

### Primary audience

1. **Existing team member** — quick access to env URLs, repos, runbooks
2. **New employee** — understand scope, stack, contacts, onboarding links
3. **Cross-team engineer** — grasp what the project does and where to look

### Overview content blocks

| Block | Source | MVP |
|-------|--------|-----|
| Project name & short description | `projects` | ✓ |
| Project status | new `project_status` | ✓ |
| Project owner | `project_members` WHERE role=OWNER | ✓ |
| Technical owner | new field or designated member | ✓ |
| QA owner | new field or designated member | P1 or P2 |
| Access badge | `access_type`, archived | ✓ |
| Technology stack | `learn_technology_project_links` | ✓ (reuse) |
| Related initiatives | future junction | Future |
| Quick links | pinned resources | P1 |
| Repository summary | `project_repositories` top 3 | P3 |
| Environment summary | env names + primary app URL | P3 |
| Recent updates | items/folders by `updated_at` | P2 |
| Knowledge Base entry | link to KB root | P2 |
| Team contacts | members with roles | P1 |

### Layout principle

**Above the fold:** name, status, description, owners, quick links, tech stack.  
**Below:** environment/repo summaries, recent activity, KB entry.

---

## 5. Knowledge folders and item types

### Conceptual model

```text
Project
  → Knowledge Folder (optional hierarchy)
    → Project Resource (typed link or future file)
```

### Item type analysis

The prompt listed many types: `DOCUMENT_LINK`, `REPOSITORY`, `REQUIREMENT`, `API_DOCUMENTATION`, `POSTMAN_COLLECTION`, etc.

**Recommendation:** Do not put all types in one flat enum alone.

| Type category | Model |
|---------------|-------|
| Repositories | **Separate entity** (`project_repositories`) |
| Environments + URLs | **Separate entities** (`project_environments`, `project_environment_references`) |
| Credential refs | **Separate entity** (`project_credential_references`) |
| Everything else | **Generic `ProjectResource`** with `resource_type` enum |

### Generic resource types (enum)

| Type | Example |
|------|---------|
| `DOCUMENT_LINK` | Confluence page, Google Doc |
| `REQUIREMENT` | PRD in SharePoint |
| `API_DOCUMENTATION` | Swagger, OpenAPI portal |
| `POSTMAN_COLLECTION` | Postman workspace link |
| `ARCHITECTURE_DOCUMENT` | Diagrams, ADRs |
| `DASHBOARD` | Grafana, Kibana |
| `CI_CD_PIPELINE` | Jenkins, GitHub Actions |
| `RUNBOOK` | Ops doc |
| `WIKI` | Wiki page |
| `USEFUL_LINK` | General |
| `SECRET_REFERENCE` | Pointer to vault (see §7) |
| `OTHER` | Fallback |

Validation rules vary by type (e.g., `SECRET_REFERENCE` requires provider fields) — easier with typed metadata JSON column than 12 separate tables.

---

## 6. Environment management

### Requirements

- Projects have **different** environment sets
- Support standard names: LOCAL, DEV, QA, QA2, UAT, STAGING, PRE_PROD, PROD, DR
- Support **custom** names (e.g., "Perf Lab", "Demo East")
- Each environment has **multiple references** (app URL, API URL, Swagger, admin portal, monitoring, logs, etc.)

### Model

```text
project_environments
  id, project_id, name, code (optional slug), description, sort_order, is_production (flag)

project_environment_references
  id, environment_id, reference_type, title, url, description, sort_order
```

### `reference_type` examples

`APPLICATION_URL`, `API_URL`, `SWAGGER_URL`, `ADMIN_PORTAL`, `EMPLOYEE_PORTAL`, `MONITORING_DASHBOARD`, `LOG_DASHBOARD`, `CI_CD_DEPLOYMENT`, `DATABASE_REFERENCE`, `OTHER`

`DATABASE_REFERENCE` and `SECRET_REFERENCE` types link to **credential reference records**, not connection strings.

---

## 7. Credentials and secrets

### Security requirement (non-negotiable)

The application must **never** store passwords, API keys, tokens, private keys, or connection secrets in plaintext.

### Credential reference model

```text
project_credential_references
  id
  project_id
  environment_id (optional)
  name                    -- "QA Database Credentials"
  provider                -- AWS_SECRETS_MANAGER | AZURE_KEY_VAULT | HASHICORP_VAULT | CYBERARK | INTERNAL | OTHER
  reference_identifier    -- non-secret locator: secret name, ARN path, vault path
  access_instructions     -- optional text: "Request access via IAM role X"
  owner_user_id           -- who maintains this reference
  last_reviewed_at
  created_by, created_at, updated_at
```

### UX behavior

- Display name, provider, reference identifier, instructions
- **"Open in {Provider}"** button only if org supplies a **configured base URL template** (future config; not secret)
- Otherwise copy-friendly reference identifier
- Visible only to users with project read access; edit restricted to OWNER/CONTRIBUTOR
- **Never** render secret values from any API

### Permission note

Credential references reveal **where** secrets live. Treat visibility same as project read access; consider stricter CONTRIBUTOR-only view for highly sensitive projects (future option).

---

## 8. Document handling

### Options evaluated

| Option | Description |
|--------|-------------|
| A | Store uploaded files directly |
| B | External references only |
| C | Both |

### Analysis

| Factor | A: Upload | B: Links only | C: Both |
|--------|-----------|---------------|---------|
| Storage architecture | Local disk (exists); needs S3 for cloud | None | Hybrid complexity |
| File size / versioning | Hard | N/A | Hard |
| Security / virus scan | Required for uploads | Minimal | Partial |
| Backups | File + DB | DB only | Both |
| Access control | App-managed | External ACL + app gate | Mixed |
| Local dev | Works today | Works | Works |
| Complexity | High | **Low** | Highest |
| Philosophy fit | Poor ("owns navigation") | **Strong** | Moderate |

### Recommendation

| Phase | Approach |
|-------|----------|
| **MVP (P1–P4)** | **External links only** for documents. Reuse existing link item API (refactored types). |
| **P5+** | Optional file upload for edge cases using **existing** `ProjectKnowledgeStorageService` — behind feature flag, size limits, admin approval |
| **Future** | Org may mandate SharePoint/Confluence only — links remain primary |

---

## 9. Project search (deterministic)

### MVP search scope

| Index | Fields |
|-------|--------|
| Projects | name, description, tags, status |
| Resources | title, description, type, folder name |
| Repositories | name, URL |
| Environments | name, reference titles/URLs |
| Technologies | via junction — "projects using Kafka" |

### Example queries (UI filters + text)

| User question | Mechanism |
|---------------|-----------|
| Which project uses Kafka? | `GET /projects?technology=Kafka` or unified search |
| QA Swagger for Project A? | Project → Environments → QA → Swagger ref |
| Projects using React? | Technology filter on project list |
| Automation repo for Project B? | `GET /projects/{id}/repositories?search=automation` |

### Implementation approach

- **Phase 1:** Extend existing JPQL LIKE searches
- **Phase 2:** PostgreSQL `tsvector` on projects + resources (if performance requires)
- **Not in scope:** AI/semantic search

---

## 10. Stale information (design for later)

| Mechanism | MVP | Future |
|-----------|-----|--------|
| `last_reviewed_at` on resources | Column nullable | Review workflow |
| `resource_owner_user_id` | Column nullable | Accountability |
| Stale badge | Computed: `now - last_reviewed > threshold` | UI indicator |
| Review reminder | — | Email integration |
| Broken link check | — | Scheduled HEAD requests |

Default stale threshold: **90 days** (configurable per org later).

---

## 11. Product principles (checklist)

- [ ] Overview answers "what is this project?" in 30 seconds
- [ ] Every resource opens externally or navigates to approved vault reference
- [ ] No secret values in API responses or UI
- [ ] Folder structure uses templates, not blank canvas
- [ ] Technology and initiative cross-links are bidirectional where applicable
- [ ] Archived projects are hidden from employees but recoverable by admin
