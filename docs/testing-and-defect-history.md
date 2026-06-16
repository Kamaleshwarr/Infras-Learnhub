# Testing & Defect History

Last updated: 2026-06-16 (v0.6 — Notification Infrastructure shipped)

## Test Baselines

| Area | Command | Baseline (v0.6) |
|------|---------|-----------------|
| Frontend | `cd frontend && npm test` | **140 tests** (33 files) |
| Frontend build | `cd frontend && npm run build` | Pass |
| Backend (notifications) | `mvn -f backend/pom.xml test -Dtest='com.company.learninghub.notification.**'` | Pass |
| Backend (profile) | `mvn -f backend/pom.xml test -Dtest='Profile*Test'` | Pass |
| Backend (user mgmt) | `mvn -f backend/pom.xml test -Dtest='UserManagement*Test'` | Pass |
| Backend (full) | `mvn -f backend/pom.xml test` | Pass (10 integration tests skipped without Docker) |

---

## Notifications — Validation History (v0.6 Infrastructure)

| Area | Status | Notes |
|------|--------|-------|
| Backend startup | **Passed** | NTF-D01 — `@Autowired` on `NotificationService` constructor |
| Notification APIs | **Passed** | List, unread-count, mark-read, mark-all-read |
| Bell and inbox UI | **Passed** | Bell, dropdown, `/notifications`, sidebar |
| Badge synchronization | **Passed** | NTF-D02 — `NotificationProvider` + `refresh()` |
| Account producers removed | **Passed** | Option B — no in-app generation from user management |
| Certificate producers (service layer) | **Passed** | Unit tests in `CertificateSubmissionServiceTest` |
| Certificate producers (application E2E) | **Deferred v0.6.1** | Blocked — submit/review pages are placeholders |
| Scope classification | **Approved** | v0.6 = infrastructure; E2E = v0.6.1 |

---

## Defects — Notifications (v0.6)

| ID | Symptom | Root cause | Fix | Verified |
|----|---------|------------|-----|----------|
| NTF-D01 | Backend failed to start; `NotificationService` `BeanInstantiationException` | Multiple constructors without `@Autowired` on production entry point | `@Autowired` on public constructor | Backend startup |
| NTF-D02 | Bell badge stale after mark-read on `/notifications` | Isolated unread-count state in bell hook vs page | `NotificationProvider` + `refresh()` after read mutations | Infrastructure validation |

---

## Notifications — Test coverage added

| Component / area | Tests |
|------------------|-------|
| `NotificationServiceTest` | Inbox, unread count, mark read, certificate + factory helpers |
| `NotificationFactoryTest` | Message templates |
| `NotificationControllerTest` | API contracts |
| `NotificationMethodSecurityTest` | `@PreAuthorize` on inbox APIs |
| `CertificateSubmissionServiceTest` | Producer calls on submit/approve/reject |
| `NotificationBell.test.tsx` | Badge render, dropdown, mark-all-read sync |
| `NotificationsPage.test.tsx` | List, filters, mark-all-read, badge sync regression |
| `MustChangePasswordFilterTest` | Notification API allowlist |

---

## Regression Checklist (Notifications — v0.6 Infrastructure)

Validated at v0.6 merge:

1. Backend starts; Flyway `V9` applied
2. `GET /notifications/unread-count` matches bell badge
3. Mark-read on `/notifications` updates badge immediately
4. Mark-read / mark-all-read in dropdown updates badge immediately
5. User create, activate, deactivate, reset-password → **no** new in-app notification
6. Historical account-type rows (if present) still list correctly
7. `mustChangePassword` user can access `/notifications` and bell APIs

## Regression Checklist (Notifications — v0.6.1 E2E, proposed)

Deferred until certificate workflow UI ships:

1. Certificate submit via Submit Certificate page → all active admins notified (`CERTIFICATE_SUBMITTED`)
2. Approve via Admin Review page → employee notified (`CERTIFICATE_APPROVED`)
3. Reject via Admin Review page → employee notified (`CERTIFICATE_REJECTED`)
4. Badge and inbox reflect new notifications after each workflow step

---

## Profile Management — Validation History

| Phase | PR | Manual validation | Notes |
|-------|-----|-------------------|-------|
| Phase 1 — Profile View | #27 | **Passed** | — |
| Phase 2 — Edit Profile | #27 | **Passed** (after fix) | PM-D01 import collision |
| Phase 3 — Change Password Entry | #27 | **Passed** (after fix) | PM-D02 route redirect |
| Phase 4 — Avatar | #27 | **Passed** | — |

---

## Defects — Profile Management

