# User Management UI Backlog

Items deferred beyond the approved phased rollout.

## UM-001 — Expose `mustChangePassword` in `UserResponse`

**Status:** Done (Phase 3)  
**Phase:** Backend prerequisite for list column

`UserResponse` now includes `mustChangePassword`, mapped from `User.isMustChangePassword()` in `UserManagementService.toResponse()`. The Users list shows the Must Change Password column when the API returns the field.

---

## UM-005 — Backend self-deactivation guard

**Status:** Done (Phase 3)  
**Phase:** Defense-in-depth alongside frontend guard

`PATCH /api/v1/users/{id}/deactivate` rejects requests where the target user ID matches the authenticated admin (`400` with message *"You cannot deactivate your own account"*).

---

## UM-006 — Downloadable import results / error report

**Status:** Open  
**Phase:** Backend enhancement (optional)

The Phase 4 import UI displays row-level errors inline after import. A downloadable CSV or text report of import results (including failed rows and error messages) would improve auditability for large imports.

**Scope:**

- Backend endpoint or response attachment for import result export
- Frontend "Download error report" action on the results step

---

## UM-002 — User Details Drawer

**Status:** Open  
**Phase:** Future enhancement

Read-only side drawer opened from a user row showing full account metadata (`GET /api/v1/users/{id}`), audit timestamps, and quick links to edit / reset password / activate / deactivate actions.

**Scope:**

- `UserDetailsDrawer` component
- Row click or "View" action on `UserTable`
- Optional route deep-link: `/users?userId={id}`

---

## UM-003 — Unified cross-field search

**Status:** Open  
**Phase:** Optional backend enhancement

Backend list filters are AND-combined field-specific queries. A single `search` query parameter with OR matching across `employeeId`, `fullName`, and `email` would improve quick-search UX beyond the current `fullName` mapping.

---

## UM-004 — View User Details

**Status:** Open  
**Phase:** Future enhancement

Read-only view of a single user opened from the list (drawer or dedicated panel) without entering edit mode. Shows full account metadata from `GET /api/v1/users/{id}` including employee ID, name, email, role, status, timestamps, and `mustChangePassword`.

**Scope:**

- `ViewUserDetailsDrawer` or dialog component
- Row "View" action or row click on `UserTable`
- Optional URL deep-link: `/users?userId={id}`
- Entry points to Edit, Activate/Deactivate, and Reset Password
