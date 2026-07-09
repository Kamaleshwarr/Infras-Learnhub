# Project Module — Data Model Proposal

**Date:** 2026-07-06  
**Status:** Proposed — not implemented

---

## 1. Entity relationship overview

```text
users
  └── projects (created_by)
        ├── project_members (user_id, project_role)
        ├── project_repositories
        ├── project_environments
        │     └── project_environment_references
        ├── project_credential_references
        ├── project_knowledge_folders (parent_id self-ref)
        ├── project_knowledge_items / project_resources (evolved)
        └── learn_technology_project_links → learn_technologies

learning_initiatives
  └── initiative_project_links (FUTURE — not in MVP)
```

---

## 2. Existing tables — proposed extensions

### 2.1 `projects` — add columns (V17+)

| Column | Type | Notes |
|--------|------|-------|
| `status` | VARCHAR(30) | `ACTIVE`, `ON_HOLD`, `DEPRECATED`, `ARCHIVED` — or keep `archived` boolean + status |
| `technical_owner_id` | UUID FK → users, nullable | |
| `qa_owner_id` | UUID FK → users, nullable | |
| `tags` | TEXT[] or JSONB | Searchable labels |
| `updated_by` | UUID FK → users, nullable | Last metadata editor |

**Keep:** `name`, `description`, `access_type`, `archived`, `created_by`, timestamps.

### 2.2 `project_knowledge_items` — evolve to `project_resources` (optional rename)

**Phase approach:** Add columns to existing table first; rename table in later migration if desired.

| Column | Change |
|--------|--------|
| `resource_type` | New enum column (maps from `category` + expands) |
| `tags` | TEXT[] optional |
| `is_pinned` | BOOLEAN default false — quick links |
| `last_reviewed_at` | TIMESTAMPTZ nullable |
| `last_reviewed_by` | UUID FK nullable |
| `resource_owner_id` | UUID FK → users nullable |
| `metadata` | JSONB nullable — type-specific non-sensitive fields |

**Deprecate gradually:** `category` → mapped to `resource_type` during migration.

### 2.3 `project_knowledge_folders` — add columns

| Column | Purpose |
|--------|---------|
| `folder_kind` | `STANDARD`, `TEMPLATE_ROOT` — optional |
| `sort_order` | INT for consistent ordering |
| `icon_key` | Optional UI hint |

---

## 3. New tables

### 3.1 `project_repositories`

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | UUID | PK |
| `project_id` | UUID | FK → projects CASCADE |
| `name` | VARCHAR(200) | NOT NULL |
| `url` | TEXT | NOT NULL, HTTPS |
| `provider` | VARCHAR(30) | `GITHUB`, `GITLAB`, `BITBUCKET`, `AZURE_DEVOPS`, `OTHER` |
| `description` | TEXT | |
| `is_primary` | BOOLEAN | default false |
| `sort_order` | INT | |
| `created_by` | UUID | FK → users |
| `created_at`, `updated_at` | TIMESTAMPTZ | |

Unique: `(project_id, LOWER(name))` recommended.

### 3.2 `project_environments`

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | UUID | PK |
| `project_id` | UUID | FK CASCADE |
| `name` | VARCHAR(100) | NOT NULL — "QA", "UAT", "Perf Lab" |
| `code` | VARCHAR(30) | optional slug: `QA`, `PROD`, `CUSTOM` |
| `description` | TEXT | |
| `is_production` | BOOLEAN | default false |
| `sort_order` | INT | |
| `created_by` | UUID | FK |
| `created_at`, `updated_at` | TIMESTAMPTZ | |

Unique: `(project_id, LOWER(name))`.

### 3.3 `project_environment_references`

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | UUID | PK |
| `environment_id` | UUID | FK CASCADE |
| `reference_type` | VARCHAR(40) | See product doc |
| `title` | VARCHAR(200) | NOT NULL |
| `url` | TEXT | nullable for DB_REF types |
| `credential_reference_id` | UUID | FK nullable → credential refs |
| `description` | TEXT | |
| `sort_order` | INT | |
| `last_reviewed_at` | TIMESTAMPTZ | |
| `created_at`, `updated_at` | TIMESTAMPTZ | |

### 3.4 `project_credential_references`

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | UUID | PK |
| `project_id` | UUID | FK CASCADE |
| `environment_id` | UUID | FK nullable |
| `name` | VARCHAR(200) | NOT NULL |
| `provider` | VARCHAR(40) | NOT NULL |
| `reference_identifier` | VARCHAR(500) | NOT NULL — non-secret |
| `access_instructions` | TEXT | |
| `owner_user_id` | UUID | FK → users |
| `last_reviewed_at` | TIMESTAMPTZ | |
| `created_by` | UUID | FK |
| `created_at`, `updated_at` | TIMESTAMPTZ | |

