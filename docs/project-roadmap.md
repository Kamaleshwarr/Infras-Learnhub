# Engineering Learning Hub — Project Roadmap

Last updated: 2026-06-15 (v0.3 — User Management UI Phases 1–3)

## Release Overview

| Release | Theme | Status |
|---------|-------|--------|
| v0.2 | Password Management + backend foundations | Shipped (PR #17) |
| **v0.3** | **User Management UI (Phases 1–3)** | **Shipped (PRs #18–#20)** |
| v0.4 | User Management UI Phase 4 (bulk import) | Planned |

Release notes: `docs/releases/release-v0.3.md`

---

## Shipped — v0.3 User Management UI

### Phase 1 — Users List (PR #18)

- `/users` route with `ADMIN` role guard
- Paginated, sortable user table
- Field filters (employee ID, name, email, role, status)
- URL query sync for filters, sort, and pagination
- Dynamic **Must Change Password** column (when API exposes field)

### Phase 2 — Create & Edit User (PR #19)

- `CreateUserDialog` with validation, normalization, autofill prevention
- `EditUserDialog` with fresh `GET /users/{id}`, dirty-form guard, self-role guard
- Success snackbars and list refresh preserving query state
- Backend fixes: case-insensitive employee ID, idempotent role update

### Phase 3 — Activate, Deactivate, Reset Password (PR #20)

- Row actions: activate, deactivate, reset password, edit
- `ConfirmActionDialog` for activate/deactivate with audit-friendly identity display
- Two-step `ResetPasswordDialog` with must-change-password messaging
- Self-deactivation protection (frontend + backend UM-005)
- UM-001: `mustChangePassword` exposed in `UserResponse`

---

## In Progress / Next

### Phase 4 — Bulk User Import UI (pending approval)

- Bulk import dialog (CSV, XLS, XLSX)
- Download import template from toolbar
- Import result summary with per-row errors
- List refresh after successful import

See approved architecture in Phase 4 plan (awaiting implementation approval).

---

## Backlog (post Phase 4)

| ID | Item | Notes |
|----|------|-------|
| UM-002 | User Details Drawer | Read-only metadata + action shortcuts |
| UM-003 | Unified cross-field search | Optional backend `search` param |
| UM-004 | View User Details | Overlaps UM-002; consolidate when scoped |
| — | Profile Management | v0.4+ |
| — | Notifications | v0.4+ |
| — | Global Search | v0.4+ |

Backlog detail: `docs/backlog/user-management-ui.md`

---

## Completed Platform Modules (backend)

- Authentication
- Learning Initiatives
- Certificate Submissions
- Leaderboards
- Study Materials Repository
- Project Knowledge Repository
- User Management (API)
- Password Management

## Completed Frontend Modules

1. Frontend foundation
2. Authentication UI
3. Role-aware dashboard
4. Password Management UI
5. **User Management UI — Phases 1–3**
