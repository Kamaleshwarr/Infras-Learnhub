# Engineering Learning Hub — Project Roadmap

Last updated: 2026-06-16 (v0.5 — Profile Management Phases 1–4)

## Release Overview

| Release | Theme | Status |
|---------|-------|--------|
| v0.2 | Password Management + backend foundations | Shipped (PR #17) |
| v0.3 | User Management UI (Phases 1–3) | Shipped (PRs #18–#20) |
| v0.4 | User Management UI Phase 4 (bulk import) | Shipped (PRs #22–#24) |
| **v0.5** | **Profile Management (Phases 1–4)** | **Shipped (PR #27)** |

Release notes: `docs/releases/release-v0.5.md`  
Workstream summary: `docs/releases/profile-management-final-summary.md`

---

## Shipped — v0.5 Profile Management (Phases 1–4)

### Phase 1 — Profile View (PR #27)

- `GET /api/v1/profile` — self-service profile API
- `ProfilePage` at `/profile` with read-only profile section
- `ProfileAvatar` with initials fallback
- Sidebar **My Profile** navigation
- Flyway `V8__profile_avatar.sql` — avatar metadata on `users`

### Phase 2 — Edit Profile (PR #27)

- `PUT /api/v1/profile` — update full name and email
- Email change issues new JWT via `accessToken` in response
- `ProfileEditForm` with dirty-form guard and snackbar feedback
- `AuthProvider.refreshProfile()` for session sync

### Phase 3 — Change Password Entry (PR #27)

- Change Password button on profile → `/change-password`
- Hidden when `mustChangePassword === true`
- `MustChangePasswordRoute` fix for voluntary change-password access

### Phase 4 — Avatar Upload / Replace / Delete (PR #27)

- `POST` / `DELETE` / `GET` `/api/v1/profile/avatar`
- `AvatarStorageService` — local filesystem storage
- `ProfileAvatarUpload` — upload, replace, delete with confirm dialog
- `avatarUrl` on login and `/auth/me` user summary

---

## Shipped — v0.4 User Management UI Phase 4

### Bulk Import & Template (PR #22)

- **Bulk Import UI** — multi-step dialog: file select → preview → confirm → results
- **Import Preview** — estimated data-row count and filename before upload
- **Download Template** — header-only CSV from toolbar
- **Create-only import** — new users only; default password with forced change on first login
- **Upload lockdown** — controls disabled during preview/import
- **Result summary** — imported/failed counts and per-row errors; list refresh on success

### Import quality fixes (PRs #23, #24)

- **Blank row parser improvements** — skip trailing empty CSV/Excel rows
- **Preview/import alignment** — frontend preview matches backend skip rules
- **Header-only template** — no comment rows that Excel treats as data
- **Import role guidance** — `ADMIN`, `EMPLOYEE` only; helper text in dialog

---

## Shipped — v0.3 User Management UI (Phases 1–3)

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

## Next (awaiting approval)

No implementation in progress. Recommended next feature: **Notifications** or **Global Search** (see status report).

---

## Backlog (post v0.5)

| ID | Item | Notes |
|----|------|-------|
| UM-002 | User Details Drawer | Read-only metadata + action shortcuts |
| UM-003 | Unified cross-field search | Optional backend `search` param |
| UM-004 | View User Details | Overlaps UM-002; consolidate when scoped |
| UM-006 | Downloadable import error report | Post-import CSV/text export |
| — | Notifications | v0.6 candidate |
| — | Global Search | v0.6 candidate |
| — | AI Features | Future |

Backlog detail: `docs/backlog/user-management-ui.md`

---

## Completed Platform Modules (backend)

- Authentication
- Learning Initiatives
- Certificate Submissions
- Leaderboards
- Study Materials Repository
- Project Knowledge Repository
- User Management (API + import parsers)
- Password Management
- **Profile Management**

## Completed Frontend Modules

1. Frontend foundation
2. Authentication UI
3. Role-aware dashboard
4. Password Management UI
5. User Management UI — Phases 1–4 (list, CRUD, activate/deactivate/reset, bulk import)
6. **Profile Management UI — Phases 1–4** (view, edit, change-password entry, avatar)