**Validation:** Application layer rejects patterns resembling secrets (Bearer tokens, `password=`, PEM blocks).

### 3.5 `initiative_project_links` (FUTURE — design only)

| Column | Type |
|--------|------|
| `id` | UUID PK |
| `initiative_id` | UUID FK → learning_initiatives |
| `project_id` | UUID FK → projects |
| `created_at` | TIMESTAMPTZ |

Unique: `(initiative_id, project_id)` — **many-to-many**.

---

## 4. Enum definitions

### 4.1 `ProjectStatus`

`ACTIVE`, `ON_HOLD`, `DEPRECATED` — plus `archived` boolean or `ARCHIVED` status value.

### 4.2 `ProjectResourceType` (replaces/extends `KnowledgeCategory`)

| Value | Maps from old category |
|-------|------------------------|
| `REQUIREMENT` | REQUIREMENTS |
| `DOCUMENT_LINK` | KT_DOCUMENTS, EXTERNAL_LINKS |
| `ARCHITECTURE_DOCUMENT` | ARCHITECTURE_DOCUMENTS |
| `RELEASE_NOTES` | RELEASE_NOTES |
| `TEST_STRATEGY` | TEST_STRATEGY |
| `TEST_DATA` | TEST_DATA_DOCUMENTATION |
| `VIDEO_LINK` | KT_VIDEOS |
| `API_DOCUMENTATION` | NEW |
| `POSTMAN_COLLECTION` | NEW |
| `DASHBOARD` | NEW |
| `CI_CD_PIPELINE` | NEW |
| `RUNBOOK` | NEW |
| `WIKI` | NEW |
| `USEFUL_LINK` | NEW |
| `SECRET_REFERENCE` | NEW (or use dedicated table only) |
| `OTHER` | NEW |

### 4.3 `EnvironmentReferenceType`

`APPLICATION_URL`, `API_URL`, `SWAGGER_URL`, `ADMIN_PORTAL`, `EMPLOYEE_PORTAL`, `MONITORING_DASHBOARD`, `LOG_DASHBOARD`, `CI_CD_DEPLOYMENT`, `DATABASE_REFERENCE`, `OTHER`

### 4.4 `CredentialProvider`

`AWS_SECRETS_MANAGER`, `AZURE_KEY_VAULT`, `HASHICORP_VAULT`, `CYBERARK`, `INTERNAL`, `OTHER`

---

## 5. Audit and activity

### MVP audit fields (per entity)

| Field | Projects | Resources | Environments | Repos |
|-------|----------|-----------|--------------|-------|
| created_by | ✓ | ✓ (uploaded_by) | ✓ | ✓ |
| created_at | ✓ | ✓ | ✓ | ✓ |
| updated_at | ✓ | ✓ | ✓ | ✓ |
| updated_by | NEW | NEW | NEW | NEW |
| last_reviewed_at | — | NEW | on refs | — |
| resource_owner | — | NEW | — | — |

### Change history (post-MVP)

Optional `project_audit_events` table:

```text
id, project_id, entity_type, entity_id, action, actor_id, payload_json, occurred_at
```

Enables activity feed and email/AI hooks without polling all tables.

---

## 6. Search indexes (P4)

```sql
-- Example: GIN index for tags
CREATE INDEX idx_projects_tags ON projects USING GIN (tags);

-- Full-text (optional)
ALTER TABLE projects ADD COLUMN search_vector tsvector;
CREATE INDEX idx_projects_search ON projects USING GIN (search_vector);
```

---

## 7. Migration strategy

| Step | Migration | Risk |
|------|-----------|------|
| 1 | Add nullable columns to `projects` | Low |
| 2 | Add `project_repositories`, `project_environments`, refs tables | Low |
| 3 | Add `resource_type` column; backfill from `category` | Medium |
| 4 | Add review/owner columns to items | Low |
| 5 | Add `project_credential_references` | Low |
| 6 | (Future) Rename items table | Medium — coordinate API |

**Never modify V6 in place.** All changes via new Flyway versions.

---

## 8. AI-friendly structuring notes

For future AI Assistant retrieval, prioritize:

- Stable `resource_type` and `reference_type` enums
- Normalized environment names (`code` field)
- Explicit technology links (existing junction)
- `tags` on projects and resources
- `description` fields encouraged in UI validation
- Separate tables for repos/envs (clear intent for queries like "UAT Swagger URL")

Avoid burying meaning only in free-text folder names.