| ID | Phase | Symptom | Root cause | Fix | Verified |
|----|-------|---------|------------|-----|----------|
| PM-D01 | 2 | App failed to load; `ProfileEditForm` import error | Case-insensitive FS collision: `profileEditForm.ts` vs `ProfileEditForm.tsx` | Renamed utility to `profileFormState.ts` | Phase 2 validation |
| PM-D02 | 3 | Change Password redirected to `/` | `MustChangePasswordRoute` blocked voluntary `/change-password` access | Removed redirect; added route tests | Phase 3 validation |

---

## Profile Management — Test coverage added

| Component / area | Tests |
|------------------|-------|
| `ProfileServiceTest` | Get/update profile, email duplicate, avatar upload/replace/delete, validation |
| `ProfileControllerTest` | API contracts, multipart upload, avatar GET/DELETE |
| `ProfileMethodSecurityTest` | `@PreAuthorize` on profile service |
| `ProfilePage` | Page render, view/edit integration |
| `ProfileEditForm` / `profileFormState` | Dirty guard, validation |
| `ProfileAvatar` | Initials fallback, blob display |
| `ProfileAvatarUpload` | Upload/replace/delete flows |
| `ProfileNavigation` | Sidebar link, route access |
| `MustChangePasswordRoute` | Voluntary change-password navigation |
| `profileApi` | API client contracts |
| `profileInitials` | Initials derivation |

---

## Regression Checklist (Profile Management)

Run before profile phase merge:

1. `/profile` loads for authenticated `ADMIN` and `EMPLOYEE`
2. Profile fields match `GET /api/v1/profile` response
3. Edit profile → dirty guard → snackbar → refresh; email change updates JWT
4. Duplicate email rejected with error message
5. Change Password button navigates to `/change-password` when `mustChangePassword` is false
6. Change Password button hidden when `mustChangePassword` is true
7. Avatar upload (valid image) → displays image; replace overwrites; delete reverts to initials
8. Invalid avatar type or oversize file rejected with error
9. `DELETE /avatar` when no avatar returns success (idempotent)
10. Sidebar **My Profile** link visible and active on `/profile`

---

## User Management UI — Validation History

| Phase | PR | Manual validation | Merge commit |
|-------|-----|-------------------|--------------|
| Phase 1 — List | #18 | Passed | `9aa5a25` |
| Phase 2 — Create/Edit | #19 | Passed (after 2 fix rounds + UX polish) | `d707f72` |
| Phase 3 — Activate/Deactivate/Reset | #20 | **Passed** | `7c308af` |
| Phase 4 — Bulk Import | #22–#24 | **Passed** (after blank-row + template fixes) | `5b694c0` |

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

## Defects — Phase 4 (Bulk Import)

| ID | Symptom | Root cause | Fix | Verified |
|----|---------|------------|-----|----------|
| UM-D06 | Trailing blank CSV rows reported as `Missing required values` | Comma-only lines not skipped by parser | `isBlankImportRow()` in CSV + Excel parsers | PR #23 validation |
| UM-D07 | Preview row count higher than importable rows | Frontend preview did not skip blank rows | `userImportPreview.ts` aligned with backend | PR #23 validation |
| UM-D08 | Template comment row visible as data in Excel | `# Valid role values...` line in CSV template | Header-only template (PR #24); role guidance in dialog only | PR #24 validation |
| UM-D09 | Users enter `Manager` / `Administrator` roles | No inline role hint in import UI | Dialog helper: `Valid role values: ADMIN, EMPLOYEE` | Phase 4 validation |

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

### Phase 4 test coverage added

| Component / area | Tests |
|------------------|-------|
| `BulkImportDialog` | Step flow, preview, upload lockdown, results |
| `UserListToolbar` | Download Template, Import Users actions |
| `userImportPreview` | Blank rows, comment lines, row count |
| `downloadBlob` | Blob download helper |
| `usersApi` | `importUsers`, `downloadImportTemplate` |
| `UserListPage` | Import dialog open, template download, refresh on complete |
| Backend | Blank CSV/Excel rows, invalid roles, header-only template |

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
8. Download template → header-only CSV
9. Import valid file → preview count matches import → results + refresh
10. Import file with trailing blank rows → no spurious row errors
11. Invalid role values rejected with row-level message

---

## Known limitations

| Item | Notes |
|------|-------|
| Integration tests | Some backend tests use Testcontainers; require Docker |
| UM-002 / UM-004 | User details view not implemented |
| UM-006 | No downloadable import error report yet |
| Import | Create-only; no update existing users via import |
| Avatar storage | Local filesystem only; no cloud/S3 provider yet |
| Notification E2E | Certificate producers backend-only; UI workflows in v0.6.1 |
| Certificate submission UI | Submit / My Submissions / Admin Review pages are placeholders |
