# User Management UI Backlog

Items deferred beyond the approved phased rollout.

## UM-001 — Expose `mustChangePassword` in `UserResponse`

**Status:** Open  
**Phase:** Backend prerequisite for list column

The `users` table and `User` entity already store `must_change_password`, but `UserResponse` does not include the field. The Phase 1 list page detects the column dynamically when any list item includes `mustChangePassword`; until this backend change ships, the column stays hidden.

**Required backend change:**

1. Add `boolean mustChangePassword` to `UserResponse`.
2. Map `user.isMustChangePassword()` in `UserManagementService.toResponse()`.
3. Update controller tests and OpenAPI schema.

**Frontend follow-up:** Column appears automatically once the API returns the field.

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
