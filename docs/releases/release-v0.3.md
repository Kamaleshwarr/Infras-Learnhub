# Engineering Learning Hub v0.3

**Theme:** User Management UI (Phases 1–3)  
**Merged:** 2026-06-15  
**PRs:** #18 (Phase 1), #19 (Phase 2), #20 (Phase 3)

---

## Completed Features

### User Management UI — Phase 1 (PR #18)

- Admin-only `/users` route and navigation
- Paginated user list with sortable columns
- Filters: employee ID, full name, email, role, status
- URL query synchronization for list state
- Empty, loading, and error states

### User Management UI — Phase 2 (PR #19)

- Create User dialog (validation, normalization, snackbar)
- Edit User dialog (fresh fetch, dirty-form guard, self-role guard)
- Shared error handling (`apiErrors`, email/employeeId utils)
- Backend: case-insensitive employee ID, idempotent role update

### User Management UI — Phase 3 (PR #20)

- Activate / Deactivate user with confirmation dialogs
- Reset Password (two-step confirmation + policy validation)
- Row action toolbar on user table
- Self-deactivation protection (frontend + backend)
- **Must Change Password** column via UM-001
- Backend self-deactivation guard (UM-005)

---

## Backend Changes (v0.3)

| Change | PR |
|--------|-----|
| `UserResponse.mustChangePassword` | #20 |
| Self-deactivation guard on `PATCH /users/{id}/deactivate` | #20 |
| Case-insensitive employee ID on create/update/import | #19 |
| Idempotent `User.replaceRole()` | #19 |

---

## APIs Used by UI

| Action | Method | Endpoint |
|--------|--------|----------|
| List | `GET` | `/api/v1/users` |
| Get | `GET` | `/api/v1/users/{id}` |
| Create | `POST` | `/api/v1/users` |
| Update | `PUT` | `/api/v1/users/{id}` |
| Activate | `PATCH` | `/api/v1/users/{id}/activate` |
| Deactivate | `PATCH` | `/api/v1/users/{id}/deactivate` |
| Reset password | `POST` | `/api/v1/users/{id}/reset-password` |

Bulk import APIs exist but **UI ships in Phase 4**:

| Action | Method | Endpoint |
|--------|--------|----------|
| Import | `POST` | `/api/v1/users/import` |
| Template | `GET` | `/api/v1/users/import/template` |

---

## Test Summary

- Frontend: **76 tests**, production build green
- Backend user management unit tests green
- Manual validation passed for Phases 1–3

See `docs/testing-and-defect-history.md` for defect log and regression checklist.

---

## Pending (v0.4 target)

- User Management UI **Phase 4** — Bulk import + template download
- Profile Management
- Notifications
- Global Search

Roadmap: `docs/project-roadmap.md`
