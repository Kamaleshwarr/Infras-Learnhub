# Project Module — Future Compatibility Notes

**Date:** 2026-07-06  
**Scope:** Email notifications and AI Assistant — design hooks only, no implementation

---

## 1. Design principles for extensibility

1. **Structured entities** over free-text (hybrid architecture supports this)
2. **Explicit event points** at service layer after successful mutations
3. **Stable enums** for resource types, env reference types, providers
4. **Audit columns** (`created_by`, `updated_by`, timestamps) on all entities
5. **No secret values** in events or AI index payloads

---

## 2. Email notifications (future)

### Planned event types

| Event | Trigger | Suggested recipients |
|-------|---------|---------------------|
| `PROJECT_RESOURCE_UPDATED` | Resource title/URL changed | Project members (CONTRIBUTOR+) |
| `PROJECT_ENVIRONMENT_URL_CHANGED` | Env reference URL updated | OWNER + subscribers |
| `PROJECT_REQUIREMENT_ADDED` | New REQUIREMENT type resource | Project members |
| `PROJECT_RESOURCE_NEEDS_REVIEW` | Stale threshold exceeded | `resource_owner_id` |
| `PROJECT_ARCHIVED` | Archive action | All members |
| `PROJECT_MEMBER_ADDED` | New membership | Added user + OWNER |

### Integration architecture (future)

```text
ProjectService mutation
    → ApplicationEventPublisher
        → ProjectDomainEvent (record)
            → NotificationDispatcher (future)
                → Email channel (future)
                → In-app notification (extend v0.6 infra)
```

### MVP preparation (no email code)

| Preparation | Phase |
|-------------|-------|
| `updated_by` columns | P1/P5 |
| `last_reviewed_at` | P5 |
| Optional `project_audit_events` table | P5 |
| Event enum definition in docs | Now |
| Service method hooks (comment/TODO) | P5 — or `ApplicationEventPublisher` stub |

### In-app vs email

Existing `notifications` table (v0.6) can consume project events later — same pattern as certificate producers in `CertificateSubmissionService`.

**Do not** implement producers in architecture review phase.

### Payload guidelines

| Include | Exclude |
|---------|---------|
| Project name, id | Secret values |
| Resource title, type | Full URLs with tokens |
| Actor name | Credential identifiers in email body (use "review in app") |
| Deep link path `/projects/{id}/...` | |

---

## 3. AI Assistant (future)

### Target questions

| Question | Retrieval strategy |
|----------|-------------------|
| Which project uses Kafka? | Junction + technology slug |
| UAT Swagger URL for Project A? | `project_environments` + `reference_type=SWAGGER_URL` + name=UAT |
| Onboarding guide for Project B? | Resource search `resource_type` + folder + title |
| Automation framework repository? | `project_repositories` search |
| Technologies used by project? | Junction from project id |

### Data modeling for reliable retrieval

| Entity | AI-useful fields |
|--------|------------------|
| Project | name, description, tags, status |
| Resource | title, description, resource_type, external_url, folder path |
| Environment | name, code, is_production |
| Env reference | reference_type, title, url |
| Repository | name, url, provider, description |
| Credential ref | name, provider, reference_identifier (non-secret) |
| Technology link | technology name, slug |

### Avoid

- Burying URLs only in unstructured description
- Using folder names as sole type indicator
- Mixing env URLs into generic link items (use typed tables)

### Future AI integration options

| Approach | When |
|----------|------|
| Structured API tools | AI calls existing search endpoints |
| Read-only materialized view | If cross-entity joins too heavy |
| Embedding index | Only after structured API insufficient |

**MVP recommendation:** Build deterministic search APIs in P4 — AI can call same endpoints.

### Permission boundary

AI Assistant must respect:

- Project visibility (PUBLIC / MEMBERS_ONLY)
- Archived hidden from employees
- Credential refs return metadata only, never secrets
- Same JWT principal as user session

---

## 4. Scheduled jobs (future — not MVP)

| Job | Purpose |
|-----|---------|
| Stale resource scanner | Set `needs_review` flag; emit events |
| Broken link checker | HEAD request; mark `link_status` |
| Review reminder digest | Weekly email to resource owners |

### Schema hooks

| Column | Table | Purpose |
|--------|-------|---------|
| `last_reviewed_at` | resources, env refs | Stale computation |
| `link_last_checked_at` | resources | Broken link job |
| `link_status` | resources | OK, BROKEN, UNKNOWN |

Add nullable in P5; jobs later.

---

## 5. Configuration keys (future)

```properties
# Examples — not implemented
app.projects.stale-threshold-days=90
app.projects.file-upload.enabled=false
app.projects.credential-providers[0].name=AWS_SECRETS_MANAGER
app.projects.credential-providers[0].console-url-template=https://...
app.projects.events.email-enabled=false
```

---

## 6. Compatibility checklist

| Future feature | Prepared by hybrid architecture? |
|----------------|-----------------------------------|
| Email on env URL change | ✓ typed env references |
| Email on stale resource | ✓ review metadata |
| AI: project stack | ✓ existing junction |
| AI: env URLs | ✓ typed env tables (P3) |
| AI: repo finder | ✓ repository table (P3) |
| In-app notifications | ✓ event-friendly services |
| Broken link checker | ✓ URL on typed entities |
| Initiative association | ✓ future junction designed |

---

## 7. Explicit non-goals (this phase)

- No email template creation
- No `ApplicationEvent` implementation
- No AI/LLM integration
- No scheduled jobs
- No notification producers wired
