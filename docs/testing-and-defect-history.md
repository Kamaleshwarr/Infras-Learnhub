# Testing & Defect History

Last updated: 2026-06-15 (v0.3)

## Test Baselines

| Area | Command | Baseline (v0.3) |
|------|---------|-----------------|
| Frontend | `cd frontend && npm test` | **76 tests** (19 files) |
| Frontend build | `cd frontend && npm run build` | Pass |
| Backend (user mgmt) | `mvn -f backend/pom.xml test -Dtest='UserManagement*Test'` | Pass |
| Backend (full) | `mvn -f backend/pom.xml test` | Pass (integration tests require Docker) |

---

## User Management UI — Validation History

| Phase | PR | Manual validation | Merge commit |
|-------|-----|-------------------|--------------|
| Phase 1 — List | #18 | Passed | `9aa5a25` |
| Phase 2 — Create/Edit | #19 | Passed (after 2 fix rounds + UX polish) | `d707f72` |
| Phase 3 — Activate/Deactivate/Reset | #20 | **Passed** | `7c308af` |

---

## Defects — Phase 2 (Create & Edit)

| ID | Symptom | Root cause | Fix | Verified |
|----|---------|------------|-----|----------|
| UM-D01 | Success snackbar not shown after create | Dialog close triggered Snackbar `clickaway` dismiss | `UserManagementSnackbar` ignores clickaway; parent closes dialog before notification | Phase 2 validation |
| UM-D02 | `Emp001` duplicate of `EMP001` allowed | Case-sensitive employee ID check | `existsByEmployeeIdIgnoreCase` + uppercase normalization | Phase 2 validation |
| UM-D03 | Edit User 500 on save | `replaceRole()` re-inserted same `(user_id, role_id)` | Idempotent `replaceRole()`, `hasRoleName()` skip, explicit `save()` | Phase 2 validation |
| UM-D04 | Create Employee ID pre-filled with admin email | Browser autofill on dialog open | Form reset on open, `autoComplete="off"`, unique field `name` attrs | Phase 2 validation |
| UM-D05 | Edit Save enabled with no changes | No dirty-state comparison | `baseline` + `isEditFormDirty()` guard | Phase 2 validation |

---

## Phase 3 — No Post-Merge Defects

Phase 3 passed manual validation on first round. No defects logged.

### Phase 3 test coverage added

| Component / area | Tests |
|------------------|-------|
| `ConfirmActionDialog` | Identity render, confirm callback, submitting state |
| `ResetPasswordDialog` | Two-step flow, password submit |
| `UserTable` | Action visibility, self-deactivate guard, reset callback |
| `UserListPage` | Activate, deactivate, reset integration flows |
| `usersApi` | `activate`, `deactivate`, `resetPassword` contracts |
| Backend | `deactivateUserRejectsSelfDeactivation`, `toResponseIncludesMustChangePassword` |

---

## Regression Checklist (User Management)

Run before each phase merge:

1. List loads with default sort; URL sync round-trips
2. Create user → snackbar → list refresh preserves filters
3. Edit user → dirty guard → snackbar → refresh
4. Activate / deactivate with confirmation → status chip updates
5. Self-deactivate disabled (UI + API `400`)
6. Reset password two-step → snackbar includes must-change wording
7. Must Change Password column reflects API data

---

## Known limitations

| Item | Notes |
|------|-------|
| Integration tests | Some backend tests use Testcontainers; require Docker |
| Import UI | Backend import API tested; **no frontend UI until Phase 4** |
| UM-002 / UM-004 | User details view not implemented |
