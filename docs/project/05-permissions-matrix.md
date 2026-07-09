# Project Module — Permissions Matrix

**Date:** 2026-07-06

---

## 1. Role model (unchanged)

The application uses **two global roles** only:

| Global role | Description |
|-------------|-------------|
| `ADMIN` | System administrator |
| `EMPLOYEE` | Standard authenticated user |

**No new global role is required** for the Project Module.

Project-specific authorization uses **project membership roles** (already implemented):

| Project role | Description |
|--------------|-------------|
| `OWNER` | Project maintainer; full project control |
| `CONTRIBUTOR` | Can add/update knowledge, environments, repos |
| `VIEWER` | Read-only project access |

---

## 2. Is project-level membership necessary?

**Yes — keep and extend.**

| Reason | Detail |
|--------|--------|
| Already implemented | V6 schema, service checks, tests |
| MEMBERS_ONLY projects | Require membership model |
| Maintainer delegation | Admins should not be sole editors |
| Audit accountability | Actions tied to project roles |
| Future notifications | "New member added" events need membership |

**Mapping to product language:**

| Product term | Implementation |
|--------------|----------------|
| Employee (view) | VIEWER, or any user for PUBLIC projects |
| Project Member / Maintainer | CONTRIBUTOR |
| Project Owner | OWNER |
| Admin | Global ADMIN |

---

## 3. Access visibility rules

| Condition | Employee access | Admin access |
|-----------|-----------------|--------------|
| PUBLIC, not archived | All authenticated users | ✓ |
| MEMBERS_ONLY, member | Members only | ✓ |
| MEMBERS_ONLY, non-member | 404 | ✓ |
| Archived | 404 | ✓ (with `includeArchived`) |

**Pattern:** 404 not 403 (matches Initiatives employee visibility).

---

## 4. Permissions matrix

Legend: ✓ allowed · — denied · (✓) admin bypass · ✓* contributor+owner · ✓** owner only

### 4.1 Project lifecycle

| Action | VIEWER | CONTRIBUTOR | OWNER | ADMIN |
|--------|--------|-------------|-------|-------|
| List/search accessible projects | ✓ | ✓ | ✓ | ✓ |
| View project overview | ✓ | ✓ | ✓ | ✓ |
| Create project | ✓* | ✓* | ✓* | ✓ |
| Update project metadata | — | — | ✓** | ✓ |
| Archive project | — | — | ✓** | ✓ |
| View archived (hidden) | — | — | — | ✓ |

*Today any employee can create — **open decision:** restrict create to ADMIN only?

### 4.2 Membership

| Action | VIEWER | CONTRIBUTOR | OWNER | ADMIN |
|--------|--------|-------------|-------|-------|
| List members | ✓ | ✓ | ✓ | ✓ |
| Add/update member | — | — | ✓** | ✓ |
| Remove member | — | — | ✓** | ✓ |
| Change own role | — | — | — | ✓ |

### 4.3 Knowledge folders & resources

| Action | VIEWER | CONTRIBUTOR | OWNER | ADMIN |
|--------|--------|-------------|-------|-------|
| Browse folders | ✓ | ✓ | ✓ | ✓ |
| Create/update folder | — | ✓* | ✓* | ✓ |
| Delete empty folder | — | — | ✓** | ✓ |
| Create/update link resource | — | ✓* | ✓* | ✓ |
| Upload file (future) | — | ✓* | ✓* | ✓ |
| Delete resource | — | — | ✓** | ✓ |
| Download file / open link | ✓ | ✓ | ✓ | ✓ |
| Pin quick link | — | ✓* | ✓* | ✓ |

### 4.4 Environments & repositories (proposed)

| Action | VIEWER | CONTRIBUTOR | OWNER | ADMIN |
|--------|--------|-------------|-------|-------|
| View environments/repos | ✓ | ✓ | ✓ | ✓ |
| Create/update env/repo | — | ✓* | ✓* | ✓ |
| Delete env/repo | — | — | ✓** | ✓ |

### 4.5 Credential references (proposed)

| Action | VIEWER | CONTRIBUTOR | OWNER | ADMIN |
|--------|--------|-------------|-------|-------|
| View credential refs | ✓ | ✓ | ✓ | ✓ |
| Create/update refs | — | ✓* | ✓* | ✓ |
| Delete refs | — | — | ✓** | ✓ |

**Future option:** Restrict credential ref visibility to CONTRIBUTOR+ on MEMBERS_ONLY projects (open decision).

### 4.6 Learn technology links

| Action | VIEWER | CONTRIBUTOR | OWNER | ADMIN |
|--------|--------|-------------|-------|-------|
| View on project/tech pages | ✓ | ✓ | ✓ | ✓ |
| Add/remove tech ↔ project link | — | — | — | ✓ |

Technology links remain **admin-only** (current `LearnTechnologyManageController`).

### 4.7 Initiative links (future)

| Action | VIEWER | CONTRIBUTOR | OWNER | ADMIN |
|--------|--------|-------------|-------|-------|
| View initiative ↔ project | ✓ | ✓ | ✓ | ✓ |
| Manage links | — | — | — | ✓ |

---

## 5. Implementation pattern (reuse)

Continue existing `ProjectKnowledgeService` pattern:

```text
requireReadAccess(project, principal)
requireContributor(project, principal)
requireOwner(project, principal)
isAdmin(principal) → bypass
```

Apply `@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")` at service class level; fine-grained checks inside methods.

---

## 6. Credential reference security

| Rule | Enforcement |
|------|-------------|
| No secret values in DB | DB constraints + service validation |
| No secret values in API responses | DTO review |
| Reference identifier is non-sensitive | Validation regex blocklist |
| Access instructions are text only | Max length limit |
| Audit credential changes | `updated_by`, future audit events |

---

## 7. Open permission decisions

| # | Question | Options |
|---|----------|---------|
| P1 | Who can create projects? | A) Any employee (current) B) Admin only C) Admin + delegated creators |
| P2 | Credential ref visibility | A) All readers B) Contributor+ only |
| P3 | File upload (future) | A) Contributor+ B) Owner only |

Recommend **P1: Admin only** for enterprise governance; employees request projects via process.
